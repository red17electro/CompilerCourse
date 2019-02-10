package frontend;

import minijava.ast.*;

public class StatementChecker extends MJElement.DefaultVisitor {


    private MJFrontend frontend;

    public StatementChecker(MJFrontend mjFrontend) {
        this.frontend = mjFrontend;
    }

    @Override
    public void visit(MJStmtAssign assignment) {
        super.visit(assignment);
        MJExpr left = assignment.getLeft();
        if (!(left instanceof MJVarUse
                || left instanceof MJArrayLookup
                || left instanceof MJFieldAccess)) {
            frontend.getSyntaxErrors().add(new SyntaxError(left, "Only assignments to variables, fields and arrays are allowed."));
        }
    }

    @Override
    public void visit(MJStmtExpr s) {
        super.visit(s);
        MJExpr e = s.getExpr();
        if (!(e instanceof MJMethodCall
                || e instanceof MJNewObject)) {
            frontend.getSyntaxErrors().add(new SyntaxError(e, "The expression " + e + " cannot appear as a statement."));
        }
    }
}
