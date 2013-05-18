package com.cda.mips.disassembler;

import java.util.HashMap;

public class Constants {
	public static final String dis = "dis";
	public static final String sim = "sim";
	
	public static final String SW = "101011";
	public static final String LW = "100011";
	
	public static final String J = "000010";
	public static final String BEQ = "000100";
	public static final String BNE = "000101";
	public static final String BGEZ_RT = "000001"+"00001";
	public static final String BGTZ_RT = "000111"+"00000";
	public static final String BLEZ_RT = "000110"+"00000";
	public static final String BLTZ_RT = "000001"+"00000";
	
	public static final String ADDI = "001000";
	public static final String ADDIU = "001001";
	
	public static final String BREAK_FUNC = "000000"+"001101";
	
	public static final String SLT_FUNC = "000000"+"101010";
	
	public static final String SLTI  = "001010";
	
	public static final String SLTU_FUNC = "000000"+"101011";
	
	public static final String SLL_FUNC = "000000"+"000000";
	public static final String SRL_FUNC = "000000"+"000010";
	public static final String SRA_FUNC = "000000"+"000011";
	
	public static final String SUB_FUNC = "000000"+"100010";
	public static final String SUBU_FUNC = "000000"+"100011";
	public static final String ADD_FUNC = "000000"+"100000";
	public static final String ADDU_FUNC = "000000"+"100001";
	
	public static final String AND = "000000"+"100100";
	public static final String OR = "000000"+"100101";
	public static final String XOR = "000000"+"100110";
	public static final String NOR = "000000"+"100111";
	
	public static final String NOP = "000000"+"00000"+"00000"+"00000"+"00000"+"000000";
	
	public static final String R0 = "00000";
	public static final String R1 = "00001";
	public static final String R2 = "00010";
	public static final String R3 = "00011";
	public static final String R4 = "00100";
	public static final String R5 = "00101";
	public static final String R6 = "00110";
	public static final String R7 = "00111";
	public static final String R8 = "01000";
	public static final String R9 = "01001";
	public static final String R10 = "01010";
	public static final String R11 = "01011";
	public static final String R12 = "01100";
	public static final String R13 = "01101";
	public static final String R14 = "01110";
	public static final String R15 = "01111";
	public static final String R16 = "10000";
	public static final String R17 = "10001";
	public static final String R18 = "10010";
	public static final String R19 = "10011";
	public static final String R20 = "10100";
	public static final String R21 = "10101";
	public static final String R22 = "10110";
	public static final String R23 = "10111";
	public static final String R24 = "11000";
	public static final String R25 = "11001";
	public static final String R26 = "11010";
	public static final String R27 = "11011";
	public static final String R28 = "11100";
	public static final String R29 = "11101";
	public static final String R30 = "11110";
	public static final String R31 = "11111";
	
	public HashMap<String, String> regMap = new HashMap<String,String>();
	public HashMap<String, String> optMap = new HashMap<String,String>();
	
	public Constants(){
		
		optMap.put(SW,"SW");
		optMap.put(LW,"LW");
		
		optMap.put(J,"J");
		optMap.put(BEQ,"BEQ");
		optMap.put(BNE,"BNE");
		optMap.put(BGEZ_RT,"BGEZ");
		optMap.put(BGTZ_RT,"BGTZ");
		optMap.put(BLEZ_RT,"BLEZ");
		optMap.put(BLTZ_RT,"BLTZ");
		
		optMap.put(ADDI,"ADDI");
		optMap.put(ADDIU,"ADDIU");
		optMap.put(ADD_FUNC,"ADD");
		
		optMap.put(BREAK_FUNC,"BREAK");
		
		optMap.put(SLT_FUNC,"SLT");
		optMap.put(SLTI,"SLTI");
		optMap.put(SLTU_FUNC,"SLTU");
		
		optMap.put(SLL_FUNC,"SLL");
		optMap.put(SRL_FUNC,"SRL");
		optMap.put(SRA_FUNC,"SRA");
		
		optMap.put(AND,"AND");
		optMap.put(OR,"OR");
		optMap.put(XOR,"XOR");
		optMap.put(NOR,"NOR");
		
		optMap.put(NOP,"NOP");
		
		regMap.put(R0,"R0");
		regMap.put(R1,"R1");
		regMap.put(R2,"R2");
		regMap.put(R3,"R3");
		regMap.put(R4,"R4");
		regMap.put(R5,"R5");
		regMap.put(R6,"R6");
		regMap.put(R7,"R7");
		regMap.put(R8,"R8");
		regMap.put(R9,"R9");
		regMap.put(R10,"R10");
	}
}
