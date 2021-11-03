package com.sos.jitl.jobs.sap.common;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Constants {

    public static final Path statusFileDirectory = Paths.get("saps4hana");
    public static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    public static final ObjectMapper objectMapperPrettyPrint = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false).configure(SerializationFeature.INDENT_OUTPUT, true);
}
