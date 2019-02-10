package translation;

import frontend.AstPrinter;
import minijava.ast.*;
import minillvm.ast.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 26.05.16.
 */
public class Translator {


    private Proc currentProcedure;
    private BasicBlock currentBlock;
    private StmtTranslator stmtTranslator = new StmtTranslator(this);
    private ExprLValue exprLValue = new ExprLValue(this);
    private ExprRValue exprRValue = new ExprRValue(this);
    private ClassTranslator classTranslator = new ClassTranslator(this);
    private Type arrayPointerType;
    private Prog prog = Ast.Prog(Ast.TypeStructList(), Ast.GlobalList(), Ast.ProcList());
    private MJProgram javaProg;
    private Map<MJVarDecl, TemporaryVar> localVarLocation = new HashMap<>();
    private Proc newIntArrayFunc;

    public Translator(MJProgram javaProg) {
        this.javaProg = javaProg;
    }

    public Prog translate() {
        createArrayType();

        // translate classes
        classTranslator.translate(javaProg);

        // translate main method
        translateMainMethod();

        // translate methods
        translateMethods();

        return prog;
    }

    private void createArrayType() {
        TypeStruct arrayStruct = Ast.TypeStruct("intArray",
                Ast.StructFieldList(
                        Ast.StructField(Ast.TypeInt(), "length"),
                        Ast.StructField(Ast.TypeArray(Ast.TypeInt(), 0), "data")
                ));
        addStructType(arrayStruct);
        arrayPointerType = Ast.TypePointer(arrayStruct);

        createNewIntArrayProc();
    }

    private void createNewIntArrayProc() {
        Parameter size = Ast.Parameter(Ast.TypeInt(), "size");
        newIntArrayFunc = Ast.Proc("newIntArray", arrayPointerType, Ast.ParameterList(size), Ast.BasicBlockList());
        addProcedure(newIntArrayFunc);
        setCurrentProc(newIntArrayFunc);

        BasicBlock init = newBasicBlock("init");
        addBasicBlock(init);
        setCurrentBlock(init);
        TemporaryVar sizeLessThanZero = Ast.TemporaryVar("sizeLessThanZero");
        addInstruction(Ast.BinaryOperation(sizeLessThanZero, Ast.VarRef(size), Ast.Slt(), Ast.ConstInt(0)));
        BasicBlock negativeSize = newBasicBlock("negativeSize");
        BasicBlock goodSize = newBasicBlock("goodSize");
        currentBlock.add(Ast.Branch(Ast.VarRef(sizeLessThanZero), negativeSize, goodSize));

        addBasicBlock(negativeSize);
        negativeSize.add(Ast.HaltWithError("Array Size must be positive"));

        addBasicBlock(goodSize);
        setCurrentBlock(goodSize);

        // allocate space for the array
        TemporaryVar arraySizeWithLen = Ast.TemporaryVar("arraySizeWitLen");
        addInstruction(Ast.BinaryOperation(arraySizeWithLen, Ast.VarRef(size), Ast.Add(), Ast.ConstInt(1)));
        TemporaryVar arraySizeInBytes = Ast.TemporaryVar("arraySizeInBytes");
        addInstruction(Ast.BinaryOperation(arraySizeInBytes, Ast.VarRef(arraySizeWithLen), Ast.Mul(), Ast.ConstInt(4)));
        TemporaryVar mallocResult = Ast.TemporaryVar("mallocRes");
        addInstruction(Ast.Alloc(mallocResult, Ast.VarRef(arraySizeInBytes)));
        TemporaryVar newArray = Ast.TemporaryVar("newArray");
        addInstruction(Ast.Bitcast(newArray, getArrayPointerType(), Ast.VarRef(mallocResult)));

        // store the size
        TemporaryVar sizeAddr = Ast.TemporaryVar("sizeAddr");
        addInstruction(Ast.GetElementPtr(sizeAddr, Ast.VarRef(newArray), Ast.OperandList(Ast.ConstInt(0), Ast.ConstInt(0))));
        addInstruction(Ast.Store(Ast.VarRef(sizeAddr), Ast.VarRef(size)));

        // initialize Array with zeros:

        BasicBlock loopStart = newBasicBlock("loopStart");
        BasicBlock loopBody = newBasicBlock("loopBody");
        BasicBlock loopEnd = newBasicBlock("loopEnd");
        TemporaryVar iVar = Ast.TemporaryVar("iVar");
        currentBlock.add(Ast.Alloca(iVar, Ast.TypeInt()));
        currentBlock.add(Ast.Store(Ast.VarRef(iVar), Ast.ConstInt(0)));
        currentBlock.add(Ast.Jump(loopStart));

        // loop condition: while i < size
        addBasicBlock(loopStart);
        setCurrentBlock(loopStart);
        TemporaryVar i = Ast.TemporaryVar("i");
        TemporaryVar nextI = Ast.TemporaryVar("nextI");
        loopStart.add(Ast.Load(i, Ast.VarRef(iVar)));
        TemporaryVar smallerSize = Ast.TemporaryVar("smallerSize");
        addInstruction(Ast.BinaryOperation(smallerSize, Ast.VarRef(i), Ast.Slt(), Ast.VarRef(size)));
        currentBlock.add(Ast.Branch(Ast.VarRef(smallerSize), loopBody, loopEnd));

        // loop body
        addBasicBlock(loopBody);
        setCurrentBlock(loopBody);
        // ar[i] = 0;
        TemporaryVar iAddr = Ast.TemporaryVar("iAddr");
        addInstruction(Ast.GetElementPtr(iAddr, Ast.VarRef(newArray), Ast.OperandList(Ast.ConstInt(0), Ast.ConstInt(1), Ast.VarRef(i))));
        addInstruction(Ast.Store(Ast.VarRef(iAddr), Ast.ConstInt(0)));

        // nextI = i + 1;
        addInstruction(Ast.BinaryOperation(nextI, Ast.VarRef(i), Ast.Add(), Ast.ConstInt(1)));
        // store new value in i
        addInstruction(Ast.Store(Ast.VarRef(iVar), Ast.VarRef(nextI)));

        loopBody.add(Ast.Jump(loopStart));

        addBasicBlock(loopEnd);
        loopEnd.add(Ast.ReturnExpr(Ast.VarRef(newArray)));


    }

