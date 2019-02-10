package calculator;

import calculator.ast.Expr;
import exprs.ExprParser.ParserError;
import org.junit.Test;

import static calculator.Main.parseString;
import static calculator.Main.prettyPrint;
import static org.junit.Assert.assertEquals;

/**
 * This class tests the parser with some input strings.
 * <p>
 * Before you run this test you have to make the method Main.parseToAST public.
 **/
public class ParserTest {
    @Test
    public void testOk1() throws Exception {
        String input = "((5*3) + 4)";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #1 - Addition and Multiplication with parenthesis.", "((5 * 3) + 4)", output);
    }

    @Test
    public void testOk2() throws Exception {
        String input = "2 + 3";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #2 - Addition.", "(2 + 3)", output);
    }

    @Test
    public void testOk3() throws Exception {
        String input = "2 + 3 * 4";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #3 - Addition and Multiplication without parenthesis.", "(2 + (3 * 4))", output);
    }

    @Test
    public void testOk4() throws Exception {
        String input = "2 * 3 + 4 * 5";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #4 - Addition and Multiplication without parenthesis.", "((2 * 3) + (4 * 5))", output);
    }

    @Test
    public void testOk5() throws Exception {
        String input = "-5";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #5 - Negation.", "(-5)", output);
    }

    // checks precedence of negation
    @Test
    public void testOk6() throws Exception {
        String input = "-5*2";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #6 - Negation and Multiplication.", "((-5) * 2)", output);
    }

    @Test
    public void testOk7() throws Exception {
        String input = "90 * 2 + 12 / 14";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #7 - Division and Multiplication.", "((90 * 2) + (12 / 14))", output);
    }

    @Test
    public void testOk8() throws Exception {
        String input = "(((((5)))))";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #8 - Parenthesis around single value.", "5", output);
    }

    @Test
    public void testOk9() throws Exception {
        String input = "2-3*4";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #9 - Subtraction and Multiplication.", "(2 - (3 * 4))", output);
    }

    @Test
    public void testOk10() throws Exception {
        String input = "90 * 2 / 12 + 14";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("Parser Test Case #10 - Multiplication, Division and Addition.", "(((90 * 2) / 12) + 14)", output);
    }

    @Test(expected = ParserError.class)
    public void testFail1() throws Exception {
        String input = "((5*3) + 4";
        Main.parseString(input);
    }

    @Test(expected = ParserError.class)
    public void testFail2() throws Exception {
        String input = "3+";
        Main.parseString(input);
    }

    @Test(expected = ParserError.class)
    public void testFail3() throws Exception {
        String input = "+3";
        Main.parseString(input);
    }
}