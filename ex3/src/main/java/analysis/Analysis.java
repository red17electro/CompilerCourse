package analysis;

import minijava.ast.*;

import java.util.*;

/*
*   This class handles names analysis and type analysis
*
 */
public class Analysis {
    /*
    * keep reference to the MJProgram
    */
    private final MJProgram prog;
    private List<TypeError> typeErrors = new ArrayList<>();

    /*
     * This will keep all the info about class and its super class
     */
    Map<MJClassDecl, MJExtended> classesInfo = new HashMap<>();

    public void addError(MJElement element, String message) {
        typeErrors.add(new TypeError(element, message));
    }

    public Analysis(MJProgram prog) {
        this.prog = prog;
    }

    public void nameAnalysis() {


        MJClassDeclList classList = this.prog.getClassDecls();

        MJMainClass main = prog.getMainClass();

        NameAnalysisVisitor nameAnalysisVisitor = new NameAnalysisVisitor(typeErrors);

        prog.accept(nameAnalysisVisitor);
        nameAnalysisVisitor.processNameChecking(classList, main);

        MJClassDecl classNAResult = nameAnalysisVisitor.getClassNAResult();
        MJMethodDecl methodNAResult = nameAnalysisVisitor.getMethodNAResult();
        MJVarDecl fieldNAResult = nameAnalysisVisitor.getFiledNAResult();
        MJVarDecl varNAResult = nameAnalysisVisitor.getVarNAResult();
        MJExtendsClass classCyclesResult = nameAnalysisVisitor.getClassCyclesResult();
        MJExtendsClass classMissingDeclResult = nameAnalysisVisitor.getMissingDeclarationsResult();

        if (classNAResult != null) {
            addError(classNAResult, "The duplicate class has been declared!");
            return;
        }

        if (classCyclesResult != null) {
            addError(classCyclesResult, "There is a cycle in the class inheritance!!!");
            return;
        }

        if (classMissingDeclResult != null) {
            addError(classMissingDeclResult, "One or more inherited classes were not declared!!!");
            return;
        }

        if (methodNAResult != null) {
            addError(methodNAResult, "The duplicate method has been declared!");
            return;
        }

        if (fieldNAResult != null) {
            addError(fieldNAResult, "The duplicate field has been declared!!!");
            return;
        }

        if (varNAResult != null) {
            addError(varNAResult, "The duplicate local variables has been declared!!!");
        }

        fillClassInfo(classList);
    }

    /**
     * this method call name analysis and type analysis methods
     */
    public void check() {
        nameAnalysis();
        typeAnalysis(classesInfo);
    }

    /**
     * @param classDeclList class declaration list
     * this method will add the sub classes and super classes information to the classesInfo structure
     */
    private void fillClassInfo(MJClassDeclList classDeclList) {
        for (MJClassDecl classObj : classDeclList) {
            MJExtended extendsClass = classObj.getExtended();

            if (classesInfo.containsKey(classObj)) {
                continue;
            }

            classesInfo.put(classObj, extendsClass);
        }
    }

    /**
     * @param classesInfo Map type object with sub classes and super classes information
     */
    private void typeAnalysis(Map<MJClassDecl, MJExtended> classesInfo) {
        SymbolTable symbolTable = new SymbolTable();
        TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(symbolTable, classesInfo, typeErrors);
        checkClassOveriding(this.prog.getClassDecls(), classesInfo);
        prog.accept(typeCheckVisitor);
    }

    /**
     * @param classDeclList class declaration list
     * @param classesInfo Map type object with sub classes and super classes information
     * This method handles the check for overriding between all the classes
     */
    private void checkClassOveriding(MJClassDeclList classDeclList, Map<MJClassDecl, MJExtended> classesInfo) {
        Map<String, List<MJVarDecl>> methodInfo = new HashMap<>();

        for (MJClassDecl classObj : classDeclList) {
            MJExtended extendsClass = classObj.getExtended();

            if (!(extendsClass instanceof MJExtendsNothing)) {
                MJMethodDeclList methodDeclsList = classObj.getMethods();

                for (MJMethodDecl methodDecl : methodDeclsList) {
                    String extendedName = ((MJExtendsClass) extendsClass).getName();
                    MJClassDecl extendedClass = null;
                    for (MJClassDecl classDecl : classDeclList) {
                        if (classDecl.getName().equals(extendedName)) {
                            extendedClass = classDecl;
                            break;
                        }
                    }

                    MJMethodDeclList extdClassMethods = extendedClass.getMethods();

                    for (MJMethodDecl methodDecl1 : extdClassMethods) {
                        if (methodDecl.getName().equals(methodDecl1.getName())) {
                            boolean result = false;
                            MJType leftType = methodDecl.getReturnType();
                            MJType rightType = methodDecl1.getReturnType();
                            Type left = new Type(leftType);
                            Type right = new Type(rightType);

                            if ((left.getType() == TypeChecker.CLASS) && (right.getType() == TypeChecker.CLASS)) {
                                result = TypeChecker.isSubType(((MJTypeClass) leftType).getName(), ((MJTypeClass) rightType).getName(), classesInfo);
                            } else {
                                result = TypeChecker.isSubType(left, right);
                            }

                            if (result) {
                                MJVarDeclList parametersList = methodDecl.getFormalParameters();
                                MJVarDeclList parametersListSupMethod = methodDecl1.getFormalParameters();

                                if (parametersList.size() != parametersListSupMethod.size()) {
                                    typeErrors.add(new TypeError(parametersList, "In overriding number of parameters should be the same!"));
                                } else {
                                    for (int k = 0; k < parametersList.size(); k++) {
                                        MJType parameterLeft = parametersList.get(k).getType();
                                        MJType parameterRight = parametersListSupMethod.get(k).getType();
                                        Type leftParamType = new Type(parameterLeft);
                                        Type rightParamType = new Type(parameterRight);

                                        if ((leftParamType.getType() == TypeChecker.CLASS) && (rightParamType.getType() == TypeChecker.CLASS)) {
                                            result = ((MJTypeClass) parameterLeft).getName().equals(((MJTypeClass) parameterRight).getName());
                                        } else {
                                            result = TypeChecker.isSubType(leftParamType, rightParamType);
                                        }

                                        if (!result) {
                                            typeErrors.add(new TypeError(parametersList, "In overriding corrosponding types should be the same!"));
                                        }
                                    }
                                }

                            } else {
                                typeErrors.add(new TypeError(methodDecl.getReturnType(), "Return types of overriding methods should be the same!"));
                            }
                        } else {
                            typeErrors.add(new TypeError(methodDecl, "Overriding methods names should be the same!"));
                        }
                    }
                }
            }
        }
    }

    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}