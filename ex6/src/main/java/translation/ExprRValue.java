package translation;

import minijava.ast.*;
import minillvm.ast.*;

public class ExprRValue implements MJExpr.Matcher<Operand> {
    private Translator tr;

    public ExprRValue(Translator translator) {
        this.tr = translator;
    }

    @Override
    public Operand case_ExprUnary(MJExprUnary e) {
        Operand expr = tr.exprRvalue(e.getExpr());

        return e.getUnaryOperator().match(new MJUnaryOperator.Matcher<Operand>() {

            @Override
            public Operand case_UnaryMinus(MJUnaryMinus unaryMinus) {
                TemporaryVar v = Ast.TemporaryVar("minus_res");
                tr.addInstruction(Ast.BinaryOperation(v, Ast.ConstInt(0),  Ast.Sub(), expr));
                return Ast.VarRef(v);
            }

            @Override
            public Operand case_Negate(MJNegate negate) {
                TemporaryVar v = Ast.TemporaryVar("neg_res");
                tr.addInstruction(Ast.BinaryOperation(v, Ast.ConstBool(false),  Ast.Eq(), expr));
                return Ast.VarRef(v);
            }
        });
    }

    @Override
    public Operand case_FieldAccess(MJFieldAccess e) {
        Operand l = tr.exprLvalue(e);
        TemporaryVar v = Ast.TemporaryVar(e.getFieldName());
        tr.addInstruction(Ast.Load(v, l));
        return Ast.VarRef(v);
    }

    @Override
    public Operand case_ArrayLength(MJArrayLength e) {
        Operand a = tr.exprRvalue(e.getArrayExpr());
        tr.addNullcheck(a, "Nullpointer exception when reading array length in line " + tr.sourceLine(e));
        return tr.getArrayLen(a);
    }



    @Override
    public Operand case_ExprThis(MJExprThis e) {
        return Ast.VarRef(tr.getThisParameter());
    }

    @Override
    public Operand case_ExprBinary(MJExprBinary e) {
        Operand left = tr.exprRvalue(e.getLeft());
        return e.getOperator().match(new MJOperator.Matcher<Operand>() {
            @Override
            public Operand case_And(MJAnd and) {
                BasicBlock andRight = tr.newBasicBlock("and_first_true");
                BasicBlock andEnd = tr.newBasicBlock("and_end");
                TemporaryVar andResVar = Ast.TemporaryVar("andResVar");
                tr.getCurrentBlock().add(Ast.Alloca(andResVar, Ast.TypeBool()));
                tr.getCurrentBlock().add(Ast.Store(Ast.VarRef(andResVar), left));
                tr.getCurrentBlock().add(Ast.Branch(left.copy(), andRight, andEnd));

                tr.addBasicBlock(andRight);
                tr.setCurrentBlock(andRight);
                Operand right = tr.exprRvalue(e.getRight());
                tr.getCurrentBlock().add(Ast.Store(Ast.VarRef(andResVar), right));
                tr.getCurrentBlock().add(Ast.Jump(andEnd));

                tr.addBasicBlock(andEnd);
                tr.setCurrentBlock(andEnd);
                TemporaryVar andRes = Ast.TemporaryVar("andRes");
                andEnd.add(Ast.Load(andRes, Ast.VarRef(andResVar)));
                return Ast.VarRef(andRes);
            }


            private Operand normalCase(Operator op) {
                Operand right = tr.exprRvalue(e.getRight());
                TemporaryVar result = Ast.TemporaryVar("res" + op.getClass().getSimpleName());
                tr.addInstruction(Ast.BinaryOperation(result, left, op, right));
                return Ast.VarRef(result);
            }

            @Override
            public Operand case_Times(MJTimes times) {
                return normalCase(Ast.Mul());
            }


            @Override
            public Operand case_Div(MJDiv div) {
                Operand right = tr.exprRvalue(e.getRight());
                TemporaryVar divResVar = Ast.TemporaryVar("divResVar");
                tr.addInstruction(Ast.Alloca(divResVar, Ast.TypeInt()));
                TemporaryVar isZero = Ast.TemporaryVar("isZero");
                tr.addInstruction(Ast.BinaryOperation(isZero, right, Ast.Eq(), Ast.ConstInt(0)));
                BasicBlock ifZero = tr.newBasicBlock("ifZero");
                BasicBlock notZero = tr.newBasicBlock("notZero");

                tr.addInstruction(Ast.Branch(Ast.VarRef(isZero), ifZero, notZero));

                tr.addBasicBlock(ifZero);
                ifZero.add(Ast.HaltWithError("Division by zero in line " + tr.sourceLine(e)));


                tr.addBasicBlock(notZero);
                tr.setCurrentBlock(notZero);

                BasicBlock div_end = tr.newBasicBlock("div_end");
                BasicBlock div_noOverflow = tr.newBasicBlock("div_noOverflow");

                TemporaryVar isMinusOne = Ast.TemporaryVar("isMinusOne");
                tr.addInstruction(Ast.BinaryOperation(isMinusOne, right.copy(), Ast.Eq(), Ast.ConstInt(-1)));
                TemporaryVar isMinInt = Ast.TemporaryVar("isMinInt");
                tr.addInstruction(Ast.BinaryOperation(isMinInt, left.copy(), Ast.Eq(), Ast.ConstInt(Integer.MIN_VALUE)));
                TemporaryVar isOverflow = Ast.TemporaryVar("isOverflow");
                tr.addInstruction(Ast.BinaryOperation(isOverflow, Ast.VarRef(isMinInt), Ast.And(), Ast.VarRef(isMinusOne)));
                tr.addInstruction(Ast.Store(Ast.VarRef(divResVar), Ast.ConstInt(Integer.MIN_VALUE)));
                tr.addInstruction(Ast.Branch(Ast.VarRef(isOverflow), div_end, div_noOverflow));


                tr.addBasicBlock(div_noOverflow);
                tr.setCurrentBlock(div_noOverflow);
                TemporaryVar divResultA = Ast.TemporaryVar("divResultA");
                tr.addInstruction(Ast.BinaryOperation(divResultA, left, Ast.Sdiv(), right.copy()));
                tr.addInstruction(Ast.Store(Ast.VarRef(divResVar), Ast.VarRef(divResultA)));
                tr.addInstruction(Ast.Jump(div_end));


                tr.addBasicBlock(div_end);
                tr.setCurrentBlock(div_end);
                TemporaryVar divResultB = Ast.TemporaryVar("divResultB");
                tr.addInstruction(Ast.Load(divResultB, Ast.VarRef(divResVar)));
                return Ast.VarRef(divResultB);
            }

            @Override
            public Operand case_Plus(MJPlus plus) {
                return normalCase(Ast.Add());
            }

            @Override
            public Operand case_Minus(MJMinus minus) {
                return normalCase(Ast.Sub());
            }

            @Override
            public Operand case_Equals(MJEquals equals) {
                Operator op = Ast.Eq();
                Operand right = tr.exprRvalue(e.getRight());
                TemporaryVar result = Ast.TemporaryVar("res" + op.getClass().getSimpleName());
                right = tr.addCastIfNecessary(right, left.calculateType());
                tr.addInstruction(Ast.BinaryOperation(result, left, op, right));
                return Ast.VarRef(result);
            }

            @Override
            public Operand case_Less(MJLess less) {
                return normalCase(Ast.Slt());
            }
        });
    }

