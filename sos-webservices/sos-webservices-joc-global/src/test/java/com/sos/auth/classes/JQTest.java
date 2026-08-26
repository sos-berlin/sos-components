package com.sos.auth.classes;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;


public class JQTest {
    
    private static final Scope rootScope = Scope.newEmptyScope();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test1() throws JsonMappingException, JsonProcessingException {
        String json = "{\r\n"
                + "  \"sub\": \"\",\r\n"
                + "  \"zoneinfo\": \"\",\r\n"
                + "  \"postal_country\": \"\",\r\n"
                + "  \"mail\": \"\",\r\n"
                + "  \"auth_level\": \"\",\r\n"
                + "  \"igg\": \"\",\r\n"
                + "  \"origin_network\": \"\",\r\n"
                + "  \"locale\": \"\",\r\n"
                + "  \"contact_id\": \"\",\r\n"
                + "  \"rc_local_sigle\": \"\",\r\n"
                + "  \"user_bdr_id\": \"\",\r\n"
                + "  \"company_bdr_level\": \"\",\r\n"
                + "  \"is_sg_group_user\": \"\",\r\n"
                + "  \"company_bdr_id\": \"\",\r\n"
                + "  \"preferred_language\": \"en\",\r\n"
                + "  \"subname\": \"\",\r\n"
                + "  \"last_name\": \"\",\r\n"
                + "  \"login_ad\": \"\",\r\n"
                + "  \"company_bdr_name\": \"\",\r\n"
                + "  \"given_name\": \"\",\r\n"
                + "  \"sgconnect_id\": \"\",\r\n"
                + "  \"user_authorization\": [\r\n"
                + "    {\r\n"
                + "      \"resource\": \"api.job-scheduler-api\",\r\n"
                + "      \"permissions\": [\r\n"
                + "        {\r\n"
                + "          \"name\": \"JS7-ADMIN\",\r\n"
                + "          \"constraints\": []\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "          \"name\": \"JS7-VIEW\",\r\n"
                + "          \"constraints\": []\r\n"
                + "        }\r\n"
                + "      ],\r\n"
                + "      \"resource_id\": \"<resource-uuid>\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"sesame_id\": \"\",\r\n"
                + "  \"name\": \"\",\r\n"
                + "  \"family_name\": \"\"\r\n"
                + "}\r\n"
                + "\r\n"
                + " ";
        
        String jqQuery = "jq:[.user_authorization[] | select(.resource == \"api.job-scheduler-api\") | .permissions[].name]";
        
        AtomicInteger groupFoundWithQuery = new AtomicInteger(0);
        List<JsonNode> nodes = runJqQuery(objectMapper.readTree(json), jqQuery.substring(3));
        nodes.stream().filter(jn -> jn.isArray() || jn.isTextual()).flatMap(jn -> jn.isArray() ? jn.valueStream() : Stream.of(jn)).filter(
                JsonNode::isTextual).map(JsonNode::asText).peek(group -> groupFoundWithQuery.getAndIncrement()).forEach(System.out::println);
        
        assertTrue(groupFoundWithQuery.get() == 2);
        
    }
    
    private static List<JsonNode> runJqQuery(JsonNode jsonNode, String jqQuery) throws JsonQueryException {
        BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, rootScope);
        List<JsonNode> out = new ArrayList<>();
        JsonQuery.compile(jqQuery, Versions.JQ_1_7).apply(rootScope, jsonNode, out::add);
        return out;
    }

}
