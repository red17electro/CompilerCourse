package mips.printer;

import mips.ast.MipsElement;

public class SourceInfo {

	private String comment = null;
	private int minijavaLineNr = 0;
	
	public static SourceInfo get(MipsElement e) {
		return new SourceInfo();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getMinijavaLineNr() {
		return minijavaLineNr;
	}

	public void setMinijavaLineNr(int minijavaLineNr) {
		this.minijavaLineNr = minijavaLineNr;
	}
}
