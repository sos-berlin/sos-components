package com.sos.joc.xmleditor.commons;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.util.SOSPath;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class Xml2JsonConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Xml2JsonConverterTest.class);

    private static final String YADE_SCHEMA_PATH = "src/main/resources/" + JocXmlEditor.YADE_SCHEMA_RESOURCE_PATH;

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void xml2json() throws Exception {
        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("TYPE", "Login");
        JsonObjectBuilder obc = Json.createObjectBuilder();
        obc.add("userId", "re");
        obc.add("password", "pass");
        ob.add("userAndPassword", obc);

        LOGGER.info(new ObjectMapper().writeValueAsString(ob.build()));

        // String s = ob.build().toString();
        // JsonReader r = Json.createReader(new StringReader(s));
        // JsonObject j = rdr.readObject();
        // r.close();

    }

    @Ignore
    @Test
    public void xml2jsonYade() throws Exception {
        ObjectType type = ObjectType.YADE;
        Path xml = Paths.get("src/test/resources/xmleditor/yade.xml");
        Path schema = Paths.get(YADE_SCHEMA_PATH);
        LOGGER.info(schema.toFile().getCanonicalPath());

        Xml2JsonConverter c = new Xml2JsonConverter();
        try {
            String s = c.convert(type, SOSPath.readFile(schema), SOSPath.readFile(xml));
            LOGGER.info(s);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void xml2jsonYade220() throws Exception {
        ObjectType type = ObjectType.YADE;
        Path xml = Paths.get("src/test/resources/xmleditor/yade_220.xml");
        Path schema = Paths.get(YADE_SCHEMA_PATH);
        LOGGER.info(schema.toFile().getCanonicalPath());

        Xml2JsonConverter c = new Xml2JsonConverter();
        try {
            String s = c.convert(type, SOSPath.readFile(schema), SOSPath.readFile(xml));
            LOGGER.info("end: len=" + s.length());
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

    }

    @Ignore
    @Test
    public void xml2jsonYade660() throws Exception {
        ObjectType type = ObjectType.YADE;
        Path xml = Paths.get("src/test/resources/xmleditor/yade_660.xml");
        Path output = Paths.get("src/test/resources/xmleditor/yade_660.json");
        Path schema = Paths.get(YADE_SCHEMA_PATH);
        LOGGER.info(schema.toFile().getCanonicalPath());

        Xml2JsonConverter c = new Xml2JsonConverter();
        try {
            String s = c.convert(type, SOSPath.readFile(schema), SOSPath.readFile(xml));
            LOGGER.info("end: len=" + s.length());
            Files.write(output, s.getBytes());
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void xml2jsonOthers() throws Exception {
        ObjectType type = ObjectType.OTHER;
        Path xml = Paths.get("src/test/resources/xmleditor/other.xml");
        Path schema = Paths.get(YADE_SCHEMA_PATH);
        LOGGER.info(schema.toFile().getCanonicalPath());

        Xml2JsonConverter c = new Xml2JsonConverter();
        try {
            String s = c.convert(type, SOSPath.readFile(schema), SOSPath.readFile(xml));
            LOGGER.info(s);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

    }

    @Ignore
    @Test
    public void jsonFactoryTest() throws Exception {
        LOGGER.info("start");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JsonFactory jfactory = new JsonFactory();
        JsonGenerator gen = jfactory.createGenerator(stream, JsonEncoding.UTF8);
        try {
            gen.writeStartObject();
            gen.writeFieldName("xx");

            writeObject(gen);

            gen.writeFieldName("my_array");
            gen.writeStartArray();
            gen.writeEndArray();

            gen.writeEndObject();

            gen.close();
            gen = null;
            String json = new String(stream.toByteArray(), "UTF-8");
            LOGGER.info(json);
            LOGGER.info("end");
        } catch (Throwable e) {
            throw e;
        } finally {
            if (gen != null) {
                gen.close();
            }
            stream.close();
        }
    }

    private void writeObject(JsonGenerator gen) throws Exception {
        // gen.writeStartArray();
        for (int i = 0; i < 1; i++) {
            gen.writeStartObject();
            gen.writeStringField("name", "Tom_" + i);
            gen.writeNumberField("age", 25);
            gen.writeFieldName("address");
            gen.writeStartArray();
            gen.writeString("Berlin");
            gen.writeString("5th avenue");
            gen.writeEndArray();
            gen.writeEndObject();
        }
        // gen.writeEndArray();
    }

}
