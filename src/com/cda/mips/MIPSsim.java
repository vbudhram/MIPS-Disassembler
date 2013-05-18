package com.cda.mips;

import java.io.File;

import com.cd.mips.simulator.Simulator;
import com.cda.mips.disassembler.Constants;
import com.cda.mips.disassembler.Disassembler;

public class MIPSsim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			if (args.length < 3) {
				System.out.println("Not enough args passed");
				System.out.println("arg[0] =  inputfilename");
				System.out.println("arg[1] =  outputfilename");
				System.out.println("arg[2] =  sim or dis");
				System.out.println("arg[3] =  (optional) -Tm:n , where m=start cycle, n=end cycle");
				return;
			}

			String inputFileName = args[0];
			String outputFileName = args[1];
			String operation = args[2];
			String cycle = args[3];
			
			if(operation.equalsIgnoreCase(Constants.dis)){
				File inputFile = new File(inputFileName);
				
				if (!inputFile.exists()) {
					System.out
							.println("Invalid file name passed: " + inputFileName);
					return;
				}
				
				System.out.println("Disassembling..."+inputFile.getAbsoluteFile());

				Disassembler disassembler = new Disassembler(inputFileName);
				
				disassembler.saveDisassembler(outputFileName);
				
			}else if(operation.equalsIgnoreCase(Constants.sim)){
				Simulator sim = new Simulator(new Disassembler(inputFileName), cycle);
				sim.save(outputFileName);
			}else{
				System.out.println("Operation '"+operation+"' ,not yet supported");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
