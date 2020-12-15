package com.sos.commons.hibernate.function.json.h2;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/** see http://h2database.com/html/features.html?highlight=drop%2Calias&search=drop%20alias#user_defined_functions */

/* DROP ALIAS IF EXISTS SOS_JSON_VALUE; */
/* CREATE ALIAS SOS_JSON_VALUE FOR "com.sos.commons.hibernate.function.json.h2.Function.jsonValue"; */
public class Function {

    public static final String NAME = "SOS_JSON_VALUE";

    public static String jsonValue(final String jsonValue, final String path) {
        JsonStructure jsonStruct;
        try {
            if (jsonValue == null || path == null) {
                return null;
            }
            jsonStruct = string2json(jsonValue);
            if (jsonStruct == null) {
                return null;
            }
            String[] paths = path.split("\\.");
            if (path.length() < 2) { // $
                return jsonStruct.toString();
            } else {
                if (!jsonStruct.getValueType().equals(ValueType.OBJECT)) {
                    return null;
                }
            }

            JsonObject json = (JsonObject) jsonStruct;
            JsonValue result = json;
            for (String p : paths) {
                if (!p.equals("$")) {
                    result = json.get(p);
                    if (result == null) {
                        return null;
                    }
                    if (result.getValueType().equals(ValueType.OBJECT)) {
                        json = (JsonObject) result;
                    }
                }
            }
            return result.getValueType().equals(ValueType.STRING) ? trim(result.toString()) : result.toString();
        } catch (Throwable e) {
            return e.toString();
        }
    }

    private static JsonStructure string2json(String response) throws Exception {
        JsonStructure json = null;
        StringReader sr = null;
        JsonReader jr = null;
        try {
            sr = new StringReader(response);
            jr = Json.createReader(sr);

            json = jr.read();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (jr != null) {
                jr.close();
            }
            if (sr != null) {
                sr.close();
            }
        }

        return json;
    }

    private static String trim(String str) {
        // remove leading and closing "
        return str.substring(1, str.length() - 1);
    }

}
