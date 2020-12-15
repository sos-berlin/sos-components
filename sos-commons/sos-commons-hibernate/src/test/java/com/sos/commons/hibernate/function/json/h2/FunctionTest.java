package com.sos.commons.hibernate.function.json.h2;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("my_array", Json.createArrayBuilder().add(1).add(2).add(3));
        builder.add("person", Json.createObjectBuilder().add("name", "Fritz Tester").add("age", 30));

        String json = builder.build().toString();
        LOGGER.info(String.format("[JSON]%s", json));

        String searchPath = "$.person.name";
        LOGGER.info(String.format("[H2 SEARCH][%s]%s", searchPath, Function.jsonValue(json, searchPath)));

    }
}
