package translation;

import analysis.Analysis;
import minijava.ast.*;
import minillvm.ast.*;
import minillvm.ast.Type;

import java.util.*;

import static minillvm.ast.Ast.*;
import static minillvm.ast.Ast.ConstBool;

/**
 * Created by Server on 6/2/2017.
 */

public class TranslateVisitor extends MJElement.DefaultVisitor {
    private Prog prog;
    private BasicBlockList blocks = BasicBlockList();
    private BasicBlock currentBlock = null;
    private Proc mainProc = null;
    private Map<MJVarDecl, TemporaryVar> declMapping = new HashMap<>();
    private StructField data = StructField(TypeArray(TypeInt(), 0), "data");
    private StructField size = StructField(TypeInt(), "size");
    private ArrayList<StructField> structFieldArrayList = new ArrayList<StructField>() {{
        add(size);
        add(data);
    }};
    private StructFieldList structFields = StructFieldList(structFieldArrayList);
    private TypeStruct struct = TypeStruct("array", structFields);

    public void addCommentInstr(String message, frontend.SourcePosition sourcePosition) {
        if (sourcePosition != null && currentBlock != null) {
            String str = "" + message + " at line " + sourcePosition.getLine();
            currentBlock.add(CommentInstr(str));
        }
    }

    /**
     * @return translated program
     */
    Prog getProg() {
        return prog;
    }

    /**
     * @param program object to access the MJProgram
     */
    @Override
    public void visit(MJProgram program) {
        // adding our structure Array to the TypeStructList of the program
        prog = Ast.Prog(TypeStructList(struct), GlobalList(), ProcList());
        super.visit(program);
        if (mainProc != null) {
            prog.getProcedures().add(mainProc);
        }
    }

    /**
     * @param block MJBlock object
     *              Once visit MJBlock object we are creating a new current block and we are performing logic for specific statements
     */
    @Override
    public void visit(MJBlock block) {
        currentBlock = BasicBlock();
        currentBlock.setName("CurrentBlock");
        addCommentInstr("Adding block ", block.getSourcePosition());
        blocks.add(currentBlock);
        getTheInstruction(block);
        // The termination instruction for the Main method
        currentBlock.add(ReturnExpr(ConstInt(0)));
        currentBlock = null;
    }

    /**
     * @param mainClass MJMainClass object
     */
    @Override
    public void visit(MJMainClass mainClass) {
        super.visit(mainClass);
        mainProc = Proc("main", TypeInt(), ParameterList(), blocks);
    }

