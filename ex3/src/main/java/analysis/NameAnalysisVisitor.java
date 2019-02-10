package analysis;

import minijava.ast.*;

import java.util.*;

/**
 * Created by Server on 5/28/2017.
 */
public class NameAnalysisVisitor extends MJElement.DefaultVisitor {

    private Map<Integer, List<String>> history = new HashMap<>();
    private List<String> currentScope = null;
    private int count = 0;
    private List<TypeError> typeErrors;
    private Map<String, MJExtended> classesInfo = new HashMap<>();
    private Map<String, List<String>> classHistory = new HashMap<>();
    private MJClassDecl classNAResult = null;
    private MJExtendsClass classCyclesResult = null;
    private MJExtendsClass missingDeclarationsResult = null;
    private MJMethodDecl methodNAResult = null;
    private MJVarDecl fieldNAResult = null;
    private MJVarDecl localVarNAResult = null;

    /**
     * @return MJClassDecl object
     */
    MJClassDecl getClassNAResult() {
        return this.classNAResult;
    }

    /**
     * @return MJMethodDecl object
     */
    MJMethodDecl getMethodNAResult() {
        return this.methodNAResult;
    }

    /**
     * @return MJVarDecl object
     */
    MJVarDecl getFiledNAResult() {
        return this.fieldNAResult;
    }

    /**
     * @return MJVarDecl object
     */
    MJVarDecl getVarNAResult() {
        return this.localVarNAResult;
    }

    /**
     * @return MJExtendsClass object
     */
    MJExtendsClass getClassCyclesResult() {
        return this.classCyclesResult;
    }

    /**
     * @return MJExtendsClass object
     */
    MJExtendsClass getMissingDeclarationsResult() {
        return this.missingDeclarationsResult;
    }


    /**
     * @param classDeclList class declaration list
     * @param main Main class object
     */
    void processNameChecking(MJClassDeclList classDeclList, MJMainClass main) {
        initializeHistory();
        mainCLassChecker(main);
        classDuplicatesChecker(classDeclList, main);
        checkClassDeclarations(main);
        checkClassCycles();
    }


    /**
     * Initialize the classHistory
     */
    private void initializeHistory() {
        classHistory.put("class", new ArrayList<String>());
        classHistory.put("method", new ArrayList<String>());
        classHistory.put("field", new ArrayList<String>());
        classHistory.put("variable", new ArrayList<String>());
    }

    /**
     * @param main Main class object
     * Iterate through all the statements inside main class and add it to the "variable" list
     */
    private void mainCLassChecker(MJMainClass main) {
        String argsName = main.getArgsName();
        MJBlock varList = main.getMainBody();

        for (MJStatement statement : varList) {
            getVarDecl(statement, argsName);
        }

        classHistory.get("variable").clear();
    }

    /**
     * @param classDeclList class declaration list
     * @param mainClass Main class object
     * This method checks the duplicate classes
     */
    private void classDuplicatesChecker(MJClassDeclList classDeclList, MJMainClass mainClass) {
        for (MJClassDecl classObj : classDeclList) {
            MJExtended extendsClass = classObj.getExtended();
            if (classesInfo.containsKey(classObj.getName()) || classObj.getName().equals(mainClass.getName())) {
                classNAResult = classObj;
            }
            classesInfo.put(classObj.getName(), extendsClass);
            methodsDuplicatesChecker(classObj);
            fieldsDuplicatesChecker(classObj);
        }
    }

    /**
     * @param classObj Class object
     *  This method checks the duplicate names of the methods inside a class
     */
    private void methodsDuplicatesChecker(MJClassDecl classObj) {
        MJMethodDeclList methodDecls = classObj.getMethods();

        for (MJMethodDecl methodDecl : methodDecls) {
            if (classHistory.get("method").contains(methodDecl.getName())) {
                methodNAResult = methodDecl;
            }
            classHistory.get("method").add(methodDecl.getName());
            localVarsDuplicatesChecker(methodDecl);
        }
        classHistory.get("method").clear();

    }

    /**
     * @param classObj  Class object
     * This method checks the variable duplication of a class
     */
    private void fieldsDuplicatesChecker(MJClassDecl classObj) {
        MJVarDeclList fieldsList = classObj.getFields();

        for (MJVarDecl fieldDecl : fieldsList) {
            if (classHistory.get("field").contains(fieldDecl.getName())) {
                fieldNAResult = fieldDecl;
            }
            classHistory.get("field").add(fieldDecl.getName());
        }
        classHistory.get("field").clear();
    }

    /**
     * @param methodObj method object
     * This method checks variable duplication inside a method
     */
    private void localVarsDuplicatesChecker(MJMethodDecl methodObj) {
        MJBlock statementList = methodObj.getMethodBody();
        MJVarDeclList parametersList = methodObj.getFormalParameters();

        for (MJVarDecl parameter : parametersList) {
            String name = parameter.getName();
            if (classHistory.get("variable").contains(name)) {
                localVarNAResult = parameter;
            } else {
                classHistory.get("variable").add(name);
            }
        }
        for (MJStatement statement : statementList) {
            getVarDecl(statement, "");
        }
        classHistory.get("variable").clear();
    }

