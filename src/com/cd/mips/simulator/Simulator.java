package com.cd.mips.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.cda.mips.disassembler.Disassembler;

public class Simulator {

	private Disassembler disassembler;
	private MIPSProcessor mipsProcessor;
	private int startCycle = 0;
	private int endCycle = 0;
	private String state = "";

	public Simulator(Disassembler d, String cycle) {
		disassembler = d;

		// Parse cycle string to get start and end cycles
		if (!cycle.equalsIgnoreCase("")) {
			cycle = cycle.replace("-T", "");
			String[] tokens = cycle.split(":");
			startCycle = Integer.parseInt(tokens[0].replace(":", "")) - 1;
			endCycle = Integer.parseInt(tokens[1]) - 1;
		}

		mipsProcessor = new MIPSProcessor(d);

		//MIP Simulate function
		simulate(startCycle, endCycle);
	}

	public void simulate(int startCycle, int endCycle) {

		//Set the max number of cycles to simulate, if none set
		int MAX_CYCLES = 999999;
		if (endCycle < 0) {
			endCycle = MAX_CYCLES;
		}
		
		//Always print first cycle
		if (startCycle < 0) {
			System.out.println(mipsProcessor.printProcessorState() + "\n");
		}
		
		//Simulate cycles up to start cycle
		for (int i = 1; i < startCycle; i++) {
			mipsProcessor.nextCycle();
		}

		//Simulate cyles up to end cycle, print processor state
		for (int i = startCycle; true; i++) {

			if (!mipsProcessor.nextCycle()) {
				break;
			}

			if (i > endCycle) {
				break;
			}

			state = state + mipsProcessor.printProcessorState();
			System.out.println(mipsProcessor.printProcessorState() + "\n");
		}
	}

	public void save(String outputFile) {
		try {
			FileWriter fstream = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write(state);

			out.close();
			fstream.close();

			System.out.println("Wrote Simulator : " + outputFile);

		} catch (Exception e) {
			System.out.println("Failed to write Simulator!");
		}
	}
}
