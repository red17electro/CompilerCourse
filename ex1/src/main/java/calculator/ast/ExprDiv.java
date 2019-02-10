package calculator.ast;

/**
 * Created by Server on 4/30/2017.
 */

/*this class  inherits ExprBinarry, and handles all the binary operator division*/
public class ExprDiv extends ExprBinary {
    /*
    Initialize the object of the ExprDiv class
    @param left left part of the division expression
    @param right right part of the division expression
    */
    public ExprDiv(Expr left, Expr right) {
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