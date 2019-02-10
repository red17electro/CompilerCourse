package analysis;

import minijava.ast.MJVarDecl;

public interface TypeContext {
	Type getReturnType();

	Type getThisType();

	void setThisType(Type thisType);

	void setReturnType(Type returnType);

	VarRef lookupVar(String varUse);

	void putVar(String varName, Type type, MJVarDecl var);

	TypeContext copy();


	static class VarRef {
		final MJVarDecl decl;
		final Type type;

		public VarRef(Type type, MJVarDecl decl) {
			this.decl = decl;
			this.type = type;
		}
	}
}
