package com.sos.commons.hibernate.function.json.h2;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionsTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("my_array", Json.createArrayBuilder().add(1).add(11).add(111));
        builder.add("person", Json.createObjectBuilder().add("name", "Fritz Tester").add("age", 30));

        String json = builder.build().toString();
        LOGGER.info(String.format("[JSON]%s", json));

        String searchPath = "$.person.name";
        LOGGER.info(String.format("[H2 SOS_JSON_VALUE][%s]=%s", searchPath, Functions.jsonValue(json, searchPath)));

        searchPath = "$.my_array[2]";
        LOGGER.info(String.format("[H2 SOS_JSON_VALUE][%s]=%s", searchPath, Functions.jsonValue(json, searchPath)));
        LOGGER.info(String.format("[H2 SOS_JSON_ARRAY_LENGTH][%s]=%s", searchPath, Functions.jsonArrayLength(json, searchPath)));

        searchPath = "$.my_array";
        LOGGER.info(String.format("[H2 SOS_JSON_ARRAY_LENGTH][%s]=%s", searchPath, Functions.jsonArrayLength(json, searchPath)));
    }
}
