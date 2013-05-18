package com.cda.mips.disassembler;

public class Command {
	private String OPT;
	private String RS;
	private String RT;
	private String RD;
	private String IMA;
	private String IMB;
	private String address;
	private String assembly;

	public Command() {

	}

	public String getRS() {
		return RS;
	}

	public void setRS(String rS) {
		RS = rS;
	}

	public String getRT() {
		return RT;
	}

	public void setRT(String rT) {
		RT = rT;
	}

	public String getRD() {
		return RD;
	}

	public void setRD(String rD) {
		RD = rD;
	}

	public String getOPT() {
		return OPT;
	}

	public void setOPT(String oPT) {
		OPT = oPT;
	}

	public String getIMA() {
		return IMA;
	}

	public void setIMA(String iMA) {
		IMA = iMA;
	}

	public String getIMB() {
		return IMB;
	}

	public void setIMB(String iMB) {
		IMB = iMB;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFunction() {
		return IMB;
	}

	public String getSA() {
		return IMA;
	}

	public String toString() {
		return this.getOPT() + " " + this.getRS() + " " + this.getRT() + " "
				+ this.getRD() + " " + this.getIMA() + " " + this.getIMB()
				+ " " + this.getAddress() + " " + this.getAssembly();
	}

	public String getAssembly() {
		return assembly;
	}

	public void setAssembly(String assembly) {
		this.assembly = assembly;
	}

	public String toValue() {
		return this.getOPT() + this.getRS() + this.getRT() + this.getRD()
				+ this.getIMA() + this.getIMB() + " " + this.getAddress() +" "+this.getAssembly();
	}
	
	public String toAddrAssembly(){
		return this.getAddress() +" "+this.getAssembly();
	}
}
