package translation;

import minijava.ast.*;
import minillvm.ast.*;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassTranslator {

    Translator tr;
    private MJProgram prog;
    private Map<MJClassDecl, TypeStruct> classStructType = new HashMap<>();
    private Map<MJClassDecl, TypeStruct> classVtableType = new HashMap<>();
    private Map<MJClassDecl, Proc> classConstructorProc = new HashMap<>();
    private Map<MJVarDecl, Integer> fieldIndex = new HashMap<>();
    private Map<MJMethodDecl, Integer> methodIndex = new HashMap<>();
    private Map<MJMethodDecl, Proc> methodImpl = new HashMap<>();
    private Map<MJClassDecl, Global> vtableConstants = new HashMap<>();


    ClassTranslator(Translator tr) {
        this.tr = tr;

    }

    void translate(MJProgram prog) {
        this.prog = prog;
        for (MJClassDecl c : prog.getClassDecls()) {
            initClassTypes(c);
        }
        for (MJClassDecl c : prog.getClassDecls()) {
            initMethods(c);
        }
        for (MJClassDecl c : prog.getClassDecls()) {
            translateClass(c);
        }
    }



    private void initClassTypes(MJClassDecl c) {
        TypeStruct structType = Ast.TypeStruct(c.getName(), Ast.StructFieldList());
        classStructType.put(c, structType);
        tr.addStructType(structType);

        TypeStruct structVtable = Ast.TypeStruct(c.getName() + "_vtable", Ast.StructFieldList());
        classVtableType.put(c, structVtable);
        tr.addStructType(structVtable);
    }

    private void translateClass(MJClassDecl c) {
        TypeStruct vtableStruct = createVtable(c);
        createVtableConstant(c, vtableStruct);
        createStructType(c, vtableStruct);
        createConstructorProc(c);
    }

    private void createVtableConstant(MJClassDecl c, TypeStruct vtableStruct) {
        ConstList methodPointers = Ast.ConstList();
        addMethods(c, methodPointers);

        ConstStruct value = Ast.ConstStruct(vtableStruct, methodPointers);
        Global global = Ast.Global(vtableStruct, "vtable_" + c.getName(), true, value);
        tr.addGlobal(global);
        vtableConstants.put(c, global);
    }

    private void initMethods(MJClassDecl c) {
        for (MJMethodDecl m : c.getMethods()) {
            Type returnType = tr.translateType(m.getReturnType());
            ParameterList params = m.getFormalParameters()
                    .stream()
                    .map(p -> Ast.Parameter(tr.translateType(p.getType()), p.getName()))
                    .collect(Collectors.toCollection(() -> Ast.ParameterList()));
            Parameter thisVar = Ast.Parameter(Ast.TypePointer(classStructType.get(c)), "this");
            params.add(0, thisVar);
            Proc proc = Ast.Proc(c.getName() + "_" + m.getName(), returnType, params, Ast.BasicBlockList());
            tr.addProcedure(proc);
            methodImpl.put(m, proc);
        }
    }

    private void addMethods(MJClassDecl c, ConstList methodPointers) {
        if (c.getDirectSuperClass() != null) {
            addMethods(c.getDirectSuperClass(), methodPointers);
        }
        for (MJMethodDecl m : c.getMethods()) {
            int index = methodIndex.get(m);
            Proc proc = methodImpl.get(m);
            Const methodPointer = Ast.ProcedureRef(proc);
            if (index < methodPointers.size()) {
                methodPointers.set(index, methodPointer);
            } else {
                methodPointers.add(methodPointer);
            }
        }
    }

    private void createStructType(MJClassDecl c, TypeStruct vtableStruct) {
        TypeStruct structType = classStructType.get(c);
        StructFieldList fieldTypes = structType.getFields();

        // first add the pointer to the vtable
        fieldTypes.add(Ast.StructField(Ast.TypePointer(vtableStruct), "vtable"));

        // then add all the fields
        addFields(c, fieldTypes);


    }

    private void addFields(MJClassDecl c, StructFieldList fieldTypes) {
        if (c.getDirectSuperClass() != null) {
            addFields(c.getDirectSuperClass(), fieldTypes);
        }
        for (MJVarDecl field : c.getFields()) {
            fieldIndex.put(field, fieldTypes.size());
            // TODO add comment for the name
            fieldTypes.add(Ast.StructField(tr.translateType(field.getType()), c.getName() + "_" + field.getName()));
        }
    }

    private void createConstructorProc(MJClassDecl c) {
        BasicBlockList basicBlocks = Ast.BasicBlockList();
        TypeStruct structType = classStructType.get(c);
        Proc proc = Ast.Proc("construct_" + c.getName(), Ast.TypePointer(structType), Ast.ParameterList(), basicBlocks);
        tr.addProcedure(proc);
        classConstructorProc.put(c, proc);

        tr.setCurrentProc(proc);

        BasicBlock init = tr.newBasicBlock("init");
        tr.addBasicBlock(init);
        tr.setCurrentBlock(init);


        TemporaryVar allocRes = Ast.TemporaryVar("allocRes");
        tr.addInstruction(Ast.Alloc(allocRes, Ast.Sizeof(structType)));

        // bitcast to struct type
        TemporaryVar res = Ast.TemporaryVar("res");
        tr.addInstruction(Ast.Bitcast(res, Ast.TypePointer(structType), Ast.VarRef(allocRes)));

        // initialize the fields:
        initializeFields(res, c);

        // initialize the pointer to the vtable
        Operand vtablePointerAddr = getVtablePointerAddr(Ast.VarRef(res), c);
        tr.addInstruction(Ast.Store(vtablePointerAddr, Ast.GlobalRef(vtableConstants.get(c))));

        // return res
        tr.getCurrentBlock().add(Ast.ReturnExpr(Ast.VarRef(res)));
    }



    /** sets all the fields of the given class to a default value, using `res` as the pointer to the struct*/
    private void initializeFields(Variable res, MJClassDecl c) {
        if (c.getDirectSuperClass() != null) {
            initializeFields(res, c.getDirectSuperClass());
        }
        for (MJVarDecl field : c.getFields()) {
            // res.field = 0;
            Operand fieldAddr = getFieldAddress(Ast.VarRef(res), field);
            tr.addInstruction(Ast.Store(fieldAddr, DefaultValue.get(field.getType())));
        }
    }



    private TypeStruct createVtable(MJClassDecl c) {
        TypeStruct structType = classVtableType.get(c);
        StructFieldList fieldTypes = structType.getFields();

        // then add all the fields
        addMethodsToVtable(c, fieldTypes, new HashMap<>());

        return structType;
    }

    private void addMethodsToVtable(MJClassDecl c, StructFieldList fieldTypes, Map<String, Integer> methodPos) {
        if (c.getDirectSuperClass() != null) {
            addMethodsToVtable(c.getDirectSuperClass(), fieldTypes, methodPos);
        }
        for (MJMethodDecl m : c.getMethods()) {
            TypePointer procType = Ast.TypePointer(translateMethodType(c, m));
            StructField structField = Ast.StructField(procType, m.getName());
            if (methodPos.containsKey(m.getName())) {
                int index = methodPos.get(m.getName());
                fieldTypes.set(index, structField);
            } else {
                // add new method to vtable
                methodPos.put(m.getName(), fieldTypes.size());
                fieldTypes.add(structField);
            }
            // remember the method index
            methodIndex.put(m, methodPos.get(m.getName()));
        }
    }

    private TypeProc translateMethodType(MJClassDecl c, MJMethodDecl m) {
        TypeRefList argTypes = Ast.TypeRefList();
        argTypes.add(Ast.TypePointer(classStructType.get(c)));
        for (MJVarDecl p : m.getFormalParameters()) {
            argTypes.add(tr.translateType(p.getType()));
        }
        Type resType = tr.translateType(m.getReturnType());
        return Ast.TypeProc(argTypes, resType);
    }


    /** returns the address to the class struct, where the pointer to the vtable is stored */
    Operand getVtablePointerAddr(Operand objAddress, MJClassDecl c) {
        TemporaryVar res = Ast.TemporaryVar("vtable_addr");
        tr.addInstruction(Ast.GetElementPtr(res, objAddress.copy(), Ast.OperandList(Ast.ConstInt(0), Ast.ConstInt(0))));
        return Ast.VarRef(res);
    }

    /** returns a pointer to the vtable, given a pointer to the object */
    Operand loadProcFromVtable(Operand objAddress, MJClassDecl c, MJMethodDecl m) {
        Operand vtablePointerAddr = getVtablePointerAddr(objAddress, c);
        TemporaryVar vtablePointer = Ast.TemporaryVar("vtable");
        tr.addInstruction(Ast.Load(vtablePointer, vtablePointerAddr));
        int index = methodIndex.get(m);
        TemporaryVar procAddressPointer = Ast.TemporaryVar("procAddressPointer");
        tr.addInstruction(Ast.GetElementPtr(procAddressPointer, Ast.VarRef(vtablePointer), Ast.OperandList(Ast.ConstInt(0), Ast.ConstInt(index))));

        TemporaryVar procAddress = Ast.TemporaryVar("procAddress");
        tr.addInstruction(Ast.Load(procAddress, Ast.VarRef(procAddressPointer)));

        return Ast.VarRef(procAddress);
    }

    /** returns a pointer to a specific field, given a pointer to the object */
    Operand getFieldAddress(Operand objAddress, MJVarDecl field) {
        int index = fieldIndex.get(field);
        TemporaryVar addr = Ast.TemporaryVar("addr_" + field.getName());
        tr.addInstruction(Ast.GetElementPtr(addr, objAddress, Ast.OperandList(Ast.ConstInt(0), Ast.ConstInt(index))));
        return Ast.VarRef(addr);
    }

    public Operand loadProcFromVtable(Operand receiver, MJMethodDecl methodDeclaration) {
        return loadProcFromVtable(receiver, (MJClassDecl) methodDeclaration.getParent().getParent(), methodDeclaration);
    }



    Proc getProcImpl(MJMethodDecl m) {
        return methodImpl.get(m);
    }

    public Type getStructTypeFor(MJClassDecl classDeclaration) {
        if (classDeclaration == null) {
            throw new RuntimeException("ClassDeclaration must not be null");
        } else if (!classStructType.containsKey(classDeclaration)) {
            throw new RuntimeException("No entry for class " + classDeclaration.getName());
        }
        return classStructType.get(classDeclaration);
    }

    public Proc getConstructorProc(MJClassDecl classDeclaration) {
        return classConstructorProc.get(classDeclaration);
    }
}
