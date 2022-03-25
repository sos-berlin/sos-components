package com.sos.commons.hibernate.function.json.h2;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/** see http://h2database.com/html/features.html?highlight=drop%2Calias&search=drop%20alias#user_defined_functions */

/* DROP ALIAS IF EXISTS SOS_JSON_VALUE; */
/* CREATE ALIAS SOS_JSON_VALUE FOR "com.sos.commons.hibernate.function.json.h2.Functions.jsonValue"; */
/**/
/* DROP ALIAS IF EXISTS SOS_JSON_ARRAY_LENGTH; */
/* CREATE ALIAS SOS_JSON_ARRAY_LENGTH FOR "com.sos.commons.hibernate.function.json.h2.Functions.jsonArrayLength"; */
public class Functions {

    public static final String NAME_JSON_VALUE = "SOS_JSON_VALUE";
    public static final String NAME_JSON_ARRAY_LENGTH = "SOS_JSON_ARRAY_LENGTH";

    /** SOS_JSON_VALUE - used by com.sos.commons.hibernate.function.json.SOSHibernateJsonValue
     * 
     * @param path ,e.g. '$.ports.usb' */
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
                    SOSJsonArray arr = newSOSJsonArray(p);
                    p = arr.index > -1 ? arr.name : p;
                    result = json.get(p);
                    if (result == null) {
                        return null;
                    }
                    if (result.getValueType().equals(ValueType.OBJECT)) {
                        json = (JsonObject) result;
                    } else if (result.getValueType().equals(ValueType.ARRAY) && arr.index > -1) {
                        try {
                            result = ((JsonArray) result).get(arr.index);
                        } catch (Throwable e) {
                        }
                    }
                }
            }
            return result.getValueType().equals(ValueType.STRING) ? trim(result.toString()) : result.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    /** SOS_JSON_ARRAY_LENGTH - currently used only in the sql scripts
     * 
     * @param path - array ,e.g. '$.ports.usb' */
    public static int jsonArrayLength(final String jsonValue, final String path) {
        JsonStructure jsonStruct;
        int length = -1;
        try {
            if (jsonValue == null || path == null) {
                return length;
            }
            jsonStruct = string2json(jsonValue);
            if (jsonStruct == null) {
                return length;
            }
            String[] paths = path.split("\\.");
            if (path.length() < 2) { // $
                return length;
            } else {
                if (!jsonStruct.getValueType().equals(ValueType.OBJECT)) {
                    return length;
                }
            }

            JsonObject json = (JsonObject) jsonStruct;
            JsonValue result = json;
            for (String p : paths) {
                if (!p.equals("$")) {
                    SOSJsonArray arr = newSOSJsonArray(p);
                    p = arr.index > -1 ? arr.name : p;
                    result = json.get(p);
                    if (result == null) {
                        return length;
                    }
                    if (result.getValueType().equals(ValueType.OBJECT)) {
                        json = (JsonObject) result;
                    } else if (result.getValueType().equals(ValueType.ARRAY)) {
                        if (arr.index > -1) {
                            try {
                                result = ((JsonArray) result).get(arr.index);
                                if (result.getValueType().equals(ValueType.ARRAY)) {
                                    length = ((JsonArray) result).size();
                                }
                            } catch (Throwable e) {
                            }
                        } else {
                            length = ((JsonArray) result).size();
                        }
                    }
                }
            }
        } catch (Throwable e) {

        }
        return length;
    }

    private static SOSJsonArray newSOSJsonArray(String propertyName) {
        return new Functions().new SOSJsonArray(propertyName);
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

    private class SOSJsonArray {

        private String name;
        private int index = -1;

        private SOSJsonArray(String name) {
            if (name.endsWith("]")) {
                int i = name.indexOf("[");
                if (i > -1) {
                    try {
                        this.index = Integer.parseInt(name.substring(i + 1, name.length() - 1));
                        this.name = name.substring(0, i);
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }

}
