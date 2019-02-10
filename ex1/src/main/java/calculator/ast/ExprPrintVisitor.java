package calculator.ast;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.VoidType;

/**
 * Created by Server on 4/30/2017.
 */

/*
 *This class inherits the ExprVisitor interface.
 * The idea of the class is to put the parenthesis around the expressions accordingly
 */
public class ExprPrintVisitor implements ExprVisitor<String> {
    /**
     * This method will pretty print the expression for operator addition
     *
     * @param exprAdd object from the class ExprAdd
     */
    public String visit(ExprAdd exprAdd) {
        return "(" + exprAdd.getLeft().accept(this) + " + " + exprAdd.getRight().accept(this) + ")";
    }

    /**
     * This method will pretty print the expression for operator subtraction
     *
     * @param exprSubtr object from the class ExprSubtr
     */
    public String visit(ExprSubtr exprSubtr) {
        return "(" + exprSubtr.getLeft().accept(this) + " - " + exprSubtr.getRight().accept(this) + ")";
    }

    /**
     * This method will pretty print the expression for operator Multiplication
     *
     * @param exprMult object from the class ExprMult
     */
    public String visit(ExprMult exprMult) {
        return "(" + exprMult.getLeft().accept(this) + " * " + exprMult.getRight().accept(this) + ")";
    }

    /**
     * This method will pretty print the numerical value of the expression
     *
     * @param exprNumber object from the class ExprNumber
     */
    public String visit(ExprNumber exprNumber) {
        return "" + exprNumber.getValue();
    }

    /**
     * This method will call the accept method of the Expr class
     *
     * @param expr object from the class Expr
     */
    public String visit(Expr expr) {
        return expr.accept(this);
    }

    /**
     * This method will pretty print the expression for operator Division
     *
     * @param exprDiv object from the class ExprDiv
     */
    public String visit(ExprDiv exprDiv) {
        return "(" + exprDiv.getLeft().accept(this) + " / " + exprDiv.getRight().accept(this) + ")";
    }

    /**
     * This method will pretty print the expression for unary operator NEGATION
     *
     * @param exprNegation object from the class ExprNegation
     */
    public String visit(ExprNegation exprNegation) {
        return "(-" + exprNegation.getExpression().accept(this) + ")";
    }
}
