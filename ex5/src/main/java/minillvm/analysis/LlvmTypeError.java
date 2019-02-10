package minillvm.analysis;

import frontend.SourcePosition;

public class LlvmTypeError extends RuntimeException {
	private SourcePosition source;

	public LlvmTypeError(SourcePosition source, String message) {
		super(message);
		this.source = source;
	}


	public int getLine() {
		return source.getLine();
	}

	public int getColumn() {
		return source.getColumn();
	}

	@Override
	public String toString() {
		return "Error in line " + getLine() + ":" + getColumn() + ": " + getMessage();
	}



}