    /**
     * @param e MJStatement object
     *          Go through all the possible statement types by using the matcher
     */
    private void getTheInstruction(MJStatement e) {
        e.match(new MJStatement.MatcherVoid() {

            /**
             * @param stmtAssign MJStmtAssign object which consists of expression assign
             */
            @Override
            public void case_StmtAssign(MJStmtAssign stmtAssign) {
                MJExpr left = stmtAssign.getLeft();
                Operand right = getExpr(stmtAssign.getRight());
                if (left instanceof MJVarUse) {
                    TemporaryVar leftvar = declMapping.get(((MJVarUse) left).getVariableDeclaration());
                    currentBlock.add(Store(VarRef(leftvar), right));
                } else if (left instanceof MJArrayLookup) {
                    MJArrayLookup arrayLookup = (MJArrayLookup) left;
                    Operand arrayExp = getExpr(arrayLookup.getArrayExpr());
                    Operand index = getExpr(arrayLookup.getArrayIndex());

                    BasicBlock invalidIndex = BasicBlock();
                    invalidIndex.setName("invalidIndexLeftLookup");
                    BasicBlock validIndex = BasicBlock();
                    validIndex.setName("indexValidLeftLookup");

                    blocks.add(invalidIndex);
                    blocks.add(validIndex);

                    TemporaryVar length = TemporaryVar("lengthVar");
                    TemporaryVar lengthValue = TemporaryVar("lengthValue");
                    currentBlock.add(GetElementPtr(length, arrayExp.copy(), OperandList(ConstInt(0),
                            ConstInt(0))));
                    currentBlock.add(Load(lengthValue, VarRef(length)));

                    TemporaryVar isGreater = TemporaryVar("IndexGreaterThanZero");
                    currentBlock.add(BinaryOperation(isGreater, ConstInt(0), Slt(), index));
                    TemporaryVar isEqual = TemporaryVar("IndexEqualToZero");
                    currentBlock.add(BinaryOperation(isEqual, ConstInt(0), Eq(), index.copy()));
                    TemporaryVar isLess = TemporaryVar("isLessThanLength");
                    currentBlock.add(BinaryOperation(isLess, index.copy(), Slt(), VarRef(lengthValue)));
                    TemporaryVar isGreaterOrEqual = TemporaryVar("isGreaterOrEqual");
                    currentBlock.add(BinaryOperation(isGreaterOrEqual, VarRef(isGreater), Or(), VarRef(isEqual)));

                    TemporaryVar isIndexValid = TemporaryVar("isIndexValid");
                    currentBlock.add(BinaryOperation(isIndexValid, VarRef(isLess), And(), VarRef(isGreaterOrEqual)));
                    currentBlock.add(Branch(VarRef(isIndexValid), validIndex, invalidIndex));

                    currentBlock = invalidIndex;
                    currentBlock.add(HaltWithError("The index of the array is out of defined bounds!"));

                    currentBlock = validIndex;

                    TemporaryVar addressVar = TemporaryVar("address");
                    currentBlock.add(GetElementPtr(addressVar, arrayExp.copy(), OperandList(ConstInt(0), ConstInt(1),index.copy())));
                    currentBlock.add(Store(VarRef(addressVar), right));
                } else {
                    throw new RuntimeException(
                            "Unexpected expression");
                }
            }

            /**
             * @param block MJBlock object
             * Iterate through the block object and use match the corresponding statements
             */
            @Override
            public void case_Block(MJBlock block) {
                for (MJStatement i : block) {
                    i.match(this);
                }
            }

            @Override
            public void case_StmtReturn(MJStmtReturn stmtReturn) {
                // TODO We will need it for the methods, but not now!
            }

            /**
             * @param stmtPrint MJStmtPrint object
             * handle the print statement by using print Instructions in AST.
             */
            @Override
            public void case_StmtPrint(MJStmtPrint stmtPrint) {
                Operand printed = getExpr(stmtPrint.getPrinted());
                addCommentInstr("Print statement", stmtPrint.getSourcePosition());
                currentBlock.add(Print(printed));
            }

            /**
             * @param stmtWhile MJStmtWhile object which contains while loop statement
             * The logic is to separate While Loop in three blocks: condition, truePart and falsePart
                               and to add instructions to the corresponding blocks */
            @Override
            public void case_StmtWhile(MJStmtWhile stmtWhile) {
                BasicBlock loopCondition = BasicBlock();
                loopCondition.setName("WhileCondition");
                BasicBlock truePart = BasicBlock();
                truePart.setName("WhileTrue");
                BasicBlock falsePart = BasicBlock();
                falsePart.setName("WhileFalse");

                addCommentInstr("While statement ", stmtWhile.getSourcePosition());
                currentBlock.add(Jump(loopCondition));
                blocks.add(loopCondition);
                currentBlock = loopCondition;
                Operand condition = getExpr(stmtWhile.getCondition());
                Branch whileBranch = Branch(condition, truePart, falsePart);
                currentBlock.add(whileBranch);

                blocks.add(truePart);
                currentBlock = truePart;
                getTheInstruction(stmtWhile.getLoopBody());
                currentBlock.add(Jump(loopCondition));

                blocks.add(falsePart);
                currentBlock = falsePart;
            }

            @Override
            public void case_StmtExpr(MJStmtExpr stmtExpr) {
            }

            /**
             * @param varDecl MJVarDecl object
             *                puts the declared variables into our manual collection which mapps varDecls to TemporaryVars
             */
            @Override
            public void case_VarDecl(MJVarDecl varDecl) {
                // Parameter could be used in the procedure declarations
                if (declMapping.get(varDecl) == null) {
                    TemporaryVar variable = TemporaryVar(varDecl.getName());
                    declMapping.put(varDecl, variable);
                    addCommentInstr("VarDecl statement " + varDecl.getName(), varDecl.getSourcePosition());
                    currentBlock.add(Alloca(declMapping.get(varDecl), getType(varDecl.getType())));
                }
            }

            /**
             * @param stmtIf MJStmtIf object which contains if statement
             *               The idea is the same as for While case.
             *               If statement is divided into three blocks, which are going to be filled with instructions
             */
            @Override
            public void case_StmtIf(MJStmtIf stmtIf) {
                BasicBlock ifCondition = BasicBlock();
                ifCondition.setName("ifCondition");
                BasicBlock truePart = BasicBlock();
                truePart.setName("True");
                BasicBlock falsePart = BasicBlock();
                falsePart.setName("False");
                BasicBlock restOfTheCode = BasicBlock();
                restOfTheCode.setName("RestOfTheCode");

                addCommentInstr("If statement", stmtIf.getSourcePosition());
                currentBlock.add(Jump(ifCondition));
                blocks.add(ifCondition);
                currentBlock = ifCondition;
                Operand condition = getExpr(stmtIf.getCondition());
                Branch ifBranch = Branch(condition, truePart, falsePart);
                currentBlock.add(ifBranch);

                blocks.add(truePart);
                currentBlock = truePart;
                getTheInstruction(stmtIf.getIfTrue());
                currentBlock.add(Jump(restOfTheCode));

                blocks.add(falsePart);
                currentBlock = falsePart;
                getTheInstruction(stmtIf.getIfFalse());
                currentBlock.add(Jump(restOfTheCode));

                blocks.add(restOfTheCode);
                currentBlock = restOfTheCode;
            }
        });
    }

