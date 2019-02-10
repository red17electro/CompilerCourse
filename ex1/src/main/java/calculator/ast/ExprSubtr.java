package calculator.ast;

/**
 * Created by Server on 4/25/2017.
 */

/*this class  inherits ExprBinarry, and handles all the binary operator subtraction*/
public class ExprSubtr extends ExprBinary {
    public ExprSubtr(Expr left, Expr right) {
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