package com.sos.jitl.jobs.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.commons.httpclient.exception.SOSConnectionRefusedException;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Versions;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JS7RESTClientJob extends Job<JS7RESTClientJobArguments> {

    public static final ObjectMapper objectMapper;
    private static final Scope rootScope;

    public void processOrder(OrderProcessStep<JS7RESTClientJobArguments> step) throws Exception {
        JS7RESTClientJobArguments myArgs = step.getDeclaredArguments();
        OrderProcessStepLogger logger = step.getLogger();

        String requestJson = (String) myArgs.getMyRequest().getValue();
        logger.info(requestJson);
        if (requestJson != null && !requestJson.isBlank()) {
            JsonNode requestNode;
            try {
                requestNode = objectMapper.readTree(requestJson);
            } catch (Exception e) {
                throw new JobException("Invalid JSON in 'myRequest': " + e.getMessage(), e);
            }

            String endpoint = requestNode.has("endpoint") ? requestNode.get("endpoint").asText(null) : null;
            if (endpoint == null || endpoint.isBlank()) {
                throw new JobRequiredArgumentMissingException("Missing or empty 'endpoint' in request JSON.");
            }

            logger.info("Endpoint: " + endpoint);

            String bodyStr = null;
            if (requestNode.has("body")) {
                try {
                    JsonNode bodyNode = requestNode.get("body");
                    if (bodyNode != null) {
                        bodyStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyNode);
                        logger.info("Body:" + bodyStr);
                    }
                } catch (Exception e) {
                    throw new JobException("Failed to extract 'body' from request JSON: " + e.getMessage(), e);
                }
            }

            List<Header> headers = new ArrayList<Header>();

            if (requestNode.has("headers") && requestNode.get("headers").isArray()) {
                for (JsonNode headerNode : requestNode.get("headers")) {
                    if (headerNode.has("key") && headerNode.has("value")) {
                        String key = headerNode.get("key").asText();
                        String value= headerNode.get("value").asText();
                        headers.add(new BasicHeader(key, value));
                    }
                }
            }

            ApiExecutor apiExecutor = new ApiExecutor(step,headers);
            String accessToken = null;
            ApiResponse response = null;
            boolean loginSuccessful = false;
            boolean logoutSuccessful = false;

            try {
                response = apiExecutor.login();
                if (response == null || response.getStatusCode() != 200) {
                    String body = response != null ? response.getResponseBody() : "No response";
                    throw new JobException("Login failed. " + body);
                }
                loginSuccessful = true;
                logger.info("Login successful.");

                accessToken = response.getAccessToken();

                response = apiExecutor.post(accessToken, endpoint, bodyStr);
                int statusCode = response.getStatusCode();

                if (statusCode < 200 || statusCode >= 300) {
                    logger.error("Request failed with status code: " + statusCode + ". Response: " + response.getResponseBody());
                    if (statusCode == 420) {
                        throw new JobException("JS7 API Throttling (420): " + response.getResponseBody());
                    }
                    throw new JobException("Unexpected status code: " + statusCode);
                }

                logger.info("REST call successful. Status Code: " + statusCode);

                String targetFilePath = (String) step.getOutcome().getVariables().get("js7ApiExecutorOutfile");
                if (targetFilePath != null && !targetFilePath.isEmpty() && Files.exists(Paths.get(targetFilePath))) {
                    logger.info("Export to File: " + targetFilePath);
                }

                // Extract content-type header (case-insensitive)
                Map<String, String> resHeaders = apiExecutor.getResponseHeaders();
                String contentType = null;
                for (Map.Entry<String, String> entry : resHeaders.entrySet()) {
                    if ("content-type".equalsIgnoreCase(entry.getKey())) {
                        contentType = entry.getValue();
                        break;
                    }
                }

                String jqQuery = null;
                String pI = null;
                String filePath = null;
                String returnVarJson =  myArgs.getReturnVariable().getValue();
                String varName=null;
                String path= null;

                if(response.getResponseBody() != null && !response.getResponseBody().trim().isEmpty()){
                    if ("application/json".equalsIgnoreCase(contentType)) {
                        JsonNode responseJson;
                        responseJson = objectMapper.readTree(response.getResponseBody());
                        logger.debug("Response Body: " +objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseJson) );
                        if (returnVarJson != null && !returnVarJson.isEmpty()) {
                            JsonNode returnVars;
                                try {
                                    returnVars = objectMapper.readTree(returnVarJson);
                                } catch (Exception e) {
                                    throw new JobException("Invalid JSON in 'return_variable': " + e.getMessage(), e);
                                }

                            if (returnVars.isArray()) {
                                for (JsonNode mappingNode : returnVars) {
                                    varName = mappingNode.has("name") ? mappingNode.get("name").asText() : null;
                                    path = mappingNode.has("path") ? mappingNode.get("path").asText() : null;

                                    if (varName != null && path != null) {
                                        String[] parts = path.split("\\|\\|", 2);
                                        jqQuery = parts[0].trim();
                                        boolean rawOutput = false;
                                        filePath = null;
                                        pI = null;

                                        // Parse the second part: options and/or file path
                                        if (parts.length > 1) {
                                            String[] processorParts = parts[1].trim().split("\\s+");
                                            for (int i = 0; i < processorParts.length; i++) {
                                                String token = processorParts[i].trim();
                                                if (token.equals("-r") || token.equals("--raw-output")) {
                                                    rawOutput = true;
                                                } else if (token.equals(">") || token.equals(">>")) {
                                                    if (i + 1 < processorParts.length) {
                                                        pI = token;
                                                        filePath = processorParts[i + 1].trim();
                                                        break;
                                                    } else {
                                                        throw new JobArgumentException("Missing file path.");
                                                    }
                                                }
                                            }
                                        }

                                        // Compile and execute jq query
                                        JsonQuery query = JsonQuery.compile(jqQuery, Versions.JQ_1_7);
                                        List<JsonNode> out = new ArrayList<>();
                                        query.apply(rootScope, responseJson, out::add);

                                        if (!out.isEmpty()) {

                                            if (filePath != null) {
                                                File file = new File(filePath);
                                                File parent = file.getParentFile();
                                                if (parent != null && !parent.exists()) {
                                                    parent.mkdirs();
                                                }

                                                boolean append = ">>".equals(pI);
                                                if (!file.exists()) {
                                                    file.createNewFile();
                                                }

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
                                                step.getOutcome().getVariables().put(varName, filePath);
                                                logger.info("Assigned return variable: " + varName + " = " + filePath);
                                            } else {
                                                // If NOT writing to file, store result directly
                                                if (rawOutput && out.size() == 1 && out.get(0).isTextual()) {
                                                    String raw = out.get(0).asText();
                                                    step.getOutcome().getVariables().put(varName, raw);
                                                    logger.info("Assigned return variable: " + varName + " = " + raw);
                                                } else {
                                                    JsonNode resultNode = out.size() == 1 ? out.get(0) : objectMapper.valueToTree(out);
                                                    String resultPretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
                                                    step.getOutcome().getVariables().put(varName, resultPretty);
                                                    logger.info("Assigned return variable: " + varName + " = " + resultPretty);
                                                }
                                            }
                                        } else {
                                            logger.error("Error in extracting return variable " + varName + " from jq Query " + jqQuery);
                                            step.getOutcome().setReturnCode(1);
                                        }
                                    }
                                }
                            } else {
                                logger.error("return_variable is not a valid JSON array");
                                step.getOutcome().setReturnCode(1);
                            }

                        }
                    }
                    else{// response is not JSON
                            logger.info("Response Body is not in JSON format");
                            logger.debug("Response Body : \n" + response.getResponseBody());

                            if (returnVarJson != null && !returnVarJson.isEmpty()) {
                                JsonNode returnVars;
                                try {
                                    returnVars = objectMapper.readTree(returnVarJson);
                                } catch (Exception e) {
                                    throw new JobException("Invalid JSON in 'return_variable': " + e.getMessage(), e);
                                }

                                if (returnVars.isArray()) {
                                    for (JsonNode mappingNode : returnVars) {
                                        String missingVar = mappingNode.has("name") ? mappingNode.get("name").asText() : null;
                                        if (missingVar != null) {
                                            logger.error("Could not create return variable: " + missingVar);
                                            step.getOutcome().setReturnCode(1);
                                        }
                                    }
                                } else {
                                    logger.error("return_variable is not a valid JSON array");
                                    step.getOutcome().setReturnCode(1);
                                }
                            }
                        }
                }
                else{
                    logger.info("Empty Response Body");
                    if (returnVarJson != null && !returnVarJson.isEmpty()) {
                        JsonNode returnVars;
                        try {
                            returnVars = objectMapper.readTree(returnVarJson);
                        } catch (Exception e) {
                            throw new JobException("Invalid JSON in 'return_variable': " + e.getMessage(), e);
                        }

                        if (returnVars.isArray()) {
                            for (JsonNode mappingNode : returnVars) {
                                String missingVar = mappingNode.has("name") ? mappingNode.get("name").asText() : null;
                                if (missingVar != null) {
                                    logger.error("Could not create return variable: " + missingVar);
                                    step.getOutcome().setReturnCode(1);
                                }
                            }
                        } else {
                            logger.error("return_variable is not a valid JSON array");
                            step.getOutcome().setReturnCode(1);
                        }
                    }
                }
            } catch (SOSConnectionRefusedException | SOSBadRequestException e) {
                if (response != null) {
                    step.getOutcome().setReturnCode(response.getStatusCode());
                    logger.error("Request failed: " + response.getStatusCode() + " Body: " + response.getResponseBody());
                }
                throw e;
            } catch (IOException e) {
                logger.error("I/O error during REST call: " + e.getMessage(), e);
                throw new JobException("REST call failed due to I/O error: " + e.getMessage(), e);
            }
            finally {
                try {
                    if (accessToken != null) {
                        apiExecutor.logout(accessToken);
                        logoutSuccessful = true;
                    }
                } catch (Exception e) {
                    logger.warn("Logout failed: " + e.getMessage(), e);
                }

                try {
                    apiExecutor.close();
                } catch (Exception e) {
                    logger.warn("Failed to close ApiExecutor: " + e.getMessage(), e);
                }

                if (loginSuccessful && logoutSuccessful) {
                    logger.info("Logout complete.");
                }
            }
        } else {
            throw new JobRequiredArgumentMissingException("Missing request JSON in job arguments.");
        }
    }

    static {
        rootScope = Scope.newEmptyScope(); // create empty root scope
        BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, rootScope); // load jq built-ins

        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    }
}
