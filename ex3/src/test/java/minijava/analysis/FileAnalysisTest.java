package minijava.analysis;

import analysis.Analysis;
import analysis.TypeError;
import frontend.MJFrontend;
import frontend.SyntaxError;
import minijava.ast.MJProgram;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.fail;


/**
 * Runs the Parser on files from the folder testdata/parser.
 * <p>
 * Files from testdata/parser/ok are expected to produce no syntax error.
 * Files from testdata/parser/error are expected to contain syntax errors.
 */
@RunWith(Parameterized.class)
public class FileAnalysisTest {

    /**
     * The text file to parseFile.
     */
    private final File inputFile;
    /**
     * True iff this is a test case in which you believe the input file to have
     * correct syntax. False if you expect a parseFile error.
     */
    private final boolean parseSuccessMeansTestPasses;

    /**
     * Create a new test case (to be called by JUnit, not by you)
     *
     * @param inputFile                   the file to parseFile
     * @param parseSuccessMeansTestPasses true iff you expect the parser to accept it
     */
    public FileAnalysisTest(File inputFile, boolean parseSuccessMeansTestPasses) {
        this.inputFile = inputFile;
        this.parseSuccessMeansTestPasses = parseSuccessMeansTestPasses;
    }

    /**
     * The only test method of this test class: run a fresh parser instance on
     * the input file, and judge the result according to the
     * {@link #parseSuccessMeansTestPasses} parameter: parseFile error becomes test
     * failure iff parseSuccessMeansTestPasses is true.
     *
     * @throws IOException if file IO goes wrong (shouldn't; but want to know when)
     */
    @Test
    public void testChecker() throws Exception {
        File file = inputFile;
        MJFrontend frontend = new MJFrontend();
        MJProgram program = frontend.parseFile(file);
        if (!frontend.getSyntaxErrors().isEmpty()) {
            SyntaxError syntaxError = frontend.getSyntaxErrors().get(0);
            fail("Unexpected syntax error in (" + file.getName() + ":" + syntaxError.getLine() + ")\n" + syntaxError.getMessage());
        }

        Analysis analysis = new Analysis(program);
        analysis.check();


        if (!parseSuccessMeansTestPasses && analysis.getTypeErrors().isEmpty()) {
            fail("Parser accepted (" + file.getName() + ":1), but should reject it.");
        }
        if (parseSuccessMeansTestPasses && !analysis.getTypeErrors().isEmpty()) {
            TypeError typeError = analysis.getTypeErrors().get(0);
            System.err.println("Type error in ... (" + file.getName() + ":" + typeError.getLine() + ")");
            throw typeError;
        }

    }


    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> ctorParams = new ArrayList<Object[]>();

        appendTestCasesFromDir(new File("testdata/typechecker/ok"), true, ctorParams);
        appendTestCasesFromDir(new File("testdata/typechecker/error"), false, ctorParams);
        return ctorParams;
    }


    /**
     * Helper function to fill a list of constructor args with data gained from
     * file names in a directory
     *
     * @param testDataDir the directory whose files should be parsed
     * @param isOkTest    true iff you expect parseFile OK
     * @param ctorParams  the growing list to be one day returned by {@link #data()}
     */
    private static void appendTestCasesFromDir(File testDataDir,
                                               boolean isOkTest, ArrayList<Object[]> ctorParams) {
        if (testDataDir.exists()) {
            if (testDataDir.isDirectory()) {
                File[] files = testDataDir.listFiles();
                for (File f : files) {
                    if (f.isFile()) {
                        ctorParams.add(new Object[]{f, isOkTest});
                    }
                }
            } else {
                // yes, RuntimeException is dirty technique, but this is just
                // a simple test case.
                throw new RuntimeException(String.format(
                        "Cannot derive parser test from %s: not a directory",
                        testDataDir.getAbsolutePath()));
            }
        }
    }
}
