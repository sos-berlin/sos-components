package com.sos.commons.util;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class SOSParameterSubstitutorTest {
    
    @Test
    public void testGetParameterNameFromString() {
        SOSParameterSubstitutor subs = new SOSParameterSubstitutor(true, " ",":");
        String string = " key1:value1 key2:value2;";
        List<String> vals = subs.getParameterNameFromString(string);
        assertTrue(vals.contains("key1"));
        assertTrue(vals.contains("key2"));
    }

}
