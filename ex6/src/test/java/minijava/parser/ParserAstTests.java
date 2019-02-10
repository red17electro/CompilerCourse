package minijava.parser;

import frontend.AstPrinter;
import frontend.MJFrontend;
import frontend.SyntaxError;
import minijava.ast.MJElement;
import minijava.ast.MJMethodCall;
import minijava.ast.MJProgram;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ParserAstTests {

	@Test
	public void testMinimalProg() throws Exception {
		String input = "class Main { public static void main(String[] args) { }}";
		MJFrontend mjFrontend = new MJFrontend();
		mjFrontend.parseString(input);
		Assert.assertTrue(mjFrontend.getSyntaxErrors().isEmpty());
	}

	@Test
	public void testPrint() throws Exception {
		String input = "class Main { public static void main(String[] args) { System.out.println(42); }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("System.out.println(42);"));
	}

	@Test
	public void testLocalVar() throws Exception {
		String input = "class Main { public static void main(String[] args) { boolean x; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("boolean x;"));
	}

	@Test
	public void testAssignment() throws Exception {
		String input = "class Main { public static void main(String[] args) { int x; x = 5; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = 5;"));
	}

	@Test
	public void testIfStmt() throws Exception {
		String input = "class Main { public static void main(String[] args) { int x; if (true) x=5; else x = 7;  }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("if (true) x = 5;"));
		Assert.assertThat(printed, CoreMatchers.containsString("else x = 7;"));
	}

	@Test
	public void testWhileStmt() throws Exception {
		String input = "class Main { public static void main(String[] args) { int x; while (x < 10) x=x+1;  }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("while ((x < 10)) x = (x + 1);"));
	}

	@Test
	public void operators() throws Exception {
		String input = "class Main { public static void main(String[] args) { boolean x; x = ((((3 * 4) + 5) < 2) && (1 < 3)); }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = ((((3 * 4) + 5) < 2) && (1 < 3))"));
	}

	@Test
	public void operatorPrecedence() throws Exception {
		String input = "class Main { public static void main(String[] args) { boolean x; x = 3*4+5 < 2 && 1 < 3; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = ((((3 * 4) + 5) < 2) && (1 < 3))"));
	}

	@Test
	public void operatorAssociativity() throws Exception {
		String input = "class Main { public static void main(String[] args) { boolean x; x = 10 - 5 -3; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = ((10 - 5) - 3)"));
	}

	@Test
	public void operatorPrecedenceMethodCallAndArrays() throws Exception {
		String input = "class Main { public static void main(String[] args) { boolean x; x = bar[1].foo; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = bar[1].foo"));
	}

	@Test
	public void operatorPrecedenceUnaryAndFields() throws Exception {
		String input = "class Main { public static void main(String[] args) { int x; x = - bar.foo(); }}";
		MJProgram ast = new MJFrontend().parseString(input);
		ast.accept(new MJElement.DefaultVisitor() {
			@Override
			public void visit(MJMethodCall node) {
				Assert.assertEquals("bar.foo()", AstPrinter.print(node));
			}
		});
	}

	@Test
	public void newObjectStatement() throws Exception {
		String input = "class Main { public static void main(String[] args) { new C(); }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("new C();"));
	}

	@Test
	public void operatorPrecedenceUnary() throws Exception {
		String input = "class Main { public static void main(String[] args) { boolean x; x = - - ! - ! - 5; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = (- (- (! (- (! (- 5))))))"));
	}

	@Test
	public void arrayLength() throws Exception {
		String input = "class Main { public static void main(String[] args) { int x; x = new int[5].length; }}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = (new int[5]).length"));
		;
	}

	@Test
	public void arrayAccess() throws Exception {
		String input = "class Main { public static void main(String[] args) { int x; x = new int[5][2]; }}";
		MJFrontend frontend = new MJFrontend();
		frontend.parseString(input);
		assertFalse(frontend.getSyntaxErrors().isEmpty());
	}

	@Test
	public void testMultipleClasses() throws Exception {
		String input = "class Main{ public static void main(String[] a){}} class A{} class B{} class C{}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("class A"));
		Assert.assertThat(printed, CoreMatchers.containsString("class B"));
		Assert.assertThat(printed, CoreMatchers.containsString("class C"));
	}

	@Test
	public void testMultipleParameters() throws Exception {
		String input = "class Main{ public static void main(String[] a){}} class A{ int m(int a, boolean b, int c){return 0;}}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("int m(int a, boolean b, int c)"));
	}

	@Test
	public void testMultipleArgumetns() throws Exception {
		String input = "class Main{public static void main(String[] a){x=a.s(1,2,f+g);}}";
		MJProgram ast = new MJFrontend().parseString(input);
		String printed = AstPrinter.print(ast);
		Assert.assertThat(printed, CoreMatchers.containsString("x = a.s(1, 2, (f + g));"));
	}

}
