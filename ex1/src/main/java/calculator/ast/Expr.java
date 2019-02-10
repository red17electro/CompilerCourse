package calculator.ast;

/**
 * This class provides an abstract class to all
 * the expressions. The expressions will be the child classes of Expr.
 */

public abstract class Expr {
    /**
     * The accept method calls a visit method of the visitor
     *
     * @param visitor object from the class ExprVisitor
     */
    <V> V accept(ExprVisitor <V> visitor) {
        return visitor.visit(this);
    }
}