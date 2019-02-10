package main;

import analysis.Analysis;
import analysis.TypeError;
import frontend.MJFrontend;
import frontend.SyntaxError;
import minijava.ast.MJProgram;
import minillvm.ast.Prog;
import translation.Translator;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class MiniJavaCompiler {


    private MJProgram javaProgram;
    private Prog llvmProg;
    private Analysis analysis;
    private MJFrontend frontend;

    public void compileFile(File file) throws Exception {
        try (FileReader r = new FileReader(file)) {
            compile(file.getPath(), r);
        }

    }

    public void compileString(String inputName, String input) throws Exception {
        compile(inputName, new StringReader(input));
    }

    public void compile(String inputName, Reader input) throws Exception {
        frontend = new MJFrontend();
        javaProgram = frontend.parse(input);
        if (!frontend.getSyntaxErrors().isEmpty()) {
            return;
        }

        // typecheck
        analysis = new Analysis(javaProgram);
        analysis.check();
        if (!analysis.getTypeErrors().isEmpty()) {
            return;
        }

        // translate
        // TODO you can pass analysis results to your translator here:
        Translator translator = new Translator(javaProgram);
        llvmProg = translator.translate();

    }

    public MJProgram getJavaProgram() {
        return javaProgram;
    }

    public Prog getLlvmProg() {
        return llvmProg;
    }

    public List<SyntaxError> getSyntaxErrors() {
        return frontend.getSyntaxErrors();
    }

    public List<TypeError> getTypeErrors() {
        if (analysis == null) {
            return Collections.emptyList();
        }
        return analysis.getTypeErrors();
    }
}
