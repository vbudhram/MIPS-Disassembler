package com.cd.mips.simulator;

import com.cda.mips.disassembler.Command;

public class Stage {
	public Command command = null;
	public Command prevCommand = null;

	public Stage() {
		// TODO Auto-generated constructor stub
	}
	
	public void updateCommand(Command c){
		prevCommand = command;
		command = c;
	}

}