    /**
     * @param expr MJExpr object
     * @return Operand element which matches with the corresponding expression
     */
    public Operand getExpr(MJExpr expr) {
        return expr.match(new MJExpr.Matcher<Operand>() {
            @Override
            public Operand case_ExprUnary(MJExprUnary exprUnary) {
                Operand expr = exprUnary.getExpr().match(this);
                return getUnaryOperator(exprUnary.getUnaryOperator(), expr);
            }

            @Override
            public Operand case_NewObject(MJNewObject newObject) {
                return null;
            }

            @Override
            public Operand case_ExprBinary(MJExprBinary exprBinary) {
                Operand left = exprBinary.getLeft().match(this);
                Operand right = exprBinary.getRight().match(this);
                Operator operator = getOperator(exprBinary.getOperator());

                TemporaryVar result = TemporaryVar("result");
                BasicBlock divByZero = BasicBlock();
                divByZero.setName("divByZero");
                BasicBlock divValid = BasicBlock();
                divValid.setName("divValid");

                addCommentInstr("Binary Expression statement of " + operator.toString(), exprBinary.getSourcePosition());

                //if the operator is Sdiv we have to consider about the possibility of divide by zero
                if (operator instanceof Sdiv) {
                    TemporaryVar isZero = TemporaryVar("isZero");
                    BinaryOperation condition = BinaryOperation(isZero, right, Eq(), ConstInt(0));
                    currentBlock.add(condition);

                    Branch divBranch = Branch(VarRef(isZero), divByZero, divValid);
                    currentBlock.add(divBranch);

                    blocks.add(divByZero);
                    currentBlock = divByZero;
                    currentBlock.add(HaltWithError("Division by zero"));

                    blocks.add(divValid);
                    currentBlock = divValid;

                    currentBlock.add(BinaryOperation(result, left, operator, right.copy()));
                } else {
                    currentBlock.add(BinaryOperation(result, left, operator, right));
                }

                return VarRef(result);
            }

            @Override
            public Operand case_BoolConst(MJBoolConst boolConst) {
                return ConstBool(boolConst.getBoolValue());
            }

            @Override
            public Operand case_ExprNull(MJExprNull exprNull) {
                return Nullpointer();
            }

            @Override
            public Operand case_VarUse(MJVarUse varUse) {
                TemporaryVar varUsage = TemporaryVar("temporary");
                TemporaryVar var = declMapping.get(varUse.getVariableDeclaration());
                addCommentInstr("VarUse of " + varUse.getVarName() + " statement", varUse.getSourcePosition());
                currentBlock.add(Load(varUsage, VarRef(var)));
                return VarRef(varUsage);
            }

            /**
             * @param methodCall
             * @return
             */
            @Override
            public Operand case_MethodCall(MJMethodCall methodCall) {
                Operand receiver = getExpr(methodCall.getReceiver());
                MJMethodDecl mDecl = methodCall.getMethodDeclaration();
                Ast.TypePointer(classTranslator.getStructTypeFor(classDeclaration))
                receiver = addCastIfNecessary(receiver, getPointerToClassStruct((MJClassDecl) mDecl.getParent().getParent()));

                OperandList args = Ast.OperandList(receiver);
                for (int i = 0; i < methodCall.getArguments().size(); i++) {
                    Operand arg = tr.exprRvalue(methodCall.getArguments().get(i));
                    MJVarDeclList formalParameters = mDecl.getFormalParameters();
                    arg = tr.addCastIfNecessary(arg, tr.translateType(formalParameters.get(i).getType()));
                    args.add(arg);
                }

                // lookup in vtable
                Operand proc = tr.getClassTranslator().loadProcFromVtable(receiver, methodCall.getMethodDeclaration());
                // do the call
                TemporaryVar result = Ast.TemporaryVar(methodCall.getMethodName() + "_result");
                tr.addInstruction(Ast.Call(result, proc, args));
                return Ast.VarRef(result);
            }

            @Override
            public Operand case_Number(MJNumber number) {
                return ConstInt(number.getIntValue());
            }

            @Override
            public Operand case_FieldAccess(MJFieldAccess fieldAccess) {
                return null;
            }

            /**
             * @param arrayLookup MJArrayLookup arrayLookup
             * @return the value, which is stored in the specific array position
             */
            @Override
            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {
                Operand arrayExp = getExpr(arrayLookup.getArrayExpr());
                Operand index = getExpr(arrayLookup.getArrayIndex());

                addCommentInstr("Array Lookup Statement", arrayLookup.getSourcePosition());

                BasicBlock invalidIndex = BasicBlock();
                invalidIndex.setName("invalidIndexLookup");
                BasicBlock validIndex = BasicBlock();
                validIndex.setName("indexValidLookup");

                blocks.add(invalidIndex);
                blocks.add(validIndex);

                TemporaryVar length = TemporaryVar("lengthVar");
                TemporaryVar lengthValue = TemporaryVar("lengthValue");
                currentBlock.add(GetElementPtr(length, arrayExp, OperandList(ConstInt(0),
                        ConstInt(0))));
                currentBlock.add(Load(lengthValue, VarRef(length)));

                TemporaryVar isGreater = TemporaryVar("IndexGreaterThanZero");
                currentBlock.add(BinaryOperation(isGreater, ConstInt(0), Slt(), index));
                TemporaryVar isEqual = TemporaryVar("IndexEqualToZero");
                currentBlock.add(BinaryOperation(isEqual, ConstInt(0), Eq(), index.copy()));
                TemporaryVar isLess = TemporaryVar("isLessThanLength");
                currentBlock.add(BinaryOperation(isLess, index.copy(), Slt(), VarRef(lengthValue)));
                TemporaryVar isGreaterOrEqual = TemporaryVar("isGreaterOrEqual");
                currentBlock.add(BinaryOperation(isGreaterOrEqual, VarRef(isGreater), Or(), VarRef(isEqual)));

                TemporaryVar isIndexValid = TemporaryVar("isIndexValid");
                currentBlock.add(BinaryOperation(isIndexValid, VarRef(isLess), And(), VarRef(isGreaterOrEqual)));
                currentBlock.add(Branch(VarRef(isIndexValid), validIndex, invalidIndex));

                currentBlock = invalidIndex;
                currentBlock.add(HaltWithError("The index of the array is out of defined bounds!"));

                currentBlock = validIndex;
                TemporaryVar dataAddress = TemporaryVar("dataAddress");
                TemporaryVar itemValue = TemporaryVar("itemValue");
                currentBlock.add(GetElementPtr(dataAddress, arrayExp.copy(), OperandList(ConstInt(0), ConstInt(1),  index.copy())));
                currentBlock.add(Load(itemValue, VarRef(dataAddress)));

                return VarRef(itemValue);
            }

            @Override
            public Operand case_ExprThis(MJExprThis exprThis) {
                return null;
            }

            /**
             * @param arrayLength MJArrayLength
             * @return the length of the array
             */
            @Override
            public Operand case_ArrayLength(MJArrayLength arrayLength) {
                Operand arrayExp = getExpr(arrayLength.getArrayExpr());
                TemporaryVar length = TemporaryVar("lengthVar");
                TemporaryVar lengthValue = TemporaryVar("lengthValue");
                addCommentInstr("Array Length Statement", arrayLength.getSourcePosition());
                currentBlock.add(GetElementPtr(length, arrayExp, OperandList(ConstInt(0),
                        ConstInt(0))));

                currentBlock.add(Load(lengthValue, VarRef(length)));
                return VarRef(lengthValue);
            }

            /**
             * @param newIntArray MJNewIntArray
             * @return pointer to struct type, for which memory has been allocated
             */
            @Override
            public Operand case_NewIntArray(MJNewIntArray newIntArray) {
                Operand sizeOfArray = getExpr(newIntArray.getArraySize());
                addCommentInstr("NewIntArray statement", newIntArray.getSourcePosition());

                TemporaryVar isGreater = TemporaryVar("LengthGreaterThanZero");
                currentBlock.add(BinaryOperation(isGreater, ConstInt(0), Slt(), sizeOfArray));
                TemporaryVar isEqual = TemporaryVar("LengthEqualToZero");
                currentBlock.add(BinaryOperation(isEqual, ConstInt(0), Eq(), sizeOfArray.copy()));
                TemporaryVar isGreaterOrEqual = TemporaryVar("isGreaterOrEqual");
                currentBlock.add(BinaryOperation(isGreaterOrEqual, VarRef(isGreater), Or(), VarRef(isEqual)));

                BasicBlock invalidIndex = BasicBlock();
                invalidIndex.setName("invalidIndexArray");
                BasicBlock validIndex = BasicBlock();
                validIndex.setName("indexValidArray");

                currentBlock.add(Branch(VarRef(isGreaterOrEqual), validIndex, invalidIndex));

                blocks.add(invalidIndex);
                currentBlock = invalidIndex;
                currentBlock.add(HaltWithError("The array is defined with length that is not possible!"));

                blocks.add(validIndex);
                currentBlock = validIndex;

                TemporaryVar structVar = TemporaryVar("structVar");
                TemporaryVar compensateSizeFieldVar = TemporaryVar("compensateSizeFieldVar");
                TemporaryVar SizeOfArrayVar = TemporaryVar("SizeOfArrayVar");
                TemporaryVar memoryPointer = TemporaryVar("memoryPointer");
                currentBlock.add(BinaryOperation(compensateSizeFieldVar, ConstInt(1), Add(), sizeOfArray.copy()));
                currentBlock.add(BinaryOperation(SizeOfArrayVar, ConstInt(4), Mul(), VarRef(compensateSizeFieldVar)));
                currentBlock.add(Alloc(memoryPointer, VarRef(SizeOfArrayVar))); // we have now some part of the memory that we can use for array alloc

                currentBlock.add(Bitcast(structVar, TypePointer(struct), VarRef(memoryPointer)));
                TemporaryVar arrayLength = TemporaryVar("arrayLength");
                currentBlock.add(GetElementPtr(arrayLength, VarRef(structVar), OperandList(ConstInt(0), ConstInt(0))));
                currentBlock.add(Store(VarRef(arrayLength), sizeOfArray.copy()));

                BasicBlock loopCondition = BasicBlock();
                loopCondition.setName("WhileCondition");
                BasicBlock truePart = BasicBlock();
                truePart.setName("WhileTrue");
                BasicBlock falsePart = BasicBlock();
                falsePart.setName("WhileFalse");

                TemporaryVar count = TemporaryVar("count");
                TemporaryVar countValue = TemporaryVar("countValue");
                currentBlock.add(Alloca(count, TypeInt()));
                currentBlock.add(Store(VarRef(count), ConstInt(0)));
                currentBlock.add(Jump(loopCondition));
                blocks.add(loopCondition);
                currentBlock = loopCondition;

                currentBlock.add(Load(countValue,VarRef(count)));
                TemporaryVar loopBreaker = TemporaryVar("result");
                currentBlock.add(BinaryOperation(loopBreaker, VarRef(countValue), Eq(), sizeOfArray.copy()));
                Branch whileBranch = Branch(VarRef(loopBreaker), falsePart, truePart);

                currentBlock.add(whileBranch);

                blocks.add(truePart);
                currentBlock = truePart;

                TemporaryVar addressVar = TemporaryVar("addressVar");
                TemporaryVar incrementCount = TemporaryVar("incrementCount");
                TemporaryVar index = TemporaryVar("index");
                currentBlock.add(Load(index,VarRef(count)));
                currentBlock.add(GetElementPtr(addressVar, VarRef(structVar), OperandList(ConstInt(0), ConstInt(1), VarRef(index))));
                currentBlock.add(Store(VarRef(addressVar), ConstInt(0)));

                currentBlock.add(BinaryOperation(incrementCount, VarRef(countValue), Add(), ConstInt(1)));
                currentBlock.add(Store(VarRef(count), VarRef(incrementCount)));
                currentBlock.add(Jump(loopCondition));

                blocks.add(falsePart);
                currentBlock = falsePart;

                return VarRef(structVar);
            }
        });
    }

