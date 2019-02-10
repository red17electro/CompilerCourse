package frontend;

import minijava.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Server on 5/13/2017.
 */
public class InvalidStatementsChecker extends MJElement.DefaultVisitor {
    /**
     * field of SyntaxErrors, which comes from MJFrontEnd
     */
    private List<SyntaxError> syntaxErrors;

    /**
     * @param syntaxErrors initialize the syntaxErrors using the constructor
     */
    InvalidStatementsChecker(List<SyntaxError> syntaxErrors) {
        this.syntaxErrors = syntaxErrors;
    }

    /**
     * This method overrides the method in the MJElement.DefaultVisitor and checks whether the MJStmtExpr object is valid
     *
     * @param stmtExpr Object from the class MJStmtExpr
     */
    @Override
    public void visit(MJStmtExpr stmtExpr) {
        MJExpr expr = stmtExpr.getExpr();
        if (!(expr instanceof MJMethodCall || expr instanceof MJNewObject)) {
            this.syntaxErrors.add(new SyntaxError(expr, "Wrong kind of expression is presented!"));
        }
    }

    /**
     * This method overrides the method in the MJElement.DefaultVisitor and checks whether the MJStmtAssign object is valid
     *
     * @param stmtAssign Object from the class MJStmtAssign
     */
    @Override
    public void visit(MJStmtAssign stmtAssign) {
        MJExpr exprLeft = stmtAssign.getLeft();
        // ~ info from https://www.cs.purdue.edu/homes/hosking/502/project/html/Absyn/MiniJava.Exp.Assign.html#left
        if (!(exprLeft instanceof MJVarUse || exprLeft instanceof MJArrayLookup || exprLeft instanceof MJFieldAccess)) {
            this.syntaxErrors.add(new SyntaxError(exprLeft, "Wrong type of the left operand: impossible to assign the value on the right hand side!"));
        }
    }
}