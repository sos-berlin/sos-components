package com.sos.commons.hibernate.function.json.h2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

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
     * @param path ,e.g. '$.calendars[1].calendarName' */
    public static String jsonValue(final String jsonValue, final String path) {
        Optional<JsonValue> result = resolveJsonValue(jsonValue, path);
        return result.map(Functions::toStringValue).orElse(null);
    }

    /** SOS_JSON_ARRAY_LENGTH - currently used only in the SQL scripts
     * 
     * @param path - array ,e.g. '$.calendars' */
    public static int jsonArrayLength(final String jsonValue, final String path) {
        Optional<JsonValue> result = resolveJsonValue(jsonValue, path);
        if (result.isPresent() && result.get().getValueType() == JsonValue.ValueType.ARRAY) {
            return result.get().asJsonArray().size();
        }
        return -1;
    }

    private static Optional<JsonValue> resolveJsonValue(final String jsonValue, final String path) {
        if (jsonValue == null || jsonValue.isEmpty() || path == null || path.isEmpty()) {
            return Optional.empty();
        }

        try {
            JsonStructure json;
            try (JsonReader r = Json.createReader(new java.io.StringReader(jsonValue))) {
                json = r.read();
            }

            if (json.getValueType() != JsonValue.ValueType.OBJECT) {
                if (path.equals("$")) {
                    return Optional.of(json);
                }
                return Optional.empty();
            }
            String p = path.startsWith("$.") ? path.substring(2) : path;
            return extractValue(json, Arrays.stream(p.split("\\.")).iterator());
        } catch (Throwable e) {
            System.err.println("[resolveJsonValue][" + jsonValue + "][" + path + "]" + e);
            return Optional.empty();
        }
    }

    private static Optional<JsonValue> extractValue(JsonValue current, Iterator<String> tokens) {
        if (!tokens.hasNext() || current == null) {
            return Optional.ofNullable(current);
        }
        String token = tokens.next();
        switch (current.getValueType()) {
        case OBJECT:
            JsonObject obj = current.asJsonObject();
            if (token.contains("[")) {
                String key = token.substring(0, token.indexOf("["));
                int index = extractIndex(token);
                return Optional.ofNullable(obj.get(key)).flatMap(value -> extractValue(value, index, tokens));
            } else {
                return Optional.ofNullable(obj.get(token)).flatMap(value -> extractValue(value, tokens));
            }
        case ARRAY:
            int index = extractIndex(token);
            return extractValue(current, index, tokens);
        default:
            return Optional.empty();
        }
    }

    private static Optional<JsonValue> extractValue(JsonValue value, int index, Iterator<String> tokens) {
        if (value == null || value.getValueType() != JsonValue.ValueType.ARRAY) {
            return Optional.empty();
        }
        JsonArray arr = value.asJsonArray();
        if (index < arr.size()) {
            return extractValue(arr.get(index), tokens);
        } else {
            return Optional.empty();
        }
    }

    private static int extractIndex(String token) {
        int start = token.indexOf('[');
        int end = token.indexOf(']', start);
        if (start != -1 && end != -1) {
            return Integer.parseInt(token.substring(start + 1, end));
        }
        return -1;
    }

    private static String toStringValue(JsonValue value) {
        if (value == null) {
            return null;
        }
        switch (value.getValueType()) {
        case STRING:
            String val = ((JsonString) value).getString();
            // trim " (if available)
            if (val.length() > 1 && val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }
            return val;
        default: // NUMBER,BOOLEAN,ARRAY,OBJECT...
            return value.toString();
        }
    }
}
