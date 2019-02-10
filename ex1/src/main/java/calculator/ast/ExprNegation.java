package calculator.ast;

/**
 * Created by Server on 5/1/2017.
 */

/*this class inherits Expr, and handles the unary operators*/
public class ExprNegation extends Expr {

    /*A property which stores the expression*/
    private Expr e;

    /*
    Initialize the object of the ExprNegation class
    @param e expression which attached to the unary operator
    */
    public ExprNegation(Expr e) {
        this.e = e;
    }

    /**
     * @return expression
     */
    public Expr getExpression() {
        return e;
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
