package com.cd.mips.simulator;

import java.util.ArrayList;
import java.util.HashMap;

import com.cda.mips.disassembler.Command;
import com.cda.mips.disassembler.Constants;
import com.cda.mips.disassembler.Disassembler;

public class MIPSProcessor {

	//Current cycle of processor
	int cycle;

	ArrayList<Command> IQ = new ArrayList<Command>(); // Holds Instruction Queue

	static int RS_SIZE = 8;
	ArrayList<Command> RS = new ArrayList<Command>(); // Holds Instruction Queue

	static int ROB_SIZE = 10;
	ArrayList<Command> ROB = new ArrayList<Command>(); // Holds Instruction
														// Queue

	// Map to hold mappings between commands and ROB
	HashMap<Integer, Integer> rsToROB = new HashMap<Integer, Integer>();

	Register PC = new Register(); // Holds current PC

	ArrayList<BTB> BTBEntries = new ArrayList<BTB>(); // Stores the 10-entry BTB

	static int REG_SIZE = 32;
	ArrayList<Register> Registers = new ArrayList<Register>(); // Stores 32
																// Registers
	Disassembler disassembler;

	//Flag if processor is stalling
	boolean stall = false;

	//Stores the state of each stage of the processor
	public Fetch fetchStage = new Fetch();
	public Decode decodeStage = new Decode();
	public Execute executeStage = new Execute();
	public WriteBack writeBackStage = new WriteBack();
	public Commit commitStage = new Commit();
	
	//Stores system constants
	public Constants constants = new Constants();
	boolean breakFound = false;

	public MIPSProcessor(Disassembler d) {
		disassembler = d;
		cycle = 1;

		// Initializes the registers
		for (int i = 0; i < REG_SIZE; i++) {
			Register r = new Register();
			Registers.add(r);
		}

		for (int i = 0; i < RS_SIZE; i++) {
			RS.add(null);
		}

		for (int i = 0; i < ROB_SIZE; i++) {
			ROB.add(null);
		}

		//Set the PC to the PC of the first command
		PC.value = d.commands.get(0).getAddress();
	}

	/*
	 * This function will process one cycle of the processor.
	 */
	public boolean nextCycle() {
		//End program if break has been found
		if(breakFound){
			return false;
		}
		
		Command c = null;
		/*
		 * Instruction Fetch Stage
		 */
		if (!stall) {
			Command command = disassembler.getCommand(PC.value);
			if (!processCommand(command)) {
				System.out.println("Failed to process command");
			}
			fetchStage.updateCommand(command);

			/*
			 * Decode and Issue Stage
			 */
			c = fetchStage.prevCommand;
			if (c != null) {
				decodeStage.updateCommand(c);
				issueCommand(c);
			}
		}
		/*
		 * Execute Stage
		 */
		c = decodeStage.prevCommand;
		if (c != null) {
			stall = executeCommand(c);
			if (!stall) {
				executeStage.updateCommand(c);
			} else {
				executeStage.updateCommand(null);
			}
		}

		/*
		 * Write Result Stage
		 */
		c = executeStage.prevCommand;
		writeBackStage.updateCommand(c);
		if (c != null) {
			writeCommand(c);
		}

		/*
		 * Commit Stage
		 */
		c = writeBackStage.prevCommand;
		commitStage.updateCommand(c);
		if (c != null) {
			commitCommand(c);
		}

		if (!stall && !breakFound) {
			int newPC = Integer.valueOf(PC.value) + 4;
			PC.value = newPC + "";
		}
		
		if(!breakFound){
			cycle++;
		}
		
		return true;
	}

	public String toString() {
		return printProcessorState();
	}

