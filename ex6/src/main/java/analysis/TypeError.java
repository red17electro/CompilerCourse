package analysis;

import frontend.SourcePosition;
import minijava.ast.MJElement;

public class TypeError extends RuntimeException {
    private SourcePosition source;

    public TypeError(String message, int line, int column) {
        super(message);
        this.source = new SourcePosition("", line, column, line, column);
    }

    public TypeError(MJElement element, String message) {
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
        return "Error in line " + getLine() + ":" + getColumn() + ": " + getMessage();
    }

    public int getLength() {
        if (source.getLine() == source.getEndLine()) {
            return source.getEndColumn() - source.getColumn();
        }
        return 5;
    }

    public SourcePosition getSource() {
        return source;
    }
}