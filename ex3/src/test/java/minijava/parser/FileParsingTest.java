package minijava.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import frontend.MJFrontend;
import minijava.ast.MJProgram;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;


/**
 * Runs the Parser on files from the folder testdata/parser.
 *
 * Files from testdata/parser/ok are expected to produce no syntax error.
 * Files from testdata/parser/error are expected to contain syntax errors.
 */
@RunWith(Parameterized.class)
public class FileParsingTest {

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
	public FileParsingTest(File inputFile, boolean parseSuccessMeansTestPasses) {
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
	public void testParser() throws Exception {
		File file = inputFile;
		MJFrontend frontend = new MJFrontend();
		MJProgram ast = frontend.parseFile(file);

		if (!parseSuccessMeansTestPasses && frontend.getSyntaxErrors().isEmpty()) {
			fail("Parser accepted " + file.getName() + ", but should reject it.");
		}
		if (parseSuccessMeansTestPasses && !frontend.getSyntaxErrors().isEmpty()) {
			fail(frontend.getSyntaxErrors().get(0).toString());
		}

	}


	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> ctorParams = new ArrayList<Object[]>();

		appendTestCasesFromDir(new File("testdata/parser/ok"), true, ctorParams);
		appendTestCasesFromDir(new File("testdata/parser/error"), false, ctorParams);
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
