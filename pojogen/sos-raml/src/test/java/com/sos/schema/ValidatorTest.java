package com.sos.schema;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;


public class ValidatorTest {
    
    @Test
    public void testLoadingSchemas() {
        List<String> classNames = new ArrayList<String>();
        for(Entry<String, String> entry : JsonValidator.getClassUriMap().entrySet()) {
            try {
                URI schemaUri = URI.create("classpath:/raml/api/schemas/" + entry.getValue());
                JsonValidator.getSchema(schemaUri, false, false);
            } catch (Exception e) {
                classNames.add(entry.getKey());
            }
        }
        assertTrue("Schemas not found for: " + classNames.toString(), classNames.isEmpty());
    }

}
