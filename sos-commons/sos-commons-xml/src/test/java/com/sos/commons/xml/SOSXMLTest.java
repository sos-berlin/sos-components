package com.sos.commons.xml;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOSXMLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSXMLTest.class);

    @Ignore
    @Test
    public void testYade() throws Exception {
        Path file = Paths.get("src/test/resources/yade.xml");

        Document doc = SOSXML.parse(file);
        LOGGER.info("DOC ROOT=" + doc.getDocumentElement().getNodeName());

        NodeList nodes = SOSXML.newXPath().selectNodes(doc, "//Profile");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node child = nodes.item(i);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                try {
                    LOGGER.info(" profile_id=" + child.getAttributes().getNamedItem("profile_id").getNodeValue());

                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s]can't get attribute profile_id", child.getNodeName()), e);
                }
            }
        }

    }
}
