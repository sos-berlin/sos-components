package com.sos.joc.classes.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.joc.exceptions.JocConfigurationException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.value.JExpression;


public class PredicateValidatorTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testParseDollarVariable() throws IllegalArgumentException, IOException {
        parseDollarVariableTester("${hallo}", true);
        parseDollarVariableTester("$hallo", true);
        parseDollarVariableTester("$hal_lo", true);
        parseDollarVariableTester("${hallo}.toNumber", true);
        parseDollarVariableTester("$hallo.toBoolean", true);
        parseDollarVariableTester("${hal", false);
        parseDollarVariableTester("${hal+lo}", false);
        parseDollarVariableTester("$hallo}", false);
        parseDollarVariableTester("$hal.lo", false);
        parseDollarVariableTester("${hallo}.", false);
        parseDollarVariableTester("$hallo.toboolean", false);
        parseDollarVariableTester("$hallo ", false);
        parseDollarVariableTester(" $hallo", false);
    }
    
    private void parseDollarVariableTester(String str, boolean expect) {
        try {
            PredicateParser.parseDollarVariable(str, 0);
            assertTrue(expect);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertFalse(expect);
        }
    }
    
    @Test
    public void testParseVariable() throws IllegalArgumentException, IOException {
        parseVariableTester("variable('hallo')", true);
        parseVariableTester("argument('hallo')", true);
        parseVariableTester("variable(\"hallo\")", true);
        parseVariableTester("variable(\"hallo')", false);
        parseVariableTester("variable('hallo').toBoolean", true);
        parseVariableTester("variable('hallo',default=\"hallo\")", true);
        parseVariableTester("variable('hallo',label=myLabel,default=\"\")", true);
        parseVariableTester("variable('hallo',)", false);
        parseVariableTester("variable('hallo',label='myLabel',default=\"\")", false);
        parseVariableTester("variable('hallo') ", false);
    }
    
    private void parseVariableTester(String str, boolean expect) {
        try {
            PredicateParser.parseVariable(str, 0);
            assertTrue(expect);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertFalse(expect);
        }
    }
    
    @Test
    public void testParse() throws IllegalArgumentException, IOException {
        parseTester("${こんにちは}_ != 'h' && ${äöü}.toNumber in [42]|| variable('abc', default='DEF//AULT', label = LABEL).toNumber <= 9 && returnCode != 0", false);
        parseTester("${こんにちは} != 'h' && ((${äöü} in [42])|| variable(key='abc', default='DEF//AULT', label = LABEL).toNumber <= 9) && $returnCode!= 0", true);
        parseTester("${こんにちは} != 'h' && ${äöü}.toNumber in [42]|| variable(`abc`, default='DEF//AULT', label = LABEL).toNumber <= 9 && returnCode != 0", false);
        parseTester("hallo", false);
        parseTester("${こんにちは}.toBoolean", true);
        parseTester("${こんにちは}.toNumber", true); // why not false?
        parseTester("${こんにちは}", true);
        parseTester("${こんにちは} != '''a'", false);
        parseTester("variable('abc', default='') != 'a'", false);
        parseTester("variable('abc', default=\"\") != 'a'", true);
        parseTester("variable('', default='*') != 'a'", false);
    }
    
    @Test
    public void testParse2() throws IllegalArgumentException, IOException {
        //parseTester("${こんにちは} == '1'", false);
        parseTester("${こんにちは} ++'1' != 'h'", true);
    }
    
    @Test
    public void testQuoting() throws IllegalArgumentException, IOException {
        quoteTester(quoteTester("hallo welt"));
        quoteTester(quoteTester(42));
        quoteTester(quoteTester(true));
        quoteTester(quoteTester("Olli's Test"));
        quoteTester(quoteTester("Olli\"s Test"));
        quoteTester(quoteTester("こんにちは"));
    }
    
    private void parseTester(String str, boolean expect) {
        try {
            Either<Problem, JExpression> e = JExpression.parse(str);
            if (e.isLeft()) {
                throw new JocConfigurationException(e.getLeft().message());
            }
            //PredicateParser.parse(str);
            assertTrue(expect);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertFalse(expect);
        }
    }
    
    private String quoteTester(Object str) {
        String s = str.toString();
        Either<Problem, JExpression> e = JExpression.parse(s);
        if (e.isLeft()) {
            s = JExpression.quoteString(s);
        }
        System.out.println("str: " + str + " -> " + s);
        return s;
    }
}
