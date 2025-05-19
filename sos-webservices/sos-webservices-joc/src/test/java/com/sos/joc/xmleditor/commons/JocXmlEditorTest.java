package com.sos.joc.xmleditor.commons;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JocXmlEditorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void getResourceImplPathTest() throws Exception {
        assertEquals("getResourceImplPathTest", "./xmleditor/test", JocXmlEditor.getResourceImplPath("test"));
    }
}
