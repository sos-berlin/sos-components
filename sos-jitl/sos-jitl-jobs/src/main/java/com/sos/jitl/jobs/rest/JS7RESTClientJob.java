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
import java.util.*;

public class JS7RESTClientJob extends Job<JS7RESTClientJobArguments> {

    public static final ObjectMapper objectMapper;
    private static final Scope rootScope;

    public void processOrder(OrderProcessStep<JS7RESTClientJobArguments> step) throws Exception {
        JS7RESTClientJobArguments myArgs = step.getDeclaredArguments();
        OrderProcessStepLogger logger = step.getLogger();

        String requestJson = (String) myArgs.getMyRequest().getValue();
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

                if (targetFilePath!= null && !targetFilePath.isEmpty()) {
                    File f = new File(targetFilePath);
                    if (f.exists() && f.isFile()) {
                        logger.info("Export to File: " + targetFilePath);
                    }
                }

                if(response.getResponseBody()!=null && !response.getResponseBody().isEmpty()) {
                        JsonNode responseJson;
                        try {
                            responseJson = objectMapper.readTree(response.getResponseBody());
                            String jqQuery = null;
                            String pI = null;
                            String filePath = null;
                            String returnVarJson = (String) myArgs.getReturnVariable().getValue();
                            if (returnVarJson != null && !returnVarJson.isBlank()) {
                                JsonNode returnVars;
                                try {
                                    returnVars = objectMapper.readTree(returnVarJson);
                                } catch (Exception e) {
                                    throw new JobException("Invalid JSON in 'return_variable': " + e.getMessage(), e);
                                }

                                if (returnVars.isArray()) {
                                    for (JsonNode mappingNode : returnVars) {
                                        String varName = mappingNode.has("name") ? mappingNode.get("name").asText() : null;
                                        String path = mappingNode.has("path") ? mappingNode.get("path").asText() : null;
                                        if (varName != null && path != null) {
                                            try {
                                                String[] parts = path.split("\\|\\|", 2);
                                                jqQuery = parts[0].trim();

                                                // Compile the jq expression
                                                JsonQuery query = JsonQuery.compile(jqQuery, Versions.JQ_1_7);
                                                // Create a child scope from the shared root scope (ensure rootScope is initialized outside the loop once)
                                                Scope childScope = Scope.newChildScope(rootScope);

                                                // Run the query
                                                final List<JsonNode> out = new ArrayList<>();
                                                query.apply(childScope, responseJson, out::add);

                                                // Decide whether to return a single value or array
                                                JsonNode resultNode = out.size() == 1 ? out.get(0) : objectMapper.valueToTree(out);

                                                // Set the result in the named variable
                                                step.getOutcome().getVariables().put(varName, resultNode.toString());

                                                logger.info("Return variable assigned: " + varName + " = " + resultNode.toString());

                                                // sending the response data into the file
                                                if (parts.length > 1) {
                                                    if (parts[1].trim().startsWith(">")) {

                                                        // Split the second part (processor + file path) on whitespace
                                                        String[] processorParts = parts[1].trim().split("\\s+", 2);

                                                        if (processorParts.length > 0 && processorParts[0] != null && !processorParts[0].isEmpty() && (processorParts[0].equals(">>") || processorParts[0].equals(">")) && processorParts.length > 1 && processorParts[1] != null && !processorParts[1].isEmpty()) {
                                                            pI = processorParts[0];
                                                            filePath = processorParts[1].trim();
                                                        } else {
                                                            throw new JobArgumentException("Either the output file path or the processing instruction is missing or invalid.");
                                                        }
                                                        logger.info("Output File Path: " + filePath + " Processing instructions: " + pI);

                                                        // code to send data into file
                                                        File file = new File(filePath);
                                                        File parent = file.getParentFile();
                                                        if (parent != null && !parent.exists()) {
                                                            parent.mkdirs();
                                                        }

                                                        // Determine append mode based on processing instruction
                                                        boolean append = ">>".equals(pI);

                                                        // Create file if not exists
                                                        if (!file.exists()) {
                                                            file.createNewFile();
                                                        }

                                                        // Write the resultNode to file
                                                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
                                                            writer.write(resultNode.toString());
                                                            writer.newLine(); // optional
                                                        }

                                                        logger.info("Result written to file successfully.");
                                                    } else {
                                                        throw new JobArgumentException("The processing instruction is missing or invalid.");
                                                    }

                                                }
                                            } catch (Exception e) {
                                                throw new JobException("Error extracting return variable '" + varName + "' from path '" + jqQuery + "': " + e.getMessage(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e){
                        logger.info("Response Body is not in JSON format");
                        logger.debug("Response Body : \n"+response.getResponseBody());
                    }
                }
                else{
                    logger.info("Empty Response Body");
                }
            } catch (SOSConnectionRefusedException | SOSBadRequestException e) {
                if (response != null) {
                    step.getOutcome().setReturnCode(response.getStatusCode());
                    logger.error("Request failed: " + response.getStatusCode() + " Body: " + response.getResponseBody());
                }
                throw e;

            } catch (IOException e) {
                logger.error("I/O error during REST call: " + e.getMessage(), e);
                throw new JobException("REST call failed due to network error: " + e.getMessage(), e);

            } catch (Exception e) {
                logger.error("Unhandled exception: " + e.getMessage(), e);
                throw e;

            } finally {
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
