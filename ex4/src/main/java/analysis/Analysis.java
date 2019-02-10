package analysis;

import minijava.ast.*;

import java.util.*;

public class Analysis extends MJElement.DefaultVisitor {

    private final MJProgram prog;
    private List<TypeError> typeErrors = new ArrayList<>();
    private ClassTable classTable;
    private LinkedList<TypeContext> ctxt = new LinkedList<>();

    public void addError(MJElement element, String message) {
        typeErrors.add(new TypeError(element, message));
    }

    public Analysis(MJProgram prog) {
        this.prog = prog;
    }

    public void check() {
        classTable = new ClassTable(this, prog);

        prog.accept(this);
    }

    @Override
    public void visit(MJMainClass mainClass) {
        TypeContextImpl cctxt = new TypeContextImpl(Type.VOID, Type.INVALID);
        cctxt.putVar(mainClass.getArgsName(), Type.INVALID, null);
        // enter class context
        ctxt.push(cctxt);
        // check children
        super.visit(mainClass);
        // exit class context
        ctxt.pop();
    }

    @Override
    public void visit(MJClassDecl c) {
        // fields are unique:
        Set<String> fieldNames = new HashSet<>();
        for (MJVarDecl f : c.getFields()) {
            if (!fieldNames.add(f.getName())) {
                addError(f, "Field with name " + f.getName() + " already exists.");
            }
        }
        // method names are unique
        Set<String> methodNames = new HashSet<>();
        for (MJMethodDecl m : c.getMethods()) {
            if (!methodNames.add(m.getName())) {
                addError(m, "Method with name " + m.getName() + " already exists.");
            }
        }

        ClassType superClass = classTable.getDirectSuperClassType(c);
        if (superClass != null) {
            // methods are correct overrides
            for (MJMethodDecl m : c.getMethods()) {
                MJMethodDecl sm = classTable.lookupMethod(superClass, m.getName());
                if (sm != null) {
                    checkOverride(m, sm);
                }
            }
        }

        List<MJVarDecl> fields = classTable.getFields(c);

        Type cType = classTable.lookupClass(c.getName());
        // enter class context
        ctxt.push(new TypeContextImpl(this, fields, cType));

        for (MJMethodDecl m : c.getMethods()) {
            m.accept(this);
        }

        // exit class context
        ctxt.pop();

    }

    private void checkOverride(MJMethodDecl m, MJMethodDecl sm) {
        if (m.getFormalParameters().size() != sm.getFormalParameters().size()) {
            addError(m, "Method " + m.getName() + " must have same number of parameters as method in super class.");
            return;
        }
        for (int i=0; i<m.getFormalParameters().size(); i++) {
            Type t1 = type(m.getFormalParameters().get(i).getType());
            Type t2 = type(sm.getFormalParameters().get(i).getType());
            if (!t1.isEqualToType(t2)) {
                addError(m.getFormalParameters().get(i), "Parameter types must be equal for overridden methods.");
            }
        }
        if (!type(m.getReturnType()).isSubtypeOf(type(sm.getReturnType()))) {
            addError(m.getReturnType(), "Return type must be a subtype of overridden method.");
        }
    }

    @Override
    public void visit(MJMethodDecl m) {
        // parameter names are unique, build context
        TypeContext mctxt = this.ctxt.peek().copy();
        Set<String> paramNames = new HashSet<>();
        for (MJVarDecl v : m.getFormalParameters()) {
            if (!paramNames.add(v.getName())) {
                addError(m, "Parameter with name " + v.getName() + " already exists.");
            }
            mctxt.putVar(v.getName(), type(v.getType()), v);
        }
        mctxt.setReturnType(type(m.getReturnType()));
        // enter method context
        ctxt.push(mctxt);

        m.getMethodBody().accept(this);

        // exit method context
        ctxt.pop();
    }



    @Override
    public void visit(MJStmtReturn stmtReturn) {
        Type actualReturn = checkExpr(ctxt.peek(), stmtReturn.getResult());
        Type expectedReturn = ctxt.peek().getReturnType();
        if (!actualReturn.isSubtypeOf(expectedReturn)) {
            addError(stmtReturn, "Should return value of type " + expectedReturn + ", but found " + actualReturn + ".");
        }
    }

