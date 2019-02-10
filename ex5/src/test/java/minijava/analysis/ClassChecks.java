package minijava.analysis;


import analysis.Analysis;
import frontend.MJFrontend;
import frontend.SyntaxError;
import minijava.ast.MJProgram;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

/**
 * These tests are supposed to test part 1 and 3 of the exercise, but can be extended to also test other parts
 */
public class ClassChecks {

	@Test
	public void testOk1() {
		expectOk(
				"class Main { public static void main(String[] args) {} }",
				"class D {}",
				"class C extends D {}",
				"class B extends C {}",
				"class A extends B {}"
		);
	}

	@Test
	public void testOk2() {
		expectOk(
				"class Main { public static void main(String[] args) {} }",
				"class A extends B {}",
				"class B extends C {}",
				"class C extends D {}",
				"class D {}"
		);
	}

	@Test
	public void extendsNonexistingClass() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A extends B {}"
		);
	}

	@Test
	public void extendsMainClass() {
		expectTypeErrors(
				"class B { public static void main(String[] args) {} }",
				"class A extends B {}"
		);
	}

	@Test
	public void inheritanceCycle1() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A extends A{}"
		);
	}

	@Test
	public void inheritanceCycle2() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A extends B {}",
				"class B extends C {}",
				"class C extends A {}"
		);
	}

	@Test
	public void inheritanceCycle3() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A extends B {}",
				"class B extends C {}",
				"class C extends B {}"
		);
	}


	@Test
	public void duplicateClassName() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {}",
				"class A {}"
		);
	}

	@Test
	public void duplicateFieldName() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	boolean x;",
				"	int x;",
				"}"
		);
	}

	@Test
	public void duplicateMethodName() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	int f() { return 0; }",
				"	boolean f() { return true; }",
				"}"
		);
	}

	@Test
	public void duplicateParamName() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	int f(int x, boolean x) { return 0; }",
				"}"
		);
	}

	@Test
	public void overrideCheck1() {
		expectOk(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	int f(int x, boolean y) { return 0; }",
				"}",
				"class B extends A {",
				"   int f(int x, boolean y) { return 1; }",
				"}"
		);
	}

	@Test
	public void overrideCheck2() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	int f(int x, boolean y) { return 0; }",
				"}",
				"class B extends A {",
				"   int f(boolean x, int y) { return 1; }",
				"}"
		);
	}


	@Test
	public void overrideCheck3() {
		expectOk(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	A f(A a) { return new A(); }",
				"}",
				"class B extends A {",
				"   B f(A a) { return new B(); }",
				"}"
		);
	}

	@Test
	public void overrideCheck4() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	B f(A a) { return new B(); }",
				"}",
				"class B extends A {",
				"   A f(A a) { return new A(); }",
				"}"
		);
	}

	@Test
	public void overrideCheck5() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	A f(B a) { return new B(); }",
				"}",
				"class B extends A {",
				"   A f(A a) { return new A(); }",
				"}"
		);
	}

	@Test
	public void overrideCheck6() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) {} }",
				"class A {",
				"	A f(A a) { return new B(); }",
				"}",
				"class B extends A {",
				"   A f(B a) { return new A(); }",
				"}"
		);
	}


	@Test
	public void argName() {
		expectTypeErrors(
				"class Main { public static void main(String[] args) { int args; } }"
		);
	}


	private void expectTypeErrors(String... inputLines) {
		test(true, inputLines);
	}

	private void expectOk(String... inputLines) {
		test(false, inputLines);
	}

	private void test(boolean expectError, String ... inputLines) {
		try {
			String input = String.join("\n", inputLines);
			MJFrontend frontend = new MJFrontend();
			MJProgram program = frontend.parseString(input);
			if (!frontend.getSyntaxErrors().isEmpty()) {
				SyntaxError syntaxError = frontend.getSyntaxErrors().get(0);
				fail("Unexpected syntax error in line " + syntaxError.getLine() + ")\n" + syntaxError.getMessage());
			}

			Analysis analysis = new Analysis(program);
			analysis.check();


			if (expectError) {
				assertFalse("There should be type errors.", analysis.getTypeErrors().isEmpty());
			} else {
				if (!analysis.getTypeErrors().isEmpty()) {
					throw analysis.getTypeErrors().get(0);
				}
				assertTrue("There should be no type errors.", analysis.getTypeErrors().isEmpty());
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
