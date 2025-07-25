package com.sos.jitl.jobs.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.sos.js7.job.exception.JobArgumentException;

import java.util.HashSet;
import java.util.Set;

public class ReturnVariableUtils {
    public static void checkDuplicateReturnVariable(JsonNode returnVars) throws JobArgumentException {
        Set<String> seenNames = new HashSet<>();
        for (JsonNode node : returnVars) {
            String varName = node.has("name") ? node.get("name").asText() : null;
            if (varName == null || varName.isBlank()) {
                throw new JobArgumentException("Each return_variable entry must have a non-empty 'name' field.");
            }
            if (!seenNames.add(varName)) {
                throw new JobArgumentException("Duplicate return_variable 'name' found: " + varName);
            }
        }
    }

}
