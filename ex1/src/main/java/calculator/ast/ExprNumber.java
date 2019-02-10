package calculator.ast;

/*
 *This class inherits Expr.
 *Class is used to store and get the numerical value of the expression.
 */
public class ExprNumber extends Expr {

    /*store the numerical value of the expression*/
    private int value;

    /**
     * create a new ExprNumber object, storing its value
     * @param value the number to be stored
     */
    public ExprNumber(int value) {
        super();
        this.value = value;
    }

    /**
     * create a new ExprNumber object, storing its value
     * @param value string representation of the number to be stored
     */
    public ExprNumber(String value) {
        this.value = Integer.parseInt(value);
    }

    /*
    @return int get the value as a integer
     */
    public int getValue() {
        return value;
    }

    /**
     * The accept method calls a visit method of the visitor
     * @param visitor object from the class ExprVisitor
     */
    @Override
    public <V> V accept(ExprVisitor <V> visitor) {
        return visitor.visit(this);
    }
}