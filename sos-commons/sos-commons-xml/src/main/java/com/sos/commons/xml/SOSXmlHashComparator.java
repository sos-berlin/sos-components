package com.sos.commons.xml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SOSXmlHashComparator {

    public static boolean equals(String xml1, String xml2) throws Exception {
        byte[] hash1 = computeXmlHash(xml1);
        byte[] hash2 = computeXmlHash(xml2);
        return Arrays.equals(hash1, hash2);
    }

    public static byte[] computeXmlHash(String xml) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();

        parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), new DefaultHandler() {

            StringBuilder textBuffer = new StringBuilder();

            private void flushText() {
                String text = textBuffer.toString().trim();
                if (!text.isEmpty()) {
                    digest.update(text.getBytes(StandardCharsets.UTF_8));
                }
                textBuffer.setLength(0);
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                flushText();
                digest.update(qName.getBytes(StandardCharsets.UTF_8));

                List<String> attrNames = new ArrayList<>();
                for (int i = 0; i < attributes.getLength(); i++) {
                    attrNames.add(attributes.getQName(i));
                }

                Collections.sort(attrNames);
                for (String name : attrNames) {
                    digest.update(name.getBytes(StandardCharsets.UTF_8));
                    digest.update(attributes.getValue(name).getBytes(StandardCharsets.UTF_8));
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                textBuffer.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                flushText();
                digest.update(("/" + qName).getBytes(StandardCharsets.UTF_8));
            }
        });

        return digest.digest();
    }
}
