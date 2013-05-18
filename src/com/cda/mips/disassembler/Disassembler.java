package com.cda.mips.disassembler;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public class Disassembler {

	public ArrayList<Command> commands;
	public Constants constants = new Constants();
	private static int BREAK_ADDRESS_MAX = 700;

	public Disassembler() {
		commands = new ArrayList<Command>();
	}

	public Disassembler(String inputFile) {
		commands = new ArrayList<Command>();

		loadCommands(inputFile);

		//System.out.println(this);
	}
	
	public void loadCommands(String inputFile) {
		try {

			FileInputStream fs = new FileInputStream(inputFile);

			int read;

			String command = "";

			int count = 0;
			int address = 588;
			boolean breakFound = false;

			while ((read = fs.read()) != -1) {
				String byteSection = Integer.toBinaryString(read);

				int sectionDiff = 8 - byteSection.length();

				while (sectionDiff != 0) {
					byteSection = "0" + byteSection;
					sectionDiff--;
				}

				count++;
				command = command + byteSection;
				if (count == 4) {

					Command newCommand = new Command();

					if (address > BREAK_ADDRESS_MAX && !breakFound) {
						newCommand.setOPT("000000");
						newCommand.setRS("00000");
						newCommand.setRT("00000");
						newCommand.setRD("00000");
						newCommand.setIMA("00000");
						newCommand.setIMB("001101");
						newCommand.setAddress(Integer.toString(address));

					} else {
						newCommand.setOPT(command.substring(0, 6));
						newCommand.setRS(command.substring(6, 11));
						newCommand.setRT(command.substring(11, 16));
						newCommand.setRD(command.substring(16, 21));
						newCommand.setIMA(command.substring(21, 26));
						newCommand.setIMB(command.substring(26, 32));
						newCommand.setAddress(Integer.toString(address));
						
					}

					newCommand.setAssembly(processCommand(newCommand,
							breakFound));

					commands.add(newCommand);

					if (newCommand.getAssembly().equalsIgnoreCase("BREAK")) {
						breakFound = true;
					}

					command = "";
					count = 0;
					address += 4;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String disassembler = "";

		for (Command c : commands) {
			disassembler = disassembler + c.toString() + "\n";
		}

		return disassembler;
	}

	public int getImmediate(Command c, boolean signed) {
		int immediate = 0;
		String RD;

		if (signed) {
			if (c.getRD().charAt(0) == '1') {
				RD = c.getRD().substring(1, c.getRD().length());
				Double immediateD = Integer.valueOf(RD + c.getIMA() + c.getIMB(), 2) - (Math.pow(2, 15));
				immediate = immediateD.intValue();
			} else {
				RD = c.getRD();
				immediate = Integer.valueOf(RD + c.getIMA() + c.getIMB(), 2);
			}
		} else {
			RD = c.getRD();
			immediate = Integer.valueOf(RD + c.getIMA() + c.getIMB(), 2);
		}

		return immediate;
	}

	public Integer getValue(Command c, boolean signed) {
		Integer value = 0;
		String sValue = c.getOPT() + c.getRS() + c.getRT() + c.getRD()
				+ c.getIMA() + c.getIMB();

		if (signed && sValue.charAt(0) == '1') {
			sValue = c.getRD().substring(1, c.getRD().length());
			Double valueDouble = Integer.valueOf(sValue, 2) - (Math.pow(2, 31) - 1);
			
			value = valueDouble.intValue();
		} else {
			value = Integer.valueOf(sValue, 2);
		}

		return value;
	}

	public int getInstr_index(Command c) {
		int instr_index = 0;

		instr_index = Integer.valueOf(
				c.getRS() + c.getRT() + c.getRD() + c.getIMA() + c.getIMB(), 2) * 4;

		return instr_index;
	}

	public int getOffset(Command c) {
		int offset = 0;

		offset = Integer.valueOf(c.getRD() + c.getIMA() + c.getIMB(), 2) * 4;

		return offset;
	}

	public String processCommand(Command c, boolean afterBreak) {
		String result = "";

		if (afterBreak) {
			//Load the 32 bit signed in value
			result = getValue(c, true).toString();
		} else if ((c.getOPT() + c.getRS() + c.getRT() + c.getRD() + c.getIMA() + c
				.getIMB()).equals(Constants.NOP)) {
			result = constants.optMap.get((c.getOPT() + c.getRS() + c.getRT()
					+ c.getRD() + c.getIMA() + c.getIMB()));

		} else if (c.getOPT().equals(Constants.SW)
				|| c.getOPT().equals(Constants.LW)) {
			result = constants.optMap.get(c.getOPT()) + " "
					+ constants.regMap.get(c.getRT()) + ", "
					+ getImmediate(c, true) + "("
					+ constants.regMap.get(c.getRS()) + ")";

		} else if (c.getOPT().equals(Constants.J)) {
			result = constants.optMap.get(c.getOPT()) + " #"
					+ getInstr_index(c);

		} else if (c.getOPT().equals(Constants.BEQ)) {
			result = constants.optMap.get(c.getOPT()) + " "
					+ constants.regMap.get(c.getRS()) + ", "
					+ constants.regMap.get(c.getRT()) + ", #" + getOffset(c);

		} else if ((c.getOPT() + c.getRT()).equals(Constants.BGEZ_RT)
				|| (c.getOPT() + c.getRT()).equals(Constants.BGTZ_RT)
				|| (c.getOPT() + c.getRT()).equals(Constants.BLEZ_RT)
				|| (c.getOPT() + c.getRT()).equals(Constants.BLTZ_RT)) {
			result = constants.optMap.get(c.getOPT() + c.getRT()) + " "
					+ constants.regMap.get(c.getRS()) + ", #" + getOffset(c);

		} else if (c.getOPT().equals(Constants.BNE)) {
			result = constants.optMap.get(c.getOPT()) + " "
					+ constants.regMap.get(c.getRS()) + ", "
					+ constants.regMap.get(c.getRT()) + ", #" + getOffset(c);

		} else if (c.getOPT().equals(Constants.ADDI)
				|| c.getOPT().equals(Constants.ADDIU)
				|| c.getOPT().equals(Constants.SLTI)) {
			result = constants.optMap.get(c.getOPT()) + " "
					+ constants.regMap.get(c.getRT()) + ", "
					+ constants.regMap.get(c.getRS()) + ", #"
					+ getImmediate(c, true);

		} else if ((c.getOPT() + c.getFunction()).equals(Constants.BREAK_FUNC)) {
			result = constants.optMap.get(c.getOPT() + c.getFunction());

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
			result = constants.optMap.get(c.getOPT() + c.getFunction()) + " "
					+ constants.regMap.get(c.getRD()) + ", "
					+ constants.regMap.get(c.getRS()) + ", "
					+ constants.regMap.get(c.getRT());

		} else if ((c.getOPT() + c.getFunction()).equals(Constants.SLL_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SRL_FUNC)
				|| (c.getOPT() + c.getFunction()).equals(Constants.SRA_FUNC)) {
			result = constants.optMap.get(c.getOPT() + c.getFunction()) + " "
					+ constants.regMap.get(c.getRD()) + ", "
					+ constants.regMap.get(c.getRT()) + ", "
					+ constants.regMap.get(c.getSA());
		}

		return result;
	}

	public void saveDisassembler(String outputFile) {
		try {
			FileWriter fstream = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			boolean breakFound = false;

			for (int i=0;i<commands.size();){
				
				if(!breakFound){
					out.write(commands.get(i).toString());
					
					if(commands.get(i).getAssembly().equalsIgnoreCase("BREAK")){
						breakFound = true;
					}
				}
				else{
					out.write(commands.get(i).toValue());
				}
				
				i++;
				if(i!=commands.size()){
					out.newLine();
				}
			}

			out.close();
			fstream.close();

			System.out.println("Wrote disassembler : " + outputFile);

		} catch (Exception e) {
			System.out.println("Failed to write disassembler!");
		}
	}
	
	public Command getCommand(String address){
		Command result = null;
		
		for(Command c: commands){
			if(c.getAddress().equalsIgnoreCase(address)){
				result = c;
				break;
			}
		}
		
		return result;
	}
}