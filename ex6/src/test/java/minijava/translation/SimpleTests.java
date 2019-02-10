package minijava.translation;


import minijava.TranslationTestHelper;
import org.junit.Test;

public class SimpleTests {

	@Test
	public void println() throws Exception {
		testStatements(
				"System.out.println(42);"
		);
	}

	@Test
	public void test0() throws Exception {
		testStatements(
				"System.out.println(42 * 7 + 3);"
		);
	}

	@Test
	public void test1() throws Exception {
		testStatements(
				"int x;",
				"x = 42;",
				"x = x + 1;",
				"System.out.println(x);"
		);
	}


	@Test
	public void test2() throws Exception {
		testStatements(
				"int x;",
				"x = 42;",
				"while (0 < x) {",
				"	System.out.println(x);",
				"	x = x - 1;",
				"}"
		);
	}

	private void testStatements(String...inputLines) throws Exception {
		String input = "class Main { public static void main(String[] args) {\n"
				+ String.join("\n", inputLines)
				+ "\n}}\n";
		TranslationTestHelper.testLLVMTranslation("Test.java", input);
	}


}
