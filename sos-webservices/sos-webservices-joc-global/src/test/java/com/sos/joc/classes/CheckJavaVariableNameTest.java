package com.sos.joc.classes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class CheckJavaVariableNameTest {
    
    private String[] strings = {"こんにちは", "niño", "ha/lo", "final", "1st", null};
    
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
        assertFalse(test(strings[4]));
    }
    
    @Test
    public void test6() {
        assertFalse(test(strings[5]));
    }

}