	/*
	 * Creates a string representation of the processor and its contents
	 */
	public String printProcessorState() {
		String state = "";

		state = "Cycle<" + cycle + ">:\n";
		state = state + "IQ:\n";

		int j = 1;
		for (Command c : IQ) {
			state = state + "[inst" + j + "] " + c.toAddrAssembly() + "\n";
			j++;
		}
		j = 1;

		state = state + "RS:\n";

		for (int i = 0; i < RS.size(); i++) {
			if (RS.get(i) == null) {
				continue;
			}

			Command c = RS.get(i);
			state = state + "[inst" + i + "] " + c.toAddrAssembly() + "\n";
		}

		state = state + "ROB:\n";

		for (int i = 0; i < ROB.size(); i++) {
			if (ROB.get(i) == null) {
				continue;
			}
			Command c = ROB.get(i);
			state = state + "[inst" + i + "] " + c.toAddrAssembly() + "\n";
		}

		state = state + "BTB:\n";

		for (int i = 0; i < BTBEntries.size(); i++) {
			BTB btb = BTBEntries.get(i);

			state = state + "[Entry " + i + "]: " + btb.toString() + "\n";
		}

		state = state + "Registers:";

		for (int i = 0; i < Registers.size(); i++) {
			if (i % 8 == 0) {
				state = state + "\nR" + i + ": ";
			}
			state = state + Registers.get(i).value + " ";
		}

		state = state + "\nData Segment:";

		int start = 700;
		int i = 0;

		while (true) {
			int curAddress = start + i;
			Command c = disassembler.getCommand(curAddress + "");

			if (c == null) {
				break;
			}

			if (i % 32 == 0) {
				state = state + "\n" + curAddress + ": ";
			}

			state = state + c.getAssembly() + " ";

			i = i + 4;
		}
		
		state = state +"\n\r";

		return state;
	}

	/*
	 * This function checks to see if the BTB entry is already in the
	 * BTB entry list.
	 */
	public BTB btbContainsAddr(String address) {
		int currAddress = Integer.valueOf(address);

		BTB result = null;
		for (BTB btb : BTBEntries) {
			if (btb.instructionAddr == currAddress) {
				result = btb;
				break;
			}
		}

		return result;
	}

	/*
	 * This function adds/updates a BTB entry in the list
	 */
	public boolean addBTBEntry(BTB btb) {
		boolean result = false;

		BTB btbUpdate = btbContainsAddr(btb.instructionAddr + "");

		if (btbUpdate == null) {
			// Not already in the btb entry list, add it
			BTBEntries.add(btb);
		} else {
			btbUpdate.twoBitPredictor = btb.twoBitPredictor;
		}

		return result;
	}

	/*
	 * This function simulates the IF stage of the processor
	 */
	public boolean processCommand(Command c) {
		boolean result = true;

		if (c == null) {
			// Error has occurred
			System.out.println("Error has occurred, no instruction at addr: "
					+ PC.value);
			result = false;
		} else {
			//System.out.println("Command Processed! " + c.toAddrAssembly());

			IQ.add(c);

			// Check to see if addr is in BTB
			BTB btb = btbContainsAddr(PC.value);
			if (btb != null) {
				// Address is contained in BTB map
//				System.out.println("Address: " + PC.value
//						+ " , contained in BTB entries");
			}
		}

		return result;
	}

	public boolean issueCommand(Command c) {
		boolean result = false;
		
		if (c.getAssembly().equalsIgnoreCase("BREAK")) {
			breakFound = true;
			flushPipeline();
		}

		int foundEmptyRSIndex = -1;
		for (int i = 0; i < RS_SIZE; i++) {
			Command temp = RS.get(i);
			if (temp == null) {
				foundEmptyRSIndex = i;
				break;
			}
		}

		if (foundEmptyRSIndex == -1) {
			System.out.println("No empty registers in RS");
			return false;
		}

		int foundEmptyROBIndex = -1;
		for (int i = 0; i < ROB_SIZE; i++) {
			Command temp = ROB.get(i);
			if (temp == null) {
				foundEmptyROBIndex = i;
				break;
			}
		}

		if (foundEmptyROBIndex == -1) {
			System.out.println("No empty registers in ROB");
			return false;
		}

		// Create mapping between ROB and RS
		RS.add(foundEmptyRSIndex, c);
		ROB.add(foundEmptyROBIndex, c);
		rsToROB.put(foundEmptyRSIndex, foundEmptyROBIndex);

//		System.out.println("Command Issued! RS[" + foundEmptyRSIndex
//				+ "] -> ROB[" + foundEmptyROBIndex + "] " + c.toAddrAssembly());

		return result;
	}