    @Override
    public void visit(MJStmtAssign stmtAssign) {
        Type lt = checkExpr(ctxt.peek(), stmtAssign.getLeft());
        Type rt = checkExpr(ctxt.peek(), stmtAssign.getRight());
        if (!rt.isSubtypeOf(lt)) {
            addError(stmtAssign.getRight(), "Cannot assign value of type " + rt + " to " + lt + ".");
        }
    }

    @Override
    public void visit(MJStmtExpr stmtExpr) {
        checkExpr(ctxt.peek(), stmtExpr.getExpr());
    }

    @Override
    public void visit(MJStmtWhile stmtWhile) {
        Type ct = checkExpr(ctxt.peek(), stmtWhile.getCondition());
        if (!ct.isSubtypeOf(Type.BOOL)) {
            addError(stmtWhile.getCondition(), "Condition of while-statement must be of type boolean, but this is of type " + ct + ".");
        }
        super.visit(stmtWhile);
    }

    @Override
    public void visit(MJStmtIf stmtIf) {
        Type ct = checkExpr(ctxt.peek(), stmtIf.getCondition());
        if (!ct.isSubtypeOf(Type.BOOL)) {
            addError(stmtIf.getCondition(), "Condition of if-statement must be of type boolean, but this is of type " + ct + ".");
        }
        super.visit(stmtIf);
    }

    @Override
    public void visit(MJBlock block) {
        TypeContext bctxt = this.ctxt.peek().copy();
        for (MJStatement s : block) {
            // could also be integrated into the visitor run
            if (s instanceof MJVarDecl) {
                MJVarDecl varDecl = (MJVarDecl) s;
                TypeContextImpl.VarRef ref = bctxt.lookupVar(varDecl.getName());
                if (ref != null && !isField(ref.decl)) {
                    addError(varDecl, "A variable with name " + varDecl.getName() + " is already defined.");
                }
                bctxt.putVar(varDecl.getName(), type(varDecl.getType()), varDecl);
            } else {
                // enter block context
                ctxt.push(bctxt);
                s.accept(this);
                // exit block context
                ctxt.pop();
            }
        }
    }

    @Override
    public void visit(MJVarDecl varDecl) {
        throw new RuntimeException(); // var decls already handled by MJBlock and MJMethodDecl
    }

    @Override
    public void visit(MJStmtPrint stmtPrint) {
        Type pt = checkExpr(ctxt.peek(), stmtPrint.getPrinted());
        if (!pt.isSubtypeOf(Type.INT)) {
            addError(stmtPrint.getPrinted(), "Can only print int values, " + pt + " is not allowed.");
        }
    }


    public Type checkExpr(TypeContext ctxt, MJExpr e) {
        return e.match(new ExprChecker(this, ctxt));
    }

    public Type type(MJType type) {
        return type.match(new MJType.Matcher<Type>() {

            @Override
            public Type case_TypeBool(MJTypeBool typeBool) {
                return Type.BOOL;
            }

            @Override
            public Type case_TypeClass(MJTypeClass typeClass) {
                ClassType t = classTable.lookupClass(typeClass.getName());
                if (t == null) {
                    addError(typeClass, "Type " + typeClass.getName() + " not found.");
                    return Type.ANY;
                }
                typeClass.setClassDeclaration(t.getClassDecl());
                return t;
            }

            @Override
            public Type case_TypeInt(MJTypeInt typeInt) {
                return Type.INT;
            }

            @Override
            public Type case_TypeIntArray(MJTypeIntArray typeIntArray) {
                return Type.INTARRAY;
            }
        });
    }

    public MJVarDecl lookupField(Type t, String fieldName) {
        return classTable.lookupField(t, fieldName);
    }

    public boolean isField(MJVarDecl decl) {
        return decl != null
                && decl.getParent() != null
                && decl.getParent().getParent() instanceof MJClassDecl;
    }

    public ClassTable getClassTable() {
        return classTable;
    }

    public MJMethodDecl lookupMethod(Type rt, String methodName) {
        return classTable.lookupMethod(rt, methodName);
    }

    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
