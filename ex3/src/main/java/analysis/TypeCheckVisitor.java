package analysis;

import minijava.ast.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Server on 5/26/2017.
 */
public class TypeCheckVisitor extends MJElement.DefaultVisitor {
    // Override the access to all the neccessary methods and statementsExpr
    private SymbolTable symbolTable;
    private List<TypeError> typeErrors;
    private MJElement CurrentClass = null;
    private Map<MJClassDecl, MJExtended> classesInfo;

    /**
     * @param symbolTable symbolTable object
     * @param classesInfo classesInfo object
     * @param typeErrors typeErrors oject
     *  Initialize the variables
     */
    TypeCheckVisitor(SymbolTable symbolTable, Map<MJClassDecl, MJExtended> classesInfo, List<TypeError> typeErrors) {
        this.symbolTable = symbolTable;
        this.typeErrors = typeErrors;
        this.classesInfo = classesInfo;
    }

    /**
     * @param mainClass object from the class MJMainClass
     */
    @Override
    public void visit(MJMainClass mainClass) {
        symbolTable.push();
        CurrentClass = mainClass;
        super.visit(mainClass);
        symbolTable.pop();
    }

    /**
     * @param classDecl object from the class MJClassDecl
     */
    @Override
    public void visit(MJClassDecl classDecl) {
        symbolTable.push();
        CurrentClass = classDecl;
        super.visit(classDecl);
        symbolTable.pop();
    }

    /**
     * @param methodDecl object from the class MJMethodDecl
     */
    @Override
    public void visit(MJMethodDecl methodDecl) {
        symbolTable.push();
        super.visit(methodDecl);
        symbolTable.pop();
    }

    /**
     * @param block object from the class MJBlock
     */
    @Override
    public void visit(MJBlock block) {
        super.visit(block);
    }

    /**
     * @param varDecl object from the class MJVarDecl
     */
    @Override
    public void visit(MJVarDecl varDecl) {
        symbolTable.getCurrentScope().put(varDecl.getName(), varDecl);
        super.visit(varDecl);
    }

    /**
     * @param stmtAssign object from the class MJStmtAssign
     */
    @Override
    public void visit(MJStmtAssign stmtAssign) {
        MJType left = getExprType(stmtAssign.getLeft());
        MJType right = getExprType(stmtAssign.getRight());
        boolean result = TypeChecker.isSubType(new Type(left), new Type(right));

        if (!result) {
            typeErrors.add(new TypeError(stmtAssign.getRight(), "The right expression in the assignment statement is not type correct!"));
        }

        super.visit(stmtAssign);
    }

    /**
     * @param stmtIf object from the class MJStmtIf
     */
    @Override
    public void visit(MJStmtIf stmtIf) {
        MJType condition = getExprType(stmtIf.getCondition());
        if (!(condition instanceof MJTypeBool)/* || !(condition instanceof MJBoolConst)*/) {
            typeErrors.add(new TypeError(condition, "The condition is not of compatible type boolean."));
        }

        super.visit(stmtIf);
    }

    /**
     * @param stmtWhile object from the class MJStmtWhile
     */
    @Override
    public void visit(MJStmtWhile stmtWhile) {
        MJExpr condition = stmtWhile.getCondition();

        if (!(condition instanceof MJBoolConst)) {
            typeErrors.add(new TypeError(condition, "The condition inside WHILE is not of compatible type boolean."));
        }

        super.visit(stmtWhile);
    }

    /**
     * @param stmtReturn object from the class MJStmtReturn
     */
    @Override
    public void visit(MJStmtReturn stmtReturn) {
        if (CurrentClass instanceof MJMainClass) {
            typeErrors.add(new TypeError(stmtReturn, "The Main Class cannot have a return statement!"));
        }

        super.visit(stmtReturn);
    }


    /**
     * @param stmtPrint object from the class MJStmtPrint
     */
    @Override
    public void visit(MJStmtPrint stmtPrint) {
        MJType printed = getExprType(stmtPrint.getPrinted());
        if (!(printed instanceof MJTypeInt)) {
            typeErrors.add(new TypeError(printed, "The condition is not of compatible type int."));
        }

        super.visit(stmtPrint);
    }