    @Override
    public Operand case_ArrayLookup(MJArrayLookup e) {
        Operand addr = tr.exprLvalue(e);
        TemporaryVar result = Ast.TemporaryVar("arrayLookupResult");
        tr.addInstruction(Ast.Load(result, addr));
        return Ast.VarRef(result);
    }


    @Override
    public Operand case_ExprNull(MJExprNull e) {
        return Ast.Nullpointer();
    }

    @Override
    public Operand case_Number(MJNumber e) {
        return Ast.ConstInt(e.getIntValue());
    }

    @Override
    public Operand case_NewIntArray(MJNewIntArray e) {
        Operand arraySize = tr.exprRvalue(e.getArraySize());
        TemporaryVar res = Ast.TemporaryVar("newArray");
        tr.addInstruction(Ast.Call(res, tr.getNewIntArrayFunc(), Ast.OperandList(arraySize)));
        return Ast.VarRef(res);
    }

    private TypePointer voidPointer() {
        return Ast.TypePointer(Ast.TypeByte());
    }

    @Override
    public Operand case_VarUse(MJVarUse e) {
        Operand addr = tr.exprLvalue(e);
        TemporaryVar res = Ast.TemporaryVar("read_" + e.getVarName());
        tr.addInstruction(Ast.Load(res, addr));
        return Ast.VarRef(res);
    }

    @Override
    public Operand case_MethodCall(MJMethodCall e) {
        Operand receiver = tr.exprRvalue(e.getReceiver());
        tr.addNullcheck(receiver, "Nullpointer exception in line " + e.getSourcePosition().getLine() + " when calling " + e.getMethodName());

        MJMethodDecl mDecl = e.getMethodDeclaration();
        receiver = tr.addCastIfNecessary(receiver, tr.getPointerToClassStruct((MJClassDecl) mDecl.getParent().getParent()));

        OperandList args = Ast.OperandList(receiver);
        for (int i = 0; i < e.getArguments().size(); i++) {
            Operand arg = tr.exprRvalue(e.getArguments().get(i));
            MJVarDeclList formalParameters = mDecl.getFormalParameters();
            arg = tr.addCastIfNecessary(arg, tr.translateType(formalParameters.get(i).getType()));
            args.add(arg);
        }

        // lookup in vtable
        Operand proc = tr.getClassTranslator().loadProcFromVtable(receiver, e.getMethodDeclaration());
        // do the call
        TemporaryVar result = Ast.TemporaryVar(e.getMethodName() + "_result");
        tr.addInstruction(Ast.Call(result, proc, args));
        return Ast.VarRef(result);
    }

    @Override
    public Operand case_NewObject(MJNewObject e) {
        TemporaryVar res = Ast.TemporaryVar("new_" + e.getClassName());
        tr.addInstruction(Ast.Call(res, tr.getConstructorProcRef(e.getClassDeclaration()), Ast.OperandList()));
        return Ast.VarRef(res);
    }

    @Override
    public Operand case_BoolConst(MJBoolConst e) {
        return Ast.ConstBool(e.getBoolValue());
    }
}
