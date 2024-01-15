package com.sos.joc.classes;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDepthTest {

    @Test
    public void test() {
        String fPath = "C:\\tmp\\BillingSOUploadFile.workflow.json";
        getDepth(new File(fPath));
    }

    public static void getDepth(File file) {
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        JsonFactory f = mapper.getFactory();
        try {
            JsonParser p = f.createParser(file);

            JsonToken t = p.nextToken();

            int currentDepth = 0;
            int maxDepth = 0;

            while (t != null) {
                if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                    currentDepth++;
                } else {
                    if (t == JsonToken.END_OBJECT || t == JsonToken.END_ARRAY) {
                        if (currentDepth > maxDepth) {
                            maxDepth = currentDepth;
                        }
                        currentDepth--;
                    }
                }

                t = p.nextToken();
            }

            System.out.printf("Max depth = %d%n", maxDepth);
            p.close();
        } catch (IOException ioe) {
            System.out.printf("File processing failed: = '%s'. Message: %s.%n", file.getAbsolutePath(), ioe.getMessage());
        }
    }
}
