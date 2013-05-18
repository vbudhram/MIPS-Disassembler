package com.cd.mips.simulator;

public class BTB {
	public int instructionAddr;
	public int branchAddr;
	public String twoBitPredictor; //00,01,10,11
	
	public BTB() {
		instructionAddr = 0;
		branchAddr = 0;
		twoBitPredictor = "11";
	}
	
	public String toString(){
		return "<"+instructionAddr+","+branchAddr+","+twoBitPredictor+">";
	}
}
