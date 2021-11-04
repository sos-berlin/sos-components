package com.sos.joc.xmleditor.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSSerializer;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static List<Object> string2jsonList(String text) throws Exception {
        List<Object> list = new ArrayList<>();
        list.add(Utils.string2json(text));
        return list;
    }

    public static JsonArray string2json(String text) throws Exception {
        StringReader sr = null;
        JsonReader jr = null;
        try {
            sr = new StringReader(text);
            jr = Json.createReader(sr);
            return jr.readArray();

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", text, e.toString()), e);
            throw e;
        } finally {
            if (jr != null) {
                jr.close();
            }
            if (sr != null) {
                sr.close();
            }
        }
    }

    public static String serialize(String content) throws Exception {
        return new SOSSerializer<String>().serializeCompressed(content);
    }

    public static String deserializeJson(String content) throws Exception {
        if (content == null || content.startsWith("{")) {
            return content;
        }
        return new SOSSerializer<String>().deserializeCompressed(content);
    }
}
