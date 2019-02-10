package translation;

import minijava.ast.*;
import minillvm.ast.Ast;
import minillvm.ast.BasicBlock;
import minillvm.ast.Operand;
import minillvm.ast.TemporaryVar;

public class ExprLValue implements MJExpr.Matcher<Operand> {
    private Translator tr;

    public ExprLValue(Translator translator) {

        this.tr = translator;
    }

    @Override
    public Operand case_ArrayLookup(MJArrayLookup e) {
        Operand arrayAddr = tr.exprRvalue(e.getArrayExpr());
        tr.addNullcheck(arrayAddr, "Nullpointer exception in line " + tr.sourceLine(e));

        Operand index = tr.exprRvalue(e.getArrayIndex());

        Operand len = tr.getArrayLen(arrayAddr);
        TemporaryVar smallerZero = Ast.TemporaryVar( "smallerZero");
        TemporaryVar lenMinusOne = Ast.TemporaryVar("lenMinusOne");
        TemporaryVar greaterEqualLen = Ast.TemporaryVar("greaterEqualLen");
        TemporaryVar outOfBoundsV = Ast.TemporaryVar("outOfBounds");
        BasicBlock outOfBounds = tr.newBasicBlock("outOfBounds");
        BasicBlock indexInRange = tr.newBasicBlock("indexInRange");


        // smallerZero = index < 0
        tr.addInstruction(Ast.BinaryOperation(smallerZero, index, Ast.Slt(), Ast.ConstInt(0)));
        // lenMinusOne = length - 1
        tr.addInstruction(Ast.BinaryOperation(lenMinusOne, len, Ast.Sub(), Ast.ConstInt(1)));
        // greaterEqualLen = lenMinusOne < index
        tr.addInstruction(Ast.BinaryOperation(greaterEqualLen, Ast.VarRef(lenMinusOne), Ast.Slt(), index.copy()));
        // outOfBoundsV = smallerZero || greaterEqualLen
        tr.addInstruction(Ast.BinaryOperation(outOfBoundsV, Ast.VarRef(smallerZero), Ast.Or(), Ast.VarRef(greaterEqualLen)));

        tr.getCurrentBlock().add(Ast.Branch(Ast.VarRef(outOfBoundsV), outOfBounds, indexInRange));

        tr.addBasicBlock(outOfBounds);
        outOfBounds.add(Ast.HaltWithError("Index out of bounds error in line " + tr.sourceLine(e)));

        tr.addBasicBlock(indexInRange);
        tr.setCurrentBlock(indexInRange);
        TemporaryVar indexAddr = Ast.TemporaryVar("indexAddr");
        tr.addInstruction(Ast.GetElementPtr(indexAddr, arrayAddr, Ast.OperandList(
                Ast.ConstInt(0),
                Ast.ConstInt(1),
                index.copy()
        )));
        return Ast.VarRef(indexAddr);
    }

    @Override
    public Operand case_FieldAccess(MJFieldAccess e) {
        Operand receiverAddr = tr.exprRvalue(e.getReceiver());
        return tr.getClassTranslator().getFieldAddress(receiverAddr, e.getVariableDeclaration());
    }

    @Override
    public Operand case_VarUse(MJVarUse e) {
        MJVarDecl varDecl = e.getVariableDeclaration();
        if (tr.isField(varDecl)) {
            // field
            Operand thisVal = Ast.VarRef(tr.getThisParameter());
            return tr.getClassTranslator().getFieldAddress(thisVal, varDecl);
        } else {
            // local TemporaryVar
            return Ast.VarRef(tr.getLocalVarLocation(varDecl));
        }
    }



    @Override
    public Operand case_ExprUnary(MJExprUnary exprUnary) {
        throw new RuntimeException("Expression  has no L-value.");
    }



    @Override
    public Operand case_ArrayLength(MJArrayLength arrayLength) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_ExprThis(MJExprThis exprThis) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_ExprBinary(MJExprBinary exprBinary) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_ExprNull(MJExprNull exprNull) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_Number(MJNumber number) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_NewIntArray(MJNewIntArray newIntArray) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_MethodCall(MJMethodCall methodCall) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_NewObject(MJNewObject newObject) {
        throw new RuntimeException("Expression  has no L-value.");
    }

    @Override
    public Operand case_BoolConst(MJBoolConst boolConst) {
        throw new RuntimeException("Expression  has no L-value.");
    }

}