	// return true for stall
	public boolean executeCommand(Command c) {
		boolean result = false;
		// Check for hazards

		if (c == null) {
			return result;
		}
		String rRS = c.getRS();
		String rRT = c.getRT();

		for (Command cTemp : ROB) {
			if (cTemp == null) {
				continue;
			}

			String RSc = cTemp.getRS();
			String RTc = cTemp.getRT();

			if (cTemp == c) {
				break;
			} else if (RTc.equalsIgnoreCase(rRS) || RSc.equalsIgnoreCase(rRT)) {
//				System.out.println("Did not execute command: "
//						+ c.toAddrAssembly());
				return true;
			}
		}

		// Jump or Branch Instructions
		if (c.getOPT().equals(Constants.BEQ)) {
			Register RT = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRT())));
			Register RS = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRS())));

			if (RT.value.equalsIgnoreCase(RS.value)) {
				// Branch Taken
				BTB btb = new BTB();
				btb.branchAddr = Integer.valueOf(PC.value) + MIPSutil.getOffset(c)-4;
				btb.instructionAddr = Integer.valueOf(c.getAddress());
				btb.twoBitPredictor = "11";
				
				PC.value = btb.branchAddr+"";
				
				flushPipeline();

				addBTBEntry(btb);
			} else {
				// Branch Not taken
				BTB btb = new BTB();
				btb.branchAddr = Integer.valueOf(PC.value) + 4;
				btb.instructionAddr = Integer.valueOf(c.getAddress());
				btb.twoBitPredictor = "00";

				addBTBEntry(btb);
			}
		}else if (c.getOPT().equals(Constants.J)) {

			//Update PC 
			int newPC = MIPSutil.getInstr_index(c)-4;
			PC.value = newPC +"";
			//Flush out fetch and decode stages on a jump
			flushPipeline();
			
		}

		//System.out.println("Command Executed! " + c.toAddrAssembly());

		return result;
	}

	/*
	 * This function flushes the pipeline
	 */
	private void flushPipeline() {
		//System.out.println("Flushing pipeline");
		ROB.remove(fetchStage.command);
		RS.remove(fetchStage.command);
		fetchStage.command = null;
		
		ROB.remove(fetchStage.prevCommand);
		RS.remove(fetchStage.prevCommand);
		fetchStage.prevCommand = null;
		
		ROB.remove(decodeStage.command);
		RS.remove(decodeStage.command);
		decodeStage.command = null;
		
		ROB.remove(decodeStage.prevCommand);
		RS.remove(decodeStage.prevCommand);
		decodeStage.prevCommand = null;
	}

	public void writeCommand(Command c) {
		//System.out.println("Command WriteBack! " + c.toAddrAssembly());

		if ((c.getOPT() + c.getRS() + c.getRT() + c.getRD() + c.getIMA() + c
				.getIMB()).equals(Constants.NOP)) {
			// NOP
		} else if (c.getOPT().equals(Constants.SW)
				|| c.getOPT().equals(Constants.LW)) {

			Register RT = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRT())));
			Register RS = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRS())));
			String value = MIPSutil.getImmediate(c, true)
					+ Integer.valueOf(RS.value) + "";

			if (c.getOPT().equals(Constants.SW)) {
				Command memory = disassembler.getCommand(value);
				memory.setAssembly(RT.value);
			} else {
				RT.value = disassembler.getCommand(value).getAssembly();
			}

		} else if (c.getOPT().equals(Constants.J)) {

			PC.value = "" + MIPSutil.getInstr_index(c);

		} else if (c.getOPT().equals(Constants.BEQ)) {

			Register RT = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRT())));
			Register RS = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRS())));

			if (RT.value.equalsIgnoreCase(RS.value)) {
				// Branch Taken
			} else {
				// Branch Not taken
			}

		} else if ((c.getOPT() + c.getRT()).equals(Constants.BGEZ_RT)
				|| (c.getOPT() + c.getRT()).equals(Constants.BGTZ_RT)
				|| (c.getOPT() + c.getRT()).equals(Constants.BLEZ_RT)
				|| (c.getOPT() + c.getRT()).equals(Constants.BLTZ_RT)) {

		} else if (c.getOPT().equals(Constants.BNE)) {
			Register RT = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRT())));
			Register RS = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRS())));

			if (!RT.value.equalsIgnoreCase(RS.value)) {
				// Branch Taken
			} else {
				// Branch Not taken
			}

		} else if (c.getOPT().equals(Constants.ADDI)
				|| c.getOPT().equals(Constants.ADDIU)
				|| c.getOPT().equals(Constants.SLTI)) {

			Register RT = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRT())));
			Register RS = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRS())));
			int value = MIPSutil.getImmediate(c, true);

			if (c.getOPT().equalsIgnoreCase(Constants.ADDI)) {
				RT.value = Integer.parseInt(RS.value) + value + "";
			}
		} else if ((c.getOPT() + c.getFunction()).equals(Constants.BREAK_FUNC)) {
			//BREAK OP

		} else if ((c.getOPT() + c.getFunction()).equals(Constants.ADD_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.ADDU_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SLT_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SLTU_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SUB_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SUBU_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.AND)
				|| (c.getOPT() + c.getFunction()).equals(Constants.OR)
				|| (c.getOPT() + c.getFunction()).equals(Constants.XOR)
				|| (c.getOPT() + c.getFunction()).equals(Constants.NOR)) {

			Register RT = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRT())));
			Register RS = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRS())));
			Register RD = Registers.get(MIPSutil
					.getRegisterIndex(constants.regMap.get(c.getRD())));

			int r = -1;
			switch (c.getOPT() + c.getFunction()) {
			case Constants.ADD_FUNC:
				RD.value = Integer.valueOf(RS.value) + Integer.valueOf(RT.value) +"";
				break;
			case Constants.ADDU_FUNC:
				RD.value = Integer.valueOf(RS.value) + Integer.valueOf(RT.value) +"";
				break;
			case Constants.SLT_FUNC:
				if(Integer.valueOf(RS.value) < Integer.valueOf(RT.value)){
					RD.value = "1";
				}else{
					RD.value = "0";
				}
				break;
			case Constants.SLTU_FUNC:
				if(Integer.valueOf(RS.value) < Integer.valueOf(RT.value)){
					RD.value = "1";
				}else{
					RD.value = "0";
				}
				break;
			case Constants.SUB_FUNC:
				RD.value = Integer.valueOf(RS.value) - Integer.valueOf(RT.value) +"";
				break;
			case Constants.SUBU_FUNC:
				RD.value = Integer.valueOf(RS.value) - Integer.valueOf(RT.value) +"";
				break;
			case Constants.AND:
				r = Integer.valueOf(RS.value) & Integer.valueOf(RT.value);
				RD.value = r + "";
				break;
			case Constants.OR:
				r = Integer.valueOf(RS.value) | Integer.valueOf(RT.value);
				RD.value = r + "";
				break;
			case Constants.XOR:
				r = Integer.valueOf(RS.value) ^ Integer.valueOf(RT.value);
				RD.value = r + "";
				break;
			case Constants.NOR:
				r = Integer.valueOf(RS.value) | Integer.valueOf(RT.value);
				RD.value = r + "";
				break;
			}
		} else if ((c.getOPT() + c.getFunction()).equals(Constants.SLL_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SRL_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SRA_FUNC)) {
		}
	}

	public boolean commitCommand(Command c) {
		//System.out.println("Command Commit! " + c.toAddrAssembly());
		
		RS.remove(c);
		ROB.remove(c);

		return true;
	}
}
