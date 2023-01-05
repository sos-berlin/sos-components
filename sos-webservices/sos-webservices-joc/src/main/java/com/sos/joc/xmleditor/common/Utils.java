package com.sos.joc.xmleditor.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;

public class Utils {

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
            throw new Exception(String.format("[%s]%s", text, e.toString()), e);
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
        if (SOSString.isEmpty(content)) {
            return null;
        }
        return new SOSSerializer<String>().serializeCompressed(content);
    }

    public static String deserializeJson(String content) throws Exception {
        if (content == null || content.startsWith("{")) {
            return content;
        }
        return new SOSSerializer<String>().deserializeCompressed(content);
    }
}