    private void translateMethods() {
        for (MJClassDecl c : javaProg.getClassDecls()) {
            for (MJMethodDecl m : c.getMethods()) {
                translateMethod(c, m);
            }
        }
    }


    public void translateMethod(MJClassDecl c, MJMethodDecl m) {
        Proc proc = classTranslator.getProcImpl(m);
        setCurrentProc(proc);
        BasicBlock initBlock = newBasicBlock("init");
        addBasicBlock(initBlock);
        setCurrentBlock(initBlock);

        localVarLocation.clear();


        // store copies of the parameters in Allocas, to make uniform read/write access possible
        int i = 1;
        for (MJVarDecl param : m.getFormalParameters()) {
            TemporaryVar v = Ast.TemporaryVar(param.getName());
            addInstruction(Ast.Alloca(v, translateType(param.getType())));
            addInstruction(Ast.Store(Ast.VarRef(v), Ast.VarRef(proc.getParameters().get(i))));
            localVarLocation.put(param, v);
            i++;
        }

        // allocate space for the local variables
        allocaLocalVars(m.getMethodBody());

        translateStmt(m.getMethodBody());

    }

    void allocaLocalVars(MJBlock methodBody) {
        methodBody.accept(new MJElement.DefaultVisitor() {
            @Override
            public void visit(MJVarDecl localVar) {
                super.visit(localVar);
                TemporaryVar v = Ast.TemporaryVar(localVar.getName());
                addInstruction(Ast.Alloca(v, translateType(localVar.getType())));
                localVarLocation.put(localVar, v);
            }
        });
    }

    private void translateMainMethod() {
        Proc mainProc = Ast.Proc("main", Ast.TypeInt(), Ast.ParameterList(), Ast.BasicBlockList());
        addProcedure(mainProc);
        setCurrentProc(mainProc);
        BasicBlock initBlock = newBasicBlock("init");
        addBasicBlock(initBlock);
        setCurrentBlock(initBlock);

        MJBlock mainBody = javaProg.getMainClass().getMainBody();
        allocaLocalVars(mainBody);
        translateStmt(mainBody);
        // add a return statement to the end of the main method
        currentBlock.add(Ast.ReturnExpr(Ast.ConstInt(0)));
    }


    public BasicBlock newBasicBlock(String name) {
        BasicBlock block = Ast.BasicBlock();
        block.setName(name);
        return block;
    }

    void addBasicBlock(BasicBlock block) {
        currentProcedure.getBasicBlocks().add(block);
    }

