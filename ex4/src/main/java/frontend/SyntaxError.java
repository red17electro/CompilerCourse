package frontend;

import minijava.ast.MJElement;

public class SyntaxError extends RuntimeException {
    private SourcePosition source;

    public SyntaxError(String message, int line, int column) {
        super(message);
        this.source = new SourcePosition("", line, column, line, column);
    }

    public SyntaxError(MJElement element, String message) {
        super(message);
        while (element != null) {
            this.source = element.getSourcePosition();
            if (this.source != null) {
                break;
            }
            element = element.getParent();
        }
    }

    public int getLine() {
        return source.getLine();
    }

    public int getColumn() {
        return source.getColumn();
    }

    @Override
    public String toString() {
        return "Syntax error in line " + getLine() + ":" + getColumn() + ": " + getMessage();
    }
}