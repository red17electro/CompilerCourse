package calculator.ast;
/* this class handles all the binary operation in our grammar, and it extends Expr class

 */

/*this class  inherits Expr, and handles all the binary operators, e.g. (+,-,*,/)*/
public abstract class ExprBinary extends Expr {

    /*A property which stores the left expression*/
    private Expr left;
    /*A property which stores the right expression*/
    private Expr right;

    /*
    Initialize the object of the ExprBinary class
    @param left left part of the addition expression
    @param right right part of the addition expression
    */
    public ExprBinary(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @return left expression
     */
    public Expr getLeft() {
        return left;
    }

    /**
     * @return right expression
     */
    public Expr getRight() {
        return right;
    }
}
