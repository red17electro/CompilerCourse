package analysis;

import minijava.ast.MJVarDecl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeContextImpl implements TypeContext {
    private final Map<String, VarRef> env;
    private Type returnType;
    private Type thisType;

    public TypeContextImpl(Map<String, VarRef> env, Type returnType, Type thisType) {
        this.env = env;
        this.returnType = returnType;
        this.thisType = thisType;
    }

    public TypeContextImpl(Type returnType, Type thisType) {
        this.env = new HashMap<>();
        this.returnType = returnType;
        this.thisType = thisType;
    }

    public TypeContextImpl(Analysis analysis, List<MJVarDecl> vars, Type thisType) {
        this.returnType = Type.INVALID;
        this.thisType = thisType;
        this.env = new HashMap<>();
        for (MJVarDecl var : vars) {
            env.put(var.getName(), new VarRef(analysis.type(var.getType()), var));
        }
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Type getThisType() {
        return thisType;
    }

    @Override
    public VarRef lookupVar(String varUse) {
        return env.get(varUse);
    }

    @Override
    public void putVar(String varName, Type type, MJVarDecl var) {
        this.env.put(varName, new VarRef(type, var));
    }

    @Override
    public TypeContext copy() {
        return new TypeContextImpl(new HashMap<>(this.env), this.returnType, this.thisType);
    }

    @Override
    public void setThisType(Type thisType) {
        this.thisType = thisType;
    }
    @Override
    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

}