    /**
     * @param operator MJOperator object
     * @return llvm operator type which matches with the corresponding MJOperator type
     */
    public Operator getOperator(MJOperator operator) {
        return operator.match(new MJOperator.Matcher<Operator>() {
            @Override
            public Operator case_And(MJAnd and) {
                return And();
            }

            @Override
            public Operator case_Times(MJTimes times) {
                return Mul();
            }

            @Override
            public Operator case_Div(MJDiv div) {
                return Sdiv();
            }

            @Override
            public Operator case_Plus(MJPlus plus) {
                return Add();
            }

            @Override
            public Operator case_Minus(MJMinus minus) {
                return Sub();
            }

            @Override
            public Operator case_Equals(MJEquals equals) {
                return Eq();
            }

            @Override
            public Operator case_Less(MJLess less) {
                return Slt();
            }
        });
    }

    /**
     * @param operator MJUnaryOperator object
     * @param var      expression value of type Operand
     * @return reference to the temporary variable result
     */
    public Operand getUnaryOperator(MJUnaryOperator operator, Operand var) {

        TemporaryVar result = TemporaryVar("result");

        return operator.match(new MJUnaryOperator.Matcher<Operand>() {
            @Override
            public Operand case_UnaryMinus(MJUnaryMinus unaryMinus) {
                addCommentInstr("Unary Minus Expression statement", unaryMinus.getSourcePosition());
                currentBlock.add(BinaryOperation(result, ConstInt(0), Sub(), var));
                return VarRef(result);
            }

            @Override
            public Operand case_Negate(MJNegate negate) {
                addCommentInstr("Negate Expression statement ", negate.getSourcePosition());
                currentBlock.add(BinaryOperation(result, ConstBool(true), Xor(), var));
                return VarRef(result);
            }
        });
    }

    /**
     * @param mjType MJType object
     * @return llvm type which matches with the corresponding MJtype
     */
    public Type getType(MJType mjType) {
        return mjType.match(new MJType.Matcher<Type>() {
            @Override
            public Type case_TypeInt(MJTypeInt typeInt) {
                return TypeInt();
            }

            @Override
            public Type case_TypeBool(MJTypeBool typeBool) {
                return TypeBool();
            }

            @Override
            public Type case_TypeIntArray(MJTypeIntArray typeIntArray) {
                return TypePointer(struct);
            }

            @Override
            public Type case_TypeClass(MJTypeClass typeClass) {
                throw new RuntimeException("Should be implemented in the next assignment, I guess"); // TODO IMPLEMENT
            }
        });
    }

    public Operand addCastIfNecessary(Operand value, Type expectedType) {
        if (expectedType.equalsType(value.calculateType())) {
            return value;
        }
        TemporaryVar castValue = Ast.TemporaryVar("castValue");
        currentBlock.add(Ast.Bitcast(castValue, expectedType, value));
        return Ast.VarRef(castValue);
    }
}