package calculator;

import exprs.ExprParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * This tests the basic version of the
 * It tests some basic expressions without lambdas or lets.
 * <p>
 * To run this test you have to create a class named explang.interpret.Interpreter
 * This class should have a static method named "run" which takes a String,
 * parses and evaluates it and returns the result of the evaluation as an int.
 */
public class InterpreterTest {

    @Test
    public void testArith1() {
        String expr = "5+2";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #1 - Addition.", 7, v);
    }

    @Test
    public void testArith2() {
        String expr = "9-3";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #2 - Subtraction.", 6, v);
    }

    @Test
    public void testArith3() {
        String expr = "3*8";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #3 - Multiplication.", 24, v);
    }


    @Test
    public void testArith4() {
        String expr = "10/2";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #4 - Division.", 5, v);
    }

    @Test
    public void testArith5() {
        String expr = "-9+2";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #5 - Negation.", -7, v);
    }

    @Test
    public void testArith6() {
        String expr = "8";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #6 - Number.", 8, v);
    }

    @Test
    public void testArith7() {
        String expr = "(5*3) + (60 / (15 - 5))";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #7 - All kinds of operations.", 21, v);
    }

    @Test
    public void testArith8() {
        String expr = "2*3+4*5";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #8 - Multiplication and Addition.", 26, v);
    }

    @Test
    public void testArith9() {
        String expr = "-(2+3) * 4";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #9 - Negation, Multiplication and Addition.", -20, v);
    }

    @Test
    public void testArith10() {
        String expr = "-(2+3) * 4 / 2 + (-6)";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #10 - Negation and all other operations.", -16, v);
    }

    @Test
    public void testArith12() {
        String expr = "5*3+4";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #11 - Multiplication and Addition.", 19, v);
    }

    @Test
    public void paren_expr() {
        String expr = "(((((5)))))";
        int v = Main.run(expr);
        Assert.assertEquals("Interpreter Test Case #12 - Brackets around the single value.", 5, v);
    }
}
