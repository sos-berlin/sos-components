package com.sos.jitl.jobs.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReturnVariableUtils {

    public static void checkDuplicateReturnVariable(JsonNode returnVars) throws JobArgumentException {
        Set<String> seenNames = new HashSet<>();
        for (JsonNode node : returnVars) {
            if (node.hasNonNull("name")) {
                String varName = node.get("name").asText().trim();
                if (!varName.isEmpty()) {
                    if (!seenNames.add(varName)) {
                        throw new JobArgumentException("Duplicate return_variable 'name' found: " + varName);
                    }
                }
            }
        }
    }

    public static List<JsonNode> runJqQuery(JsonNode mergedInput, String jqQuery, Scope rootScope, String name) throws JsonQueryException, JobArgumentException {
        JsonQuery query = JsonQuery.compile(jqQuery, Versions.JQ_1_7);
        List<JsonNode> out = new ArrayList<>();
        query.apply(rootScope, mergedInput, out::add);

        if (out.isEmpty() || out.stream().allMatch(JsonNode::isNull)) {
            throw new JobArgumentException("Error in extracting return variable " + name + " from jq Query " + jqQuery);
        }
        else{
            return out;
        }
    }

    public static void writeToFile(OrderProcessStep<JS7RESTClientJobArguments> step, OrderProcessStepLogger logger, String name, String filePath, String pI, List<JsonNode> out, boolean rawOutput, ObjectMapper objectMapper) throws IOException, JobArgumentException {

        if (name.equals("returnCode")) {
            JsonNode node = out.get(0);
            if (node != null && !node.isNull()) {
                String value = node.asText(); // Convert JsonNode to String
                try {
                    int returnCode = Integer.parseInt(value);
                    step.getOutcome().setReturnCode(returnCode);
                } catch (NumberFormatException e) {
                    throw new JobArgumentException("Return variable 'returnCode' cannot be set as job return code: value '" + value + "' is not numeric.");
                }
            } else {
                throw new JobArgumentException("Return variable 'returnCode' cannot be set as job's return code.");
            }
        }

        if (filePath != null) {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            boolean append = ">>".equals(pI);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
                for (JsonNode outputNode : out) {
                    if (rawOutput && outputNode.isTextual()) {
                        writer.write(outputNode.asText());
                    } else {
                        writer.write(objectMapper.writeValueAsString(outputNode));
                    }
                    writer.newLine();
                }
            }

            logger.info("Result written to file successfully.");
            step.getOutcome().getVariables().put(name, filePath);
            logger.info("Assigned return variable: " + name + " = " + filePath);
        } else {
            // If NOT writing to file, store result directly
            if (rawOutput && out.size() == 1 && out.get(0).isTextual()) {
                String raw = out.get(0).asText();
                step.getOutcome().getVariables().put(name, raw);
                logger.info("Assigned return variable: " + name + " = " + raw);
            } else {
                JsonNode resultNode = out.size() == 1 ? out.get(0) : objectMapper.valueToTree(out);
                String resultPretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
                step.getOutcome().getVariables().put(name, resultPretty);
                logger.info("Assigned return variable: " + name + " = " + resultPretty);
            }
        }
    }

    public static void writeToFile(OrderProcessStep<JS7RESTClientJobArguments> step, OrderProcessStepLogger logger, String name, String filePath, String pI,String result, boolean rawOutput, ObjectMapper objectMapper) throws IOException, JobArgumentException {

        if (name.equals("returnCode")) {
            try {
                int returnCode = Integer.parseInt(result);
                step.getOutcome().setReturnCode(returnCode);
            } catch (NumberFormatException e) {
                throw new JobArgumentException("Return variable 'returnCode' cannot be set as job return code: value '" + result + "' is not numeric.");
            }
        }

        if (filePath != null) {
                File file = new File(filePath);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                boolean append = ">>".equals(pI);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
                    if (rawOutput && !result.isEmpty()) {
                        writer.write(result);
                    } else {
                        writer.write(objectMapper.writeValueAsString(result));
                    }
                    writer.newLine();

                }

                logger.info("Result written to file successfully.");
                step.getOutcome().getVariables().put(name, filePath);
                logger.info("Assigned return variable: " + name + " = " + filePath);
        } else {
                // If NOT writing to file, store result directly
                if (rawOutput && !result.isEmpty()) {
                    step.getOutcome().getVariables().put(name, result);
                    logger.info("Assigned return variable: " + name + " = " + result);
                } else {
                    step.getOutcome().getVariables().put(name, result);
                    logger.info("Assigned return variable: " + name + " = " + result);
                }
        }
    }
}
