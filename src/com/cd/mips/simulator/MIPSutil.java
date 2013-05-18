package com.cd.mips.simulator;

import com.cda.mips.disassembler.Command;

public class MIPSutil {
	
	public static int getImmediate(Command c, boolean signed) {
		int immediate = 0;
		String RD;

		if (signed) {
			if (c.getRD().charAt(0) == '1') {
				RD = c.getRD().substring(1, c.getRD().length());
				Double immediateD = Integer.valueOf(
						RD + c.getIMA() + c.getIMB(), 2)
						- (Math.pow(2, 15));
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

	public static Integer getValue(Command c, boolean signed) {
		Integer value = 0;
		String sValue = c.getOPT() + c.getRS() + c.getRT() + c.getRD()
				+ c.getIMA() + c.getIMB();

		if (signed && sValue.charAt(0) == '1') {
			sValue = c.getRD().substring(1, c.getRD().length());
			Double valueDouble = Integer.valueOf(sValue, 2)
					- (Math.pow(2, 31) - 1);

			value = valueDouble.intValue();
		} else {
			value = Integer.valueOf(sValue, 2);
		}

		return value;
	}

	public static int getInstr_index(Command c) {
		int instr_index = 0;

		instr_index = Integer.valueOf(
				c.getRS() + c.getRT() + c.getRD() + c.getIMA() + c.getIMB(), 2) * 4;

		return instr_index;
	}

	public static int getOffset(Command c) {
		int offset = 0;

		offset = Integer.valueOf(c.getRD() + c.getIMA() + c.getIMB(), 2) * 4;

		return offset;
	}
	
	public static int getRegisterIndex(String register){
		int index = -1;
		
		register = register.replace("R", "");
		
		index = Integer.parseInt(register);
		
		return index;
	}

}
