package com.sos.joc.classes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class CheckJavaVariableNameTest {
    
    private String[] strings = {"こんにちは", "niño", "ha/lo", "final", "1st", null, "w.1", "2021-01-27", "come on", "see...more", "continue..."};
    
    private boolean test(String s) {
        try {
            CheckJavaVariableName.test("orderId", s);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Test
    public void test1() {
        assertTrue(test(strings[0]));
    }
    
    @Test
    public void test2() {
        assertTrue(test(strings[1]));
    }
    
    @Test
    public void test3() {
        assertFalse(test(strings[2]));
    }
    
    @Test
    public void test4() {
        assertFalse(test(strings[3]));
    }
    
    @Test
    public void test5() {
        assertTrue(test(strings[4]));
    }
    
    @Test
    public void test6() {
        assertFalse(test(strings[5]));
    }
    
    @Test
    public void test7() {
        assertTrue(test(strings[6]));
    }
    
    @Test
    public void test8() {
        assertTrue(test(strings[7]));
    }
    
    @Test
    public void test9() {
        assertFalse(test(strings[8]));
    }
    
    @Test
    public void test10() {
        assertFalse(test(strings[9]));
    }
    
    @Test
    public void test11() {
        assertFalse(test(strings[10]));
    }

}
