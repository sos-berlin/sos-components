package com.sos.joc.classes.xmleditor.validator;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLDoctypeException;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.classes.xmleditor.exceptions.SOSXsdValidatorException;

public class XsdValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(XsdValidator.class);
    private static boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private Path schema = null;

    public XsdValidator(Path sourceSchema) {
        schema = sourceSchema;
    }

    public void validate(String content) throws Exception {
        if (schema == null) {
            throw new Exception("missing schema");
        }
        if (!Files.exists(schema) || !Files.isReadable(schema)) {
            throw new Exception(String.format("[%s]schema not found or not readable", schema.toString()));
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[schema][use local file]%s", schema));
        }

        if (SOSString.isEmpty(content)) {
            SAXParseException cause = new SAXParseException("Missing XML content", "publicId", "systemId", 1, 1);
            throw new SOSXsdValidatorException(cause, "XML", "1", 1, true);
        }

        try {
            // check for vulnerabilities
            JocXmlEditor.parseXml(content);
        } catch (SOSXMLDoctypeException e) {
            throw e;
        } catch (Throwable e) {
        }

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setSchema(schemaFactory.newSchema(schema.toFile()));

            SAXParser parser = factory.newSAXParser();
            // parser.parse(new InputSource(new StringReader(content.replaceAll(">\\s+<", "><").trim())), new XsdValidatorHandler());
            parser.parse(new InputSource(new StringReader(content)), new XsdValidatorHandler());
        } catch (Throwable e) {
            throw e;
        }
    }

    public Path getSchema() {
        return schema;
    }

}