    public BasicBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(BasicBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    public void translateStmt(MJStatement s) {
        addInstruction(Ast.CommentInstr(sourceLine(s) + " start statement : " + printFirstline(s)));
        s.match(stmtTranslator);
        addInstruction(Ast.CommentInstr(sourceLine(s) + " end statement: " + printFirstline(s)));
    }

    private String printFirstline(MJStatement s) {
        String str = AstPrinter.print(s);
        str = str.replaceAll("\n.*", "");
        return str;
    }

    public Operand exprLvalue(MJExpr e) {
        return e.match(exprLValue);
    }

    public Operand exprRvalue(MJExpr e) {
        return e.match(exprRValue);
    }

    /**
     * adds an instruction to the end of the current basic block
     */
    public void addInstruction(Instruction instruction) {
        currentBlock.add(instruction);
    }

    public BasicBlock unreachableBlock() {
        return Ast.BasicBlock();
    }

    public Parameter getThisParameter() {
        // in our case 'this' is always the first parameter
        return currentProcedure.getParameters().get(0);
    }

    public Type getArrayPointerType() {
        return arrayPointerType;
    }

    public int sourceLine(MJElement e) {
        while (e != null) {
            if (e.getSourcePosition() != null) {
                return e.getSourcePosition().getLine();
            }
            e = e.getParent();
        }
        return 0;
    }

    public void addNullcheck(Operand arrayAddr, String errorMessage) {
        TemporaryVar isNull = Ast.TemporaryVar("isNull");
        addInstruction(Ast.BinaryOperation(isNull, (Operand) arrayAddr.copy(), Ast.Eq(), Ast.Nullpointer()));

        BasicBlock whenIsNull = newBasicBlock("whenIsNull");
        BasicBlock notNull = newBasicBlock("notNull");
        currentBlock.add(Ast.Branch(Ast.VarRef(isNull), whenIsNull, notNull));

        addBasicBlock(whenIsNull);
        whenIsNull.add(Ast.HaltWithError(errorMessage));

        addBasicBlock(notNull);
        setCurrentBlock(notNull);
    }


    public Operand getArrayLen(Operand arrayAddr) {
        TemporaryVar addr = Ast.TemporaryVar("length_addr");
        addInstruction(Ast.GetElementPtr(addr, (Operand) arrayAddr.copy(), Ast.OperandList(Ast.ConstInt(0), Ast.ConstInt(0))));
        TemporaryVar len = Ast.TemporaryVar("len");
        addInstruction(Ast.Load(len, Ast.VarRef(addr)));
        return Ast.VarRef(len);
    }

    public Type getPointerToClassStruct(MJClassDecl classDeclaration) {
        return Ast.TypePointer(classTranslator.getStructTypeFor(classDeclaration));
    }

    public Operand getConstructorProcRef(MJClassDecl classDeclaration) {
        return Ast.ProcedureRef(classTranslator.getConstructorProc(classDeclaration));
    }

    public Type translateType(MJType type) {
        return type.match(new MJType.Matcher<Type>() {
            @Override
            public Type case_TypeInt(MJTypeInt typeInt) {
                return Ast.TypeInt();
            }

            @Override
            public Type case_TypeBool(MJTypeBool typeBool) {
                return Ast.TypeBool();
            }

            @Override
            public Type case_TypeIntArray(MJTypeIntArray typeIntArray) {
                return arrayPointerType;
            }

            @Override
            public Type case_TypeClass(MJTypeClass typeClass) {
                return Ast.TypePointer(classTranslator.getStructTypeFor(typeClass.getClassDeclaration()));
            }
        });
    }

    public void addStructType(TypeStruct structType) {
        prog.getStructTypes().add(structType);
    }

    public void setCurrentProc(Proc currentProc) {
        if (currentProc == null) {
            throw new RuntimeException("Cannot set proc to null");
        }
        this.currentProcedure = currentProc;
    }

    public void addGlobal(Global global) {
        prog.getGlobals().add(global);
    }

    public void addProcedure(Proc proc) {
        prog.getProcedures().add(proc);
    }

    public ClassTranslator getClassTranslator() {
        return classTranslator;
    }

    public boolean isField(MJVarDecl varDecl) {
        // this -> fieldList -> class
        return varDecl.getParent().getParent() instanceof MJClassDecl;
    }

    public boolean isParameter(MJVarDecl varDecl) {
        // this -> paramList -> method
        return varDecl.getParent().getParent() instanceof MJMethodDecl;
    }

    public TemporaryVar getLocalVarLocation(MJVarDecl varDecl) {
        return localVarLocation.get(varDecl);
    }

    public Operand getNewIntArrayFunc() {
        return Ast.ProcedureRef(newIntArrayFunc);
    }

    public Operand addCastIfNecessary(Operand value, Type expectedType) {
        if (expectedType.equalsType(value.calculateType())) {
            return value;
        }
        TemporaryVar castValue = Ast.TemporaryVar("castValue");
        addInstruction(Ast.Bitcast(castValue, expectedType, value));
        return Ast.VarRef(castValue);
    }

    public Type getCurrentReturnType() {
        return currentProcedure.getReturnType();
    }
}