    /**
     * @param exprBinary object from the class MJExprBinary
     */
    @Override
    public void visit(MJExprBinary exprBinary) {
        getExprType(exprBinary);
    }


    /**
     * @param exprUnary object from the class MJExprUnary
     */
    @Override
    public void visit(MJExprUnary exprUnary) {
        getExprType(exprUnary);
    }

    /**
     * @param exprNull object from the class MJExprNull
     */
    @Override
    public void visit(MJExprNull exprNull) {
        super.visit(exprNull);
    }

    /**
     * @param newIntArray object from the class MJNewIntArray
     */
    @Override
    public void visit(MJNewIntArray newIntArray) {
        super.visit(newIntArray);
    }

    /**
     * @param newObject object from the class MJNewObject
     */
    @Override
    public void visit(MJNewObject newObject) {
        super.visit(newObject);
    }

    /**
     * @param expr object from the class MJExpr
     * This method handles all the expression types and returns the corresponding type
     */
    public MJType getExprType(MJExpr expr) {

        return expr.match(new MJExpr.Matcher<MJType>() {

            @Override
            public MJType case_ExprUnary(MJExprUnary exprUnary) {
                MJUnaryOperator op = exprUnary.getUnaryOperator();
                MJType t = exprUnary.getExpr().match(this);
                if (op instanceof MJNegate) {
                    if (!(t instanceof MJTypeBool)) {
                        typeErrors.add(new TypeError(expr, "Negate operator takes boolean value."));
                    }
                    return MJ.TypeBool();
                }
                if (op instanceof MJUnaryMinus) {
                    if (!(t instanceof MJTypeInt)) {
                        typeErrors.add(new TypeError(expr, "Negate operator takes boolean value."));
                    }
                    return MJ.TypeInt();
                }
                return null;
            }

            @Override
            public MJType case_NewObject(MJNewObject newObject) {
                return MJ.TypeClass(newObject.getClassName());
            }

            @Override
            public MJType case_ExprBinary(MJExprBinary exprBinary) {
                MJOperator op = exprBinary.getOperator();
                MJType leftSide = exprBinary.getLeft().match(this);
                MJType rightSide = exprBinary.getRight().match(this);

                if (op instanceof MJPlus ||
                        op instanceof MJMinus ||
                        op instanceof MJTimes ||
                        op instanceof MJDiv) {
                    if (!(leftSide instanceof MJTypeInt)) {
                        typeErrors.add(new TypeError(exprBinary, "Left side of the binary expression must be type of integer."));
                    }
                    if (!(rightSide instanceof MJTypeInt)) {
                        typeErrors.add(new TypeError(exprBinary, "Right side of the binary expression must be type of integer."));
                    }
                    return MJ.TypeInt();
                }
                if (op instanceof MJLess/* || op instanceof MJEquals*/) //TODO implement MJequals
                {
                    if (!(leftSide instanceof MJTypeInt)) {
                        typeErrors.add(new TypeError(exprBinary, "Left side of the comparison must be type of integer."));
                    }
                    if (!(rightSide instanceof MJTypeInt)) {
                        typeErrors.add(new TypeError(exprBinary, "Right side of the comparison must be type of integer."));
                    }
                    return MJ.TypeBool();
                }
                if (op instanceof MJAnd) {
                    if (!(leftSide instanceof MJTypeBool)) {
                        typeErrors.add(new TypeError(exprBinary, "Left side of the and expression must be type of bool."));
                    }
                    if (!(rightSide instanceof MJTypeBool)) {
                        typeErrors.add(new TypeError(exprBinary, "right side of the and expression must be type of bool."));
                    }
                    return MJ.TypeBool();
                }
                return null;
            }

            @Override
            public MJType case_BoolConst(MJBoolConst boolConst) {
                return MJ.TypeBool();
            }

            @Override
            public MJType case_ExprNull(MJExprNull exprNull) {
                return null;
            }

            @Override
            public MJType case_VarUse(MJVarUse varUse) {
                String name = varUse.getVarName();

                if (symbolTable.getCurrentScope().containsKey(name)) {
                    MJVarDecl varDecl = symbolTable.getCurrentScope().get(name);
                    return varDecl.getType();
                } else {
                    // ADD ERROR
                    return null;
                }
            }

            @Override
            public MJType case_MethodCall(MJMethodCall methodCall) {


                MJTypeClass receiver = (MJTypeClass) methodCall.getReceiver().match(this);
                MJClassDecl classObj = null;
                Iterator it = classesInfo.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String className = ((MJClassDecl) pair.getKey()).getName();
                    if (className.equals(receiver.getName())) {
                        classObj = (MJClassDecl) pair.getKey();
                        break;
                    }
                }

                MJMethodDecl method = null;
                MJMethodDeclList methods = classObj.getMethods();
                for (MJMethodDecl methodDecl : methods) {
                    if (methodDecl.getName().equals(methodCall.getMethodName())) {
                        method = methodDecl;
                        break;
                    }
                }

                MJVarDeclList formalParameters = method.getFormalParameters();
                MJExprList argumentsList = methodCall.getArguments();
                for (int k = 0; k < formalParameters.size(); k++) {
                    if (!TypeChecker.isSubType(new Type(formalParameters.get(k).getType()), new Type(getExprType(argumentsList.get(k))))){
                        typeErrors.add(new TypeError(formalParameters.get(k), "The parameters should be of the same type!"));
                    }
                }


                return method != null ? method.getReturnType() : null;
            }

            @Override
            public MJType case_Number(MJNumber number) {
                return MJ.TypeInt();
            }

            @Override
            public MJType case_FieldAccess(MJFieldAccess fieldAccess) {
                MJTypeClass receiver = (MJTypeClass) fieldAccess.getReceiver().match(this);

                MJClassDecl classObj = null;
                Iterator it = classesInfo.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    String className = ((MJClassDecl) pair.getKey()).getName();
                    if (className.equals(receiver.getName())) {
                        classObj = (MJClassDecl) pair.getKey();
                        break;
                    }
                }

                MJVarDecl varName = null;
                MJVarDeclList fields = classObj.getFields();
                for (MJVarDecl varDecl : fields) {
                    if (varDecl.getName().equals(fieldAccess.getFieldName())) {
                        varName = varDecl;
                        break;
                    }
                }

                return varName != null ? varName.getType() : null;
            }

            @Override
            public MJType case_ArrayLookup(MJArrayLookup arrayLookup) {
                MJType arrayType = arrayLookup.getArrayExpr().match(this);
                MJType indexType = arrayLookup.getArrayIndex().match(this);
                if (!(arrayType instanceof MJTypeIntArray)) {
                    typeErrors.add(new TypeError(arrayLookup, "Id must be array of int."));
                }
                if (!(indexType instanceof MJTypeInt)) {
                    typeErrors.add(new TypeError(arrayLookup, "Arraylookup expression must be integer."));
                }
                return MJ.TypeInt();
            }

            @Override
            public MJType case_ExprThis(MJExprThis exprThis) {
                String name = "";
                if (CurrentClass instanceof MJMainClass) {
                    typeErrors.add(new TypeError(exprThis, "The Main Class cannot have refer to 'this' keyword, because it is static context!"));
                } else if (CurrentClass instanceof MJClassDecl) {
                    name = ((MJClassDecl) CurrentClass).getName();
                }

                return MJ.TypeClass(name);
            }

            @Override
            public MJType case_ArrayLength(MJArrayLength arrayLength) {
                MJType t = arrayLength.getArrayExpr().match(this);
                if (!(t instanceof MJTypeIntArray)) {
                    typeErrors.add(new TypeError(arrayLength, "Length only applies to int[]"));
                }
                return MJ.TypeInt();
            }

            @Override
            public MJType case_NewIntArray(MJNewIntArray newIntArray) {
                MJType t = newIntArray.getArraySize().match(this);
                if (!(t instanceof MJTypeInt)) {
                    typeErrors.add(new TypeError(newIntArray, "Array size must be type of integer type."));
                }
                return MJ.TypeIntArray();
            }
        });
    }
}