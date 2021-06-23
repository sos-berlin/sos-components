package com.sos.commons.xml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;

public class SOSXMLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSXMLTest.class);

    @Ignore
    @Test
    public void testYade() throws Exception {
        Path file = Paths.get("src/test/resources/yade.xml");

        Document doc = SOSXML.parse(file);
        LOGGER.info("DOC ROOT=" + doc.getDocumentElement().getNodeName());

        LOGGER.info("XML - ONLY ELEMENT NODES--------------------------");
        List<Element> elements = SOSXML.getChildElemens(doc.getDocumentElement());
        if (elements != null) {
            for (Element el : elements) {
                LOGGER.info("   " + el.getNodeName());
            }
        }

        SOSXMLXPath xpath = SOSXML.newXPath();

        LOGGER.info("XPATH - ATTRIBUTE--------------------------");
        Node node = xpath.selectNode(doc, "//Profile[1]/@profile_id");
        LOGGER.info("   attr=" + node.getNodeValue());

        LOGGER.info("XPATH - CDATA--------------------------");
        node = xpath.selectNode(doc, "//Hostname[1]/text()");
        LOGGER.info("   CDATA=" + node.getNodeValue());

        // LOGGER.info("XPATH - ALL NODES--------------------------");
        // NodeList x = xpath.selectNodes(doc, "//*");
        // for (int i = 0; i < x.getLength(); i++) {
        // LOGGER.info(" node=" + x.item(i).getNodeName());
        // }

        LOGGER.info("XPATH - NODES------------------------------");
        NodeList nodes = xpath.selectNodes(doc, "//Profile");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Element child = (Element) nodes.item(i);
                LOGGER.info("    profile_id=" + child.getAttribute("profile_id"));
            }
        }

    }
}
