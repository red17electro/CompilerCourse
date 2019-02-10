package frontend;

import minijava.ast.*;

import java.io.PrintWriter;

public class AstPrinter implements MJElement.Visitor {
    private StringBuilder out = new StringBuilder();
    private int indent = 0;

    private void println() {
        out.append("\n");
        for (int i=0; i<indent; i++) {
            out.append("    ");
        }
    }

    private void print(String s) {
        out.append(s);
    }


    private void println(String s) {
        out.append(s);
        println();
    }


    @Override
    public void visit(MJClassDeclList classDeclList) {
        for (MJClassDecl c : classDeclList) {
            c.accept(this);
            println();
        }

    }

    @Override
    public void visit(MJNewIntArray newIntArray) {
        print("(new int[");
        newIntArray.getArraySize().accept(this);
        print("])");
    }

    @Override
    public void visit(MJExprNull exprNull) {
        print("null");
    }

    @Override
    public void visit(MJStmtAssign stmtAssign) {
        stmtAssign.getLeft().accept(this);
        print(" = ");
        stmtAssign.getRight().accept(this);
        println(";");
    }

    @Override
    public void visit(MJLess less) {
        print("<");
    }

    @Override
    public void visit(MJVarDeclList varDeclList) {
        for (MJVarDecl v : varDeclList) {
            v.accept(this);
            println(";");
        }
    }

    @Override
    public void visit(MJExtendsNothing extendsNothing) {

    }

    @Override
    public void visit(MJMainClass mainClass) {
        print("class ");
        print(mainClass.getName());
        indent++;
        println(" {");
        print("public static void main(String[] ");
        print(mainClass.getArgsName());
        print(")");
        mainClass.getMainBody().accept(this);
        indent--;
        println();
        println("}");
    }

    @Override
    public void visit(MJVarUse varUse) {
        print(varUse.getVarName());
    }

    @Override
    public void visit(MJExprList exprList) {
        for (MJExpr expr : exprList) {
            if (expr != exprList.get(0)) {
                print(", ");
            }
            expr.accept(this);
        }

    }

    @Override
    public void visit(MJNegate negate) {
        print("!");
    }

    @Override
    public void visit(MJStmtWhile stmtWhile) {
        print("while (");
        stmtWhile.getCondition().accept(this);
        print(") ");
        stmtWhile.getLoopBody().accept(this);
    }

    @Override
    public void visit(MJExtendsClass extendsClass) {
        print("extends ");
        print(extendsClass.getName());
        print(" ");
    }

    @Override
    public void visit(MJArrayLookup arrayLookup) {
        arrayLookup.getArrayExpr().accept(this);
        print("[");
        arrayLookup.getArrayIndex().accept(this);
        print("]");
    }

    @Override
    public void visit(MJMethodCall methodCall) {
        methodCall.getReceiver().accept(this);
        print(".");
        print(methodCall.getMethodName());
        print("(");
        methodCall.getArguments().accept(this);
        print(")");
    }

    @Override
    public void visit(MJTypeClass typeClass) {
        print(typeClass.getName());
    }

    @Override
    public void visit(MJNewObject newObject) {
        print("new ");
        print(newObject.getClassName());
        print("()");
    }

    @Override
    public void visit(MJTimes times) {
        print("*");
    }

    @Override
    public void visit(MJMinus minus) {
        print("-");
    }

    @Override
    public void visit(MJExprUnary exprUnary) {
        print("(");
        exprUnary.getUnaryOperator().accept(this);
        print(" ");
        exprUnary.getExpr().accept(this);
        print(")");
    }

    @Override
    public void visit(MJStmtReturn stmtReturn) {
        print("return ");
        stmtReturn.getResult().accept(this);
        println(";");
    }

    @Override
    public void visit(MJProgram program) {
        program.getMainClass().accept(this);
        program.getClassDecls().accept(this);
    }

    @Override
    public void visit(MJEquals equals) {
        print("==");
    }

    @Override
    public void visit(MJUnaryMinus unaryMinus) {
        print("-");
    }

    @Override
    public void visit(MJExprThis exprThis) {
        print("this");
    }

    @Override
    public void visit(MJVarDecl varDecl) {
        varDecl.getType().accept(this);
        print(" ");
        print(varDecl.getName());
    }

    @Override
    public void visit(MJFieldAccess fieldAccess) {
        fieldAccess.getReceiver().accept(this);
        print(".");
        print(fieldAccess.getFieldName());
    }

    @Override
    public void visit(MJNumber number) {
        print(""+number.getIntValue());
    }

    @Override
    public void visit(MJTypeInt typeInt) {
        print("int");
    }

    @Override
    public void visit(MJMethodDeclList methodDeclList) {
        for (MJMethodDecl m : methodDeclList) {
            m.accept(this);
            println();
        }
    }

    @Override
    public void visit(MJArrayLength arrayLength) {
        arrayLength.getArrayExpr().accept(this);
        print(".length");
    }

    @Override
    public void visit(MJStmtPrint stmtPrint) {
        print("System.out.println(");
        stmtPrint.getPrinted().accept(this);
        println(");");
    }

    @Override
    public void visit(MJPlus plus) {
        print("+");
    }

    @Override
    public void visit(MJTypeIntArray typeIntArray) {
        print("int[]");
    }

    @Override
    public void visit(MJExprBinary exprBinary) {
        print("(");
        exprBinary.getLeft().accept(this);
        print(" ");
        exprBinary.getOperator().accept(this);
        print(" ");
        exprBinary.getRight().accept(this);
        print(")");
    }

    @Override
    public void visit(MJBlock block) {
        indent++;
        println("{");
        for (MJStatement s : block) {
            s.accept(this);
            if (s instanceof MJVarDecl) {
                println(";");
            }
        }
        indent--;
        println();
        println("}");

    }

    @Override
    public void visit(MJDiv div) {
        print("/");
    }

    @Override
    public void visit(MJBoolConst boolConst) {
        print(""+boolConst.getBoolValue());
    }

    @Override
    public void visit(MJStmtIf stmtIf) {
        print("if (");
        stmtIf.getCondition().accept(this);
        print(") ");
        stmtIf.getIfTrue().accept(this);
        print("else ");
        stmtIf.getIfFalse().accept(this);

    }

    @Override
    public void visit(MJTypeBool typeBool) {
        print("boolean");
    }

    @Override
    public void visit(MJAnd and) {
        print("&&");
    }

    @Override
    public void visit(MJStmtExpr stmtExpr) {
        stmtExpr.getExpr().accept(this);
        println(";");
    }

    @Override
    public void visit(MJMethodDecl methodDecl) {
        methodDecl.getReturnType().accept(this);
        print(" ");
        print(methodDecl.getName());
        print("(");
        for (MJVarDecl p : methodDecl.getFormalParameters()) {
            if (p != methodDecl.getFormalParameters().get(0)) {
                print(", ");
            }
            p.accept(this);
        }
        print(") ");
        methodDecl.getMethodBody().accept(this);
    }

    @Override
    public void visit(MJClassDecl classDecl) {
        print("class ");
        print(classDecl.getName());
        print(" ");
        classDecl.getExtended().accept(this);
        indent++;
        println(" {");
        for (MJVarDecl v : classDecl.getFields()) {
            v.accept(this);
            println(";");
        }
        classDecl.getMethods().accept(this);
        indent--;
        println("}");

    }

    public static String print(MJElement ast) {
        AstPrinter printer = new AstPrinter();
        ast.accept(printer);
        return printer.out.toString();
    }
}
