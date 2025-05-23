package com.sos.joc.xmleditor.commons.standard;

import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.xmleditor.commons.JocXmlEditor;

public class StandardSchemaHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardSchemaHandlerTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Path file = Path.of("JobResource-Deployment-2025-05-17-YADE-FileTransfer-test-sftp.xml");
        String xml = StandardSchemaHandler.getXml(SOSPath.readFile(file), true);
        LOGGER.info(xml);

        JocXmlEditor.validate(ObjectType.YADE, StandardSchemaHandler.getYADESchema(), xml);
    }
}
