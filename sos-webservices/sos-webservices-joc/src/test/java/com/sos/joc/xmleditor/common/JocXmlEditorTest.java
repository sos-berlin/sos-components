package com.sos.joc.xmleditor.common;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.classes.xmleditor.JocXmlEditor;

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
