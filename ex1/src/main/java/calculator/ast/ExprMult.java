package calculator.ast;

/**
 * Created by mweber on 04/04/2017.
 */

/*this class  inherits ExprBinarry, and handles all the binary operator multiplication*/
public class ExprMult extends ExprBinary {

    /**
     * @param left  left part of the multiplication expression
     * @param right right part of the multiplication expression
     */

    public ExprMult(Expr left, Expr right) {
        super(left, right);
    }

    /**
     * The accept method calls a visit method of the visitor
     *
     * @param visitor object from the class ExprVisitor
     */
    @Override
    public <V> V accept(ExprVisitor <V> visitor) {
        return visitor.visit(this);
    }
}
