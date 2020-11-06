package com.sos.joc.classes.inventory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


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
        parseTester("${こんにちは} != 'h' && ${äöü} in [42]|| variable('abc', default='DEF//AULT', label = LABEL).toNumber <= 9 && returnCode != 0", false);
        parseTester("${こんにちは} != 'h' && ${äöü}.toNumber in [42]|| variable(`abc`, default='DEF//AULT', label = LABEL).toNumber <= 9 && returnCode != 0", false);
        parseTester("hallo", false);
        parseTester("${こんにちは}.toBoolean", true);
        parseTester("${こんにちは}.toNumber", false);
    }
    
    private void parseTester(String str, boolean expect) {
        try {
            PredicateParser.parse(str);
            assertTrue(expect);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertFalse(expect);
        }
    }
}
