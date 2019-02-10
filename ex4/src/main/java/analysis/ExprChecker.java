package analysis;

import analysis.TypeContext.VarRef;
import minijava.ast.*;

public class ExprChecker implements MJExpr.Matcher<Type> {
    private final Analysis analysis;
    private final TypeContext ctxt;

    public ExprChecker(Analysis analysis, TypeContext ctxt) {
        this.analysis = analysis;
        this.ctxt = ctxt;
    }

    Type check(MJExpr e) {
        return e.match(this);
    }

    void expect(MJExpr e, Type expected) {
        Type actual = check(e);
        if (!actual.isSubtypeOf(expected)) {
            analysis.addError(e, "Expected expression of type " + expected + " but found " + actual + ".");
        }
    }

    @Override
    public Type case_ExprUnary(MJExprUnary exprUnary) {
        Type t = check(exprUnary.getExpr());
        return exprUnary.getUnaryOperator().match(new MJUnaryOperator.Matcher<Type>() {

            @Override
            public Type case_UnaryMinus(MJUnaryMinus unaryMinus) {
                expect(exprUnary.getExpr(), Type.INT);
                return Type.INT;
            }

            @Override
            public Type case_Negate(MJNegate negate) {
                expect(exprUnary.getExpr(), Type.BOOL);
                return Type.BOOL;
            }
        });
    }

    @Override
    public Type case_FieldAccess(MJFieldAccess fieldAccess) {
        Type rt = check(fieldAccess.getReceiver());
        MJVarDecl v = analysis.lookupField(rt, fieldAccess.getFieldName());
        if (v == null) {
            analysis.addError(fieldAccess, "Type " + rt + " has no field named " + fieldAccess.getFieldName() + ".");
            return Type.ANY;
        }
        fieldAccess.setVariableDeclaration(v);
        return analysis.type(v.getType());
    }

    @Override
    public Type case_MethodCall(MJMethodCall methodCall) {
        Type rt = check(methodCall.getReceiver());
        MJMethodDecl m = analysis.lookupMethod(rt, methodCall.getMethodName());
        if (m == null) {
            analysis.addError(methodCall, "Type " + rt + " has no method " + methodCall.getMethodName() + ".");
            return Type.ANY;
        }
        MJExprList args = methodCall.getArguments();
        MJVarDeclList params = m.getFormalParameters();
        if (args.size() < params.size()) {
            analysis.addError(methodCall, "Not enough arguments.");
        } else if (args.size() > params.size()) {
            analysis.addError(methodCall, "Too many arguments.");
        } else {
            for (int i = 0; i< params.size(); i++) {
                expect(args.get(i), analysis.type(params.get(i).getType()));
            }
        }
        methodCall.setMethodDeclaration(m);
        return analysis.type(m.getReturnType());
    }


    @Override
    public Type case_ArrayLength(MJArrayLength arrayLength) {
        expect(arrayLength.getArrayExpr(), Type.INTARRAY);
        return Type.INT;
    }

    @Override
    public Type case_ExprThis(MJExprThis exprThis) {
        Type thisType = ctxt.getThisType();
        if (thisType == Type.INVALID) {
            analysis.addError(exprThis, "Cannot use 'this' inside main method.");
        }
        return thisType;
    }

    @Override
    public Type case_ExprBinary(MJExprBinary exprBinary) {
        return exprBinary.getOperator().match(new MJOperator.Matcher<Type>() {
            @Override
            public Type case_And(MJAnd and) {
                expect(exprBinary.getLeft(), Type.BOOL);
                expect(exprBinary.getRight(), Type.BOOL);
                return Type.BOOL;
            }

            @Override
            public Type case_Times(MJTimes times) {
                return case_intOperation();
            }

            @Override
            public Type case_Div(MJDiv div) {
                return case_intOperation();
            }

            @Override
            public Type case_Plus(MJPlus plus) {
                return case_intOperation();
            }

            @Override
            public Type case_Minus(MJMinus minus) {
                return case_intOperation();
            }

            private Type case_intOperation() {
                expect(exprBinary.getLeft(), Type.INT);
                expect(exprBinary.getRight(), Type.INT);
                return Type.INT;
            }

            @Override
            public Type case_Equals(MJEquals equals) {
                Type l = check(exprBinary.getLeft());
                Type r = check(exprBinary.getRight());
                if (!l.isSubtypeOf(r) && !r.isSubtypeOf(l)) {
                    analysis.addError(exprBinary, "Cannot compare types " + l + " and " + r +".");
                }
                return Type.BOOL;
            }

            @Override
            public Type case_Less(MJLess less) {
                expect(exprBinary.getLeft(), Type.INT);
                expect(exprBinary.getRight(), Type.INT);
                return Type.BOOL;
            }
        });
    }

    @Override
    public Type case_ArrayLookup(MJArrayLookup arrayLookup) {
        expect(arrayLookup.getArrayExpr(), Type.INTARRAY);
        expect(arrayLookup.getArrayIndex(), Type.INT);
        return Type.INT;
    }

    @Override
    public Type case_ExprNull(MJExprNull exprNull) {
        return Type.NULL;
    }

    @Override
    public Type case_Number(MJNumber number) {
        return Type.INT;
    }

    @Override
    public Type case_NewIntArray(MJNewIntArray newIntArray) {
        expect(newIntArray.getArraySize(), Type.INT);
        return Type.INTARRAY;
    }

    @Override
    public Type case_VarUse(MJVarUse varUse) {
        VarRef ref = ctxt.lookupVar(varUse.getVarName());
        if (ref == null) {
            analysis.addError(varUse, "Variable " + varUse.getVarName() + " is not defined.");
            return Type.ANY;
        }
        varUse.setVariableDeclaration(ref.decl);
        return ref.type;
    }



    @Override
    public Type case_NewObject(MJNewObject newObject) {
        ClassType ct = analysis.getClassTable().lookupClass(newObject.getClassName());
        if (ct == null) {
            analysis.addError(newObject, "No class with name " + newObject.getClassName() + " exists.");
            return Type.ANY;
        }
        newObject.setClassDeclaration(ct.getClassDecl());
        return ct;
    }

    @Override
    public Type case_BoolConst(MJBoolConst boolConst) {
        return Type.BOOL;
    }
}
