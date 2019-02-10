package calculator.ast;

/**
 * Created by Server on 4/30/2017.
 */

/**
 * This interface is crucial for implementing the visitor pattern in order to add the
 * functionality of the pretty print function
 *
 */
public interface ExprVisitor <V> {
    /**
     * This method will pretty print the expression for operator addition
     * @param exprAdd object from the class ExprAdd
     */
    V visit(ExprAdd exprAdd);

    /**
     * This method will pretty print the expression for operator subtraction
     * @param exprSubtr object from the class ExprSubtr
     */
    V visit(ExprSubtr exprSubtr);

    /**
     * This method will pretty print the expression for operator Multiplication
     * @param exprMult object from the class ExprMult
     */
    V visit(ExprMult exprMult);

    /**
     * This method will pretty print the numerical value of the expression
     * @param exprNumber object from the class ExprNumber
     */
    V visit(ExprNumber exprNumber);

    /**
     * This method will pretty print the expression for operator Division
     * @param exprDiv object from the class ExprDiv
     */
    V visit(ExprDiv exprDiv);

    /**
     * This method will pretty print the expression for unary operator NEGATION
     * @param exprNegation object from the class ExprNegation
     */
    V visit(ExprNegation exprNegation);

    /**
     * This method will call the accept method of the Expr class
     * @param expr object from the class Expr
     */
    V visit(Expr expr);
}