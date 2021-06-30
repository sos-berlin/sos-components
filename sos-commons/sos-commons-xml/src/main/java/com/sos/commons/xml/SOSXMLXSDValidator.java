package com.sos.commons.xml;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLDoctypeException;
import com.sos.commons.xml.exception.SOSXMLException;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;

public class SOSXMLXSDValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSXMLXSDValidator.class);

    public static void validate(URL schema, String xml) throws Exception {
        if (schema == null) {
            throw new SOSXMLException("missing schema");
        }
        validate(new StreamSource(schema.toExternalForm()), xml);
    }

    public static void validate(Path schema, String xml) throws Exception {
        if (schema == null) {
            throw new SOSXMLException("missing schema");
        }
        if (!Files.exists(schema) || !Files.isReadable(schema)) {
            throw new Exception(String.format("[%s]schema not found or not readable", schema.toString()));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[schema][path]%s", schema));
        }
        validate(new StreamSource(schema.toFile()), xml);
    }

    public static void validate(Source schema, String xml) throws SOSXMLException, SOSXMLXSDValidatorException {
        if (schema == null) {
            throw new SOSXMLException("missing schema");
        }
        if (SOSString.isEmpty(xml)) {
            SAXParseException cause = new SAXParseException("Missing XML content", "publicId", "systemId", 1, 1);
            throw new SOSXMLXSDValidatorException(cause, "XML", "1", 1, true);
        }

        try {
            // check for vulnerabilities
            SOSXML.parse(xml);
        } catch (SOSXMLDoctypeException e) {
            throw e;
        } catch (Throwable e) {
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setSchema(schemaFactory.newSchema(schema));

            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(new StringReader(xml)), new SOSXMLXSDValidator().new Handler());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new SOSXMLException(e);
        }
    }

    private class Handler extends DefaultHandler {

        private final String PATH_DELIMITER = "-";
        private final String REF_DELIMITER = ";";
        private final boolean isDebugEnabled;
        private final boolean isTraceEnabled;

        private int currentDepth;
        private int lastDepth;
        private String rootElement;
        private Stack<String> currentElement = new Stack<String>();
        private String currentElementValue;
        private SAXParseException error;
        // key - depth, value - count elements
        private Map<Integer, Integer> currentDepths = new LinkedHashMap<Integer, Integer>();
        private Map<String, String> refElements = new HashMap<String, String>();

        protected Handler() {
            isDebugEnabled = LOGGER.isDebugEnabled();
            isTraceEnabled = LOGGER.isTraceEnabled();
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes attr) {
            currentElement.push(qname);
            currentElementValue = null;
            currentDepth++;

            if (currentDepth < lastDepth) {
                List<Integer> toReset = currentDepths.keySet().stream().filter(x -> x > currentDepth).collect(Collectors.toList());
                for (Integer depthKey : toReset) {
                    // currentDepths.put(depthKey, 0);
                    currentDepths.remove(depthKey);
                }
            } else {
                if (currentDepth == 1) {
                    rootElement = qname;
                }
            }

            if (currentDepths.containsKey(currentDepth)) {
                currentDepths.put(currentDepth, (currentDepths.get(currentDepth) + 1));
            } else {
                currentDepths.put(currentDepth, 1);
            }
            lastDepth = currentDepth;

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[startElement][depth=%s][%s][localName=%s][uri=%s]", currentDepth, currentElement, localName, uri));
            }

            if (qname.endsWith("Ref")) {
                if (attr != null) {
                    String ref = attr.getValue("ref");
                    if (ref != null) {
                        refElements.put(ref, new StringBuilder(qname).append(REF_DELIMITER).append(getCurrentElementPosition()).toString());
                    }
                }
            }

            handleError();
        }

        @Override
        public void endElement(String uri, String localName, String qname) {
            handleError();

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[endElement][depth=%s][%s][localName=%s][uri=%s]", currentDepth, currentElement, localName, uri));
            }

            currentElement.pop();
            currentDepth--;
        }

        @Override
        public void endDocument() throws SAXException {
            handleError();
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (currentElementValue == null) {
                String val = new String(ch, start, length).trim();
                if (val.length() > 0) {
                    currentElementValue = val;
                }
            }
        }

        @Override
        public void warning(SAXParseException e) {
            LOGGER.warn(e.toString(), e);
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            if (error == null) {// use the first error, in some cases (e.g. cvc-enumeration-valid - provides 2 errors)
                error = e;
            }
        }

        @Override
        public void fatalError(SAXParseException e) throws SOSXMLXSDValidatorException {
            error = e;
            handleError();
        }

        private void handleError() throws SOSXMLXSDValidatorException {
            if (error != null) {
                boolean fatal = false;
                if (error.getMessage() != null) {
                    String msg = error.getMessage().trim();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[handleError]%s", msg));
                    }
                    if (msg.startsWith("cvc-identity-constraint")) {
                        // xs:key -> xs:keyref issue, try to find the KeyRef element position

                        // TODO check cvc-identity-constraint code?
                        if (msg.toLowerCase().contains("not found for identity")) {
                            // Message, e.g.:
                            // cvc-identity-constraint.4.3: Key 'CredentialStoreFragmentKeyRef' with value 'yade_credential_storex' not found for identity
                            // constraint of
                            // element 'Configurations'.
                            List<String> l = Arrays.asList(msg.split(" ")).stream().filter(x -> x.startsWith("'")).collect(Collectors.toList());
                            if (l.size() == 3) {
                                String key = l.get(1).replaceAll("'", "");
                                if (refElements.containsKey(key)) {
                                    String[] arr = refElements.get(key).split(REF_DELIMITER);
                                    String position = arr[1];
                                    throw new SOSXMLXSDValidatorException(error, arr[0], position, position.split(PATH_DELIMITER).length, false);
                                }
                            }
                        }
                    } else if (msg.startsWith("cvc-enumeration-valid") && currentElementValue != null) {
                        // ignore white spaces issue
                        try {
                            // Message, e.g:
                            // cvc-enumeration-valid: Value '
                            // false
                            // ' is not facet-valid with respect to enumeration '[true, false, strict, relaxed]'. It must be a value from the enumeration.
                            int pos1 = msg.indexOf("'[");
                            int pos2 = msg.indexOf("]'");
                            List<String> enumeration = Arrays.asList(msg.substring(pos1 + 2, pos2).split(",")).stream().map(x -> x.trim()).collect(
                                    Collectors.toList());
                            if (enumeration.contains(currentElementValue)) {
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("[handleError][value=%s]error ignored", currentElementValue));
                                }
                                error = null;
                                return;
                            }
                        } catch (Throwable ex) {
                            LOGGER.warn(String.format("[exception on enum handling][%s]%s", msg, ex.toString()), ex);
                        }
                    } else if (msg.startsWith("cvc-elt.1") || msg.startsWith("cvc-complex-type.2.4.a")) {
                        // cvc-elt.1 - cannot find the declaration of element
                        // cvc-complex-type.2.4.a - Invalid content was found starting with element ...
                        fatal = true;
                    }
                }
                String elementName = null;
                String position = null;
                int depth = 1;
                try {
                    elementName = currentElement.peek();
                    position = getCurrentElementPosition();
                    depth = currentDepth;
                } catch (Throwable e) {
                    elementName = rootElement == null ? "XML" : rootElement;
                    position = "1";
                }
                throw new SOSXMLXSDValidatorException(error, elementName, position, depth, fatal);
            }
        }

        private String getCurrentElementPosition() {
            return currentDepths.entrySet().stream().filter(x -> x.getKey() <= currentDepth).map(x -> x.getValue().toString()).collect(Collectors
                    .joining(PATH_DELIMITER));
        }
    }

}
