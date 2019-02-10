package analysis;

import minijava.ast.MJClassDecl;

public class ClassType extends Type {
    private ClassTable classTable;
    private MJClassDecl classDecl;

    public ClassType(ClassTable classTable, MJClassDecl classDecl) {
        this.classTable = classTable;
        this.classDecl = classDecl;
    }

    @Override
    boolean isSubtypeOf(Type other) {
        if (other instanceof ClassType) {
            ClassType ct = (ClassType) other;
            return classTable.isSubclass(classDecl, ct.classDecl);
        }
        return other == ANY;
    }

    public MJClassDecl getClassDecl() {
        return classDecl;
    }

    @Override
    public String toString() {
        return classDecl.getName();
    }
}
