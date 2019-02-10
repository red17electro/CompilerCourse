package calculator.ast;

/**
 * Created by Server on 4/30/2017.
 */
/*
 *This class inherits the ExprEvalVisitor interface.
 * The idea of the class is to recursively compute the value of the given expression
 */
public class ExprEvaluateVisitor implements ExprVisitor<Integer> {
    /**
     * @param exprAdd the expression of addition
     * @return int the value, which is the result of the operation addition
     */
    public Integer visit(ExprAdd exprAdd) {
        return exprAdd.getLeft().accept(this) + exprAdd.getRight().accept(this);
    }

    /**
     * @param exprSubtr the expression of subtraction
     * @return int the value, which is the result of the operation subtraction
     */
    public Integer visit(ExprSubtr exprSubtr) {
        return exprSubtr.getLeft().accept(this) - exprSubtr.getRight().accept(this);
    }

    /**
     * @param exprMult the expression of multiplication
     * @return int the value, which is the result of the operation multiplication
     */
    public Integer visit(ExprMult exprMult) {
        return exprMult.getLeft().accept(this) * exprMult.getRight().accept(this);
    }

    /**
     * @param exprNumber the expression, which consists only the number (some value)
     * @return int the value itself
     */
    public Integer visit(ExprNumber exprNumber) {
        return exprNumber.getValue();
    }

    /**
     * @param exprDiv the expression of division
     * @return int the value, which is the result of the operation division
     */
    public Integer visit(ExprDiv exprDiv) {
        return exprDiv.getLeft().accept(this) / exprDiv.getRight().accept(this);
    }

    /**
     * @param exprNegation the negation operation
     * @return int the value, which is the result of the operation negation
     */
    public Integer visit(ExprNegation exprNegation) {
        ExprMult operation = new ExprMult(exprNegation.getExpression(), new ExprNumber(-1));
        return operation.accept(this);
    }

    /**
     * This method will call the accept method of the Expr class
     *
     * @param expr object from the class Expr
     */
    public Integer visit(Expr expr) {
        return expr.accept(this);
    }
}
