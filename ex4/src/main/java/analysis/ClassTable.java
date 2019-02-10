package analysis;

import minijava.ast.*;

import java.util.*;
import java.util.stream.Collectors;

public class ClassTable {
    private Map<String, ClassType> classTypes = new LinkedHashMap<>();
    /** transitive, reflexive supertypes */
    private Map<MJClassDecl, List<MJClassDecl>> superTypes = new HashMap<>();


    private Analysis analysis;

    ClassTable(Analysis analysis, MJProgram prog) {
        this.analysis = analysis;
        for (MJClassDecl c : prog.getClassDecls()) {
            String mainClassName = prog.getMainClass().getName();
            if (c.getName().equals(mainClassName)) {
                analysis.addError(c, "Class must not be name as main class.");
            }
            ClassType old = classTypes.put(c.getName(), new ClassType(this, c));
            if (old != null) {
                analysis.addError(c, "There already is a class with name " + c.getName() + " defined in " + old.getClassDecl().getSourcePosition() + ".");
            }
        }
        findDirectSuperClasses(prog);
        buildSuperTypes(prog);
    }

    private void buildSuperTypes(MJProgram prog) {
        for (MJClassDecl c : prog.getClassDecls()) {
            List<MJClassDecl> superTypes = new ArrayList<>();
            MJClassDecl d = c;
            while (d != null) {
                if (superTypes.contains(d)) {
                    analysis.addError(c, "There is a cycle in the type hierarchy: "
                        + superTypes.stream().map(MJClassDecl::getName).collect(Collectors.joining(" < "))
                        + " < " + d);
                    break;
                }
                superTypes.add(d);
                d = d.getDirectSuperClass();
            }
            this.superTypes.put(c, superTypes);
        }
    }


    private void findDirectSuperClasses(MJProgram prog) {
        for (MJClassDecl c : prog.getClassDecls()) {
            if (c.getExtended() instanceof MJExtendsClass) {
                String extendedClassName = ((MJExtendsClass) c.getExtended()).getName();
                if (classTypes.containsKey(extendedClassName)) {
                    c.setDirectSuperClass(classTypes.get(extendedClassName).getClassDecl());
                } else {
                    analysis.addError(c.getExtended(), "Class " + extendedClassName + " does not exist.");
                }
            }
        }
    }


    public boolean isSubclass(MJClassDecl c, MJClassDecl d) {
        return superTypes.get(c).contains(d);
    }

    public ClassType lookupClass(String name) {
        return classTypes.get(name);
    }

    public MJVarDecl lookupField(Type t, String fieldName) {
        if (t instanceof ClassType) {
            ClassType ct = (ClassType) t;
            for (MJClassDecl c : superTypes.get(ct.getClassDecl())) {
                for (MJVarDecl v : c.getFields()) {
                    if (v.getName().equals(fieldName)) {
                        return v;
                    }
                }
            }

        }
        return null;
    }

    public MJMethodDecl lookupMethod(Type t, String methodName) {
        if (t instanceof ClassType) {
            ClassType ct = (ClassType) t;
            for (MJClassDecl c : superTypes.get(ct.getClassDecl())) {
                for (MJMethodDecl m : c.getMethods()) {
                    if (m.getName().equals(methodName)) {
                        return m;
                    }
                }
            }

        }
        return null;
    }

    public ClassType getDirectSuperClassType(MJClassDecl c) {
        MJClassDecl cd = c.getDirectSuperClass();
        if (cd == null) {
            return null;
        }
        return classTypes.get(cd.getName());
    }

    public List<MJVarDecl> getFields(MJClassDecl c) {
        List<MJVarDecl> result = new ArrayList<>();
        List<MJClassDecl> superClasses = superTypes.get(c);
        for (int i=superClasses.size()-1; i>=0; i--) {
            result.addAll(superClasses.get(i).getFields());
        }
        return result;
    }
}