    /**
     * @param main Main class object
     * This method checks the extended class is already declared or not
     */
    private void checkClassDeclarations(MJMainClass main) {
        for (Map.Entry<String, MJExtended> entry : classesInfo.entrySet()) {
            if (!(entry.getValue() instanceof MJExtendsNothing)) {
                MJExtendsClass extdClass = (MJExtendsClass) entry.getValue();
                if (!classesInfo.containsKey(extdClass.getName())) {
                    missingDeclarationsResult = extdClass;
                } else if (extdClass.getName().equals(main.getName())) {
                    classNAResult = (MJClassDecl) extdClass;
                }
            }
        }
    }

    /**
     * This method checks the cycles of the classes (In inheritance)
     */
    private void checkClassCycles() {
        for (Map.Entry<String, MJExtended> entry : classesInfo.entrySet()) {
            if (!(entry.getValue() instanceof MJExtendsNothing)) {
                classHistory.get("class").clear();
                classCyclesResult = recursiveClassCyclingChecking(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @param className Name of the sub class
     * @param extendsClass extended class
     * @return MJExtendsClass return in the recursive call
     * This recursive method checks the class cycles in inheritance
     */
    private MJExtendsClass recursiveClassCyclingChecking(String className, MJExtended extendsClass) {
        if (extendsClass == null) {
            return null;
        }

        MJExtendsClass extdClass = (MJExtendsClass) extendsClass;
        String extendsClassName = extdClass.getName();
        if (classHistory.get("class").contains(extendsClassName)) {
            return extdClass;
        } else {
            if (classesInfo.get(extendsClassName) instanceof MJExtendsNothing) {
                return null;
            }
            classHistory.get("class").add(className);
            return recursiveClassCyclingChecking(extendsClassName, classesInfo.get(extendsClassName));
        }
    }

    /**
     * @param e statement object
     * @param argsName argument name of the main method
     */
    private void getVarDecl(MJStatement e, String argsName) {
        e.match(new MJStatement.MatcherVoid() {

            @Override
            public void case_StmtAssign(MJStmtAssign stmtAssign) {
            }

            @Override
            public void case_Block(MJBlock block) {
                for (MJStatement statement : block) {
                    statement.match(this);
                }
            }

            @Override
            public void case_StmtReturn(MJStmtReturn stmtReturn) {
            }

            @Override
            public void case_StmtPrint(MJStmtPrint stmtPrint) {

            }

            @Override
            public void case_StmtWhile(MJStmtWhile stmtWhile) {
                stmtWhile.getLoopBody().match(this);
            }

            @Override
            public void case_StmtExpr(MJStmtExpr stmtExpr) {
            }

            @Override
            public void case_VarDecl(MJVarDecl varDecl) {
                String name = varDecl.getName();

                if (/*classHistory.get("variable").contains(name) || */(!argsName.equals("") && argsName.equals(name))) {
                    localVarNAResult = varDecl;
                } else {
                    classHistory.get("variable").add(name);
                }
            }

            @Override
            public void case_StmtIf(MJStmtIf stmtIf) {
                stmtIf.getIfTrue().match(this);
                stmtIf.getIfFalse().match(this);
            }

        });
    }

    NameAnalysisVisitor(List<TypeError> typeErrors) {
        this.typeErrors = typeErrors;
    }

    /**
     * @param varDecl object from the class MJVarDecl
     */
    @Override
    public void visit(MJVarDecl varDecl) {
        if (history.size() != 0) {
            List<String> localScope = history.get(count - 1);

            if (currentScope != null && currentScope.contains(varDecl.getName())) {
                typeErrors.add(new TypeError(varDecl, "The variable is already declared!"));
            }

            if (localScope.contains(varDecl.getName())) {
                typeErrors.add(new TypeError(varDecl, "The variable is already declared!"));
            } else {
                localScope.add(varDecl.getName());
            }
        }
    }

    /**
     * @param block object from the class MJBlock
     */
    @Override
    public void visit(MJBlock block) {
        if (history.size() > 0) {
            currentScope = history.get(count - 1);
        }
        history.put(count, new ArrayList<>());
        count++;
        super.visit(block);
        history.remove(count - 1);
        count--;
    }

    /**
     * @param methodDecl object from the class MJMethodDecl
     */
    @Override
    public void visit(MJMethodDecl methodDecl) {
        history.put(count, new ArrayList<>());
        count++;
        super.visit(methodDecl);
        history.clear();
        count = 0;
        currentScope = null;
    }

    /**
     * @param stmtReturn object from the class MJStmtReturn
     */
    @Override
    public void visit(MJStmtReturn stmtReturn) {
        MJExpr expr = stmtReturn.getResult();
        if (expr instanceof MJVarUse) {
            String var = ((MJVarUse) expr).getVarName();

            if (!currentScope.contains(var) && !(history.get(0).contains(var))) {
                typeErrors.add(new TypeError(expr, "Cannot resolve symbol " + var));
            }
        }
    }

    /**
     * @param classDecl object from the class MJClassDecl
     */
    @Override
    public void visit(MJClassDecl classDecl) {
        history.put(count, new ArrayList<>());
        count++;
        super.visit(classDecl);
        history.clear();
        count = 0;
        currentScope = null;
    }
}
