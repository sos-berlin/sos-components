
package com.sos.jitl.jobs.rest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.commons.mulitpart.HttpFormData;
import com.sos.commons.httpclient.commons.mulitpart.HttpFormDataCloseable;
import com.sos.commons.httpclient.commons.mulitpart.formdata.FormDataFile;
import com.sos.commons.httpclient.commons.mulitpart.formdata.FormDataString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

public class RESTClientJob extends Job<RestJobArguments> {

    public static final ObjectMapper objectMapper;
    private  BaseHttpClient client;
    private static final Scope rootScope = Scope.newEmptyScope();

    public RESTClientJob(JobContext jobContext) {
        super(jobContext);
    }

    public void processOrder(OrderProcessStep<RestJobArguments> step) throws Exception {
        RestJobArguments myArgs = (RestJobArguments)step.getDeclaredArguments();
        OrderProcessStepLogger logger = step.getLogger();

        //get logging details for info level
        boolean logReqHeaders = false;
        boolean logReqBody = false;
        boolean logResHeaders = false;
        boolean logResBody = false;

        String infoLogging = (String) myArgs.getLogItems().getValue();

        if (infoLogging != null && !infoLogging.equalsIgnoreCase("none")) {
            String[] parts = infoLogging.split(";");
            for (String part : parts) {
                String[] split = part.split(":");
                String target = split[0].trim().toLowerCase(); // request or response

                // Default = log both headers and body
                Set<String> items = new HashSet<>();
                if (split.length > 1) {
                    for (String item : split[1].split(",")) {
                        items.add(item.trim().toLowerCase());
                    }
                } else {
                    items.add("headers");
                    items.add("body");
                }

                // Apply flags
                if ("request".equals(target)) {
                    if (items.contains("headers")) logReqHeaders = true;
                    if (items.contains("body")) logReqBody = true;
                } else if ("response".equals(target)) {
                    if (items.contains("headers")) logResHeaders = true;
                    if (items.contains("body")) logResBody = true;
                }
            }
        }

        String requestJson = (String)myArgs.getMyRequest().getValue();
        if (requestJson != null && !requestJson.isBlank()) {
            JsonNode requestNode;
            try {
                requestNode = objectMapper.readTree(requestJson);
            } catch (Exception e) {
                throw new JobException("Invalid JSON in 'myRequest': " + e.getMessage(), e);
            }

            String endpoint = requestNode.has("endpoint") ? requestNode.get("endpoint").asText((String)null) : null;
            String method = requestNode.has("method") ? requestNode.get("method").asText((String)null) : null;
            URI uri = endpoint != null ? URI.create(endpoint) : null;
            if (uri == null) {
                throw new JobRequiredArgumentMissingException("Missing or empty 'URL' in request JSON.");
            } else {

                String bodyStr = null;
                HttpFormDataCloseable formData = new HttpFormDataCloseable();
                if (requestNode.has("body")) {
                    try {
                        JsonNode bodyNode = requestNode.get("body");
                        if (bodyNode != null) {
                            bodyStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyNode);
                            if (logReqBody)
                                logger.info("Request Body :" + bodyStr);
                            logger.debug("Request Body :" + bodyStr);
                        }
                    } catch (Exception e) {
                        throw new JobException("Failed to extract 'body' from request JSON: " + e);
                    }
                }
                else if (requestNode.has("formData")) {
                    JsonNode formDataNode = requestNode.get("formData");
                    if (formDataNode != null) {
                        if (logReqBody)
                            logger.info("Request Body as formData :" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(formDataNode));
                        logger.debug("Request Body as formData:" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(formDataNode));
                    }
                    if (formDataNode == null || !formDataNode.isObject()) {
                        throw new IllegalArgumentException("Missing or invalid 'formData' object in request JSON");
                    }
                    Iterator<Map.Entry<String, JsonNode>> fields = formDataNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String key = field.getKey();
                        JsonNode valueNode = field.getValue();

                        // Special handling for "file"
                        if ("file".equalsIgnoreCase(key)) {
                            // Read file path from JSON value
                            Path filePath = Paths.get(valueNode.asText());

                            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                                throw new JobException("File not found or invalid at path: " + filePath.toAbsolutePath());
                            }
                            // Determine content type based on "format" if present
                            String format = formDataNode.has("format") ? formDataNode.get("format").asText() : "";
                            String contentType;
                            if ("TAR_GZ".equalsIgnoreCase(format)) {
                                contentType = HttpFormData.CONTENT_TYPE_GZIP;
                            } else {
                                // Fallback — treat as ZIP if unknown default
                                contentType = HttpFormData.CONTENT_TYPE_ZIP;
                            }

                            // Add the file part
                            formData.addPart(new FormDataFile(key, filePath.getFileName().toString(), filePath, contentType));
                        }
                        // For all other keys → treat as normal form string
                        else {
                            String value = valueNode.asText();
                            formData.addPart(new FormDataString(key, value));
                        }
                    }
                }
                Map<String, String> headerMap = new HashMap<>();

                if (requestNode.has("headers") && requestNode.get("headers").isArray()) {
                    for (JsonNode headerNode : requestNode.get("headers")) {
                        if (headerNode.has("key") && headerNode.has("value")) {
                            String key = headerNode.get("key").asText();
                            String value = headerNode.get("value").asText();
                            headerMap.put(key, value);
                        }
                    }
                }
                if (!headerMap.isEmpty()) {
                    StringBuilder headerLog = new StringBuilder("Request Headers:\n");
                    headerMap.forEach((key, value) -> headerLog.append("  ").append(key).append(": ").append(value).append("\n"));
                    if (logReqHeaders)
                        logger.info(headerLog.toString().trim());
//                logger.debug(headerLog.toString().trim());
                }

                step.getLogger().debug("initiate REST api client");
                BaseHttpClient.Builder builder = BaseHttpClient.withBuilder().withLogger(step.getLogger()).withConnectTimeout(Duration.ofSeconds(30L));
                this.client =  builder.build();

                try {

                    HttpExecutionResult<byte[]> result = null;
                    // this part will be updated when I've the idea what will happen when header=null is passed
                    if(method!=null && !method.trim().isEmpty()) {
                        if (method.equalsIgnoreCase("post")) {
                            if (requestNode.has("formData") ){
                                headerMap.put( HttpUtils.HEADER_CONTENT_TYPE, formData.getContentType());
                                result = client.executePOST(uri, headerMap, formData == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArrays(formData), HttpResponse.BodyHandlers.ofByteArray());
                            }else if(!requestNode.has("formData") ){
                                  result = client.executePOST(uri, headerMap, bodyStr == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(bodyStr),HttpResponse.BodyHandlers.ofByteArray());
                            }
                        }
                        else if (method.equalsIgnoreCase("put")) {
                             if (requestNode.has("formData")){
                                 headerMap.put( HttpUtils.HEADER_CONTENT_TYPE, formData.getContentType());
                                result = client.executePUT(uri,headerMap, formData == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArrays(formData), HttpResponse.BodyHandlers.ofByteArray() );
                            }else if(!requestNode.has("formData")  ){
                                //when url, header body=string is present-
                                result = client.executePUT(uri,headerMap, bodyStr == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(bodyStr), HttpResponse.BodyHandlers.ofByteArray());
                            }
                        }
                        else if (method.equalsIgnoreCase("get")) {
                            if (!headerMap.isEmpty()) {
                                //when url and headers are present
                                result = client.executeGET(uri, headerMap, HttpResponse.BodyHandlers.ofByteArray());
                            }else {
                                // only url is present - this will be removed if the header=null will also send the rest call without issue from the internal code side and request sent to the server
                                result = client.executeGET(uri,HttpResponse.BodyHandlers.ofByteArray());
                            }
                        }
                        else if (method.equalsIgnoreCase("delete")) {
                            if (!headerMap.isEmpty()){
                                //when url and headers are present
                                result = client.executeDELETE(uri, headerMap, HttpResponse.BodyHandlers.ofByteArray());
                            }
                            else{
                                // only url is present - this will be removed if the header=null will also send the rest call without issue from the internal code side and request sent to the server
                                result = client.executeDELETE(uri, HttpResponse.BodyHandlers.ofByteArray());
                            }
                        } else {
                            throw new JobArgumentException("Unknown http method: " + method);
                        }
                    }else {
                        throw new JobArgumentException("Http Method (POST, PUT, GET, DELETE) missing. Request cannot be executed.");
                    }

                    int statusCode=-1;
                    if (result != null && result.response()!=null) {
                        statusCode = result.response().statusCode();
                    }
                    if (statusCode >= 200 && statusCode < 300) {
                        logger.info("REST call successful. Status Code: " + statusCode);
                    } else {
                        throw new JobException("REST call unsuccessful. Status code: " + statusCode);
                    }

                    HttpResponse<byte[]> response =  result.response();

                    Map<String, String> resHeaders =response.headers().map().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entryx) -> String.join(", ",entryx.getValue())));
                    String contentType = null;
                    for(Map.Entry<String, String> entry : resHeaders.entrySet()) {
                            if ("content-type".equalsIgnoreCase((String)entry.getKey())) {
                                contentType = (String)entry.getValue();
                                break;
                            }
                    }

                    if (!resHeaders.isEmpty()) {
                        StringBuilder headerLog = new StringBuilder("Response Headers:\n");
                        resHeaders.forEach((key, value) -> headerLog.append("  ").append(key).append(": ").append(value).append("\n"));
                        if (logResHeaders)
                            logger.info(headerLog.toString().trim());
//                    logger.debug(headerLog.toString().trim());
                    }
                    String responseBody=null;
                    if (contentType != null && (contentType.contains("application/json") || contentType.toLowerCase().startsWith("text/"))){
                        responseBody = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
                        if (contentType.contains("application/json") && !responseBody.trim().isEmpty()) {
                            Object json = objectMapper.readValue(responseBody, Object.class);
                            if (logResBody)
                                logger.info("Response Body: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                            logger.debug("Response Body: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                        } else if (contentType.toLowerCase().startsWith("text/") && !responseBody.trim().isEmpty()){
                            if (logResBody)
                                logger.info("Response Body : \n" +responseBody);
                            logger.debug("Response Body : \n" + responseBody);
                        }
                    }

                    //Return Variable
                    String jqQuery = null;
                    String pI = null;
                    String filePath = null;
                    String returnVarJson = (String)myArgs.getReturnVariable().getValue();
                    String varName = null;
                    String path = null;
                    String name = null;
                    JsonNode responseJson = null;
                    String inputOption = null;
                    boolean rawOutput = false;

                    //Return variable -check if return variable is available or not
                    if (returnVarJson != null && !returnVarJson.trim().isEmpty()) {
                        JsonNode returnVars;
                        try {
                            returnVars = objectMapper.readTree(returnVarJson);
                        } catch (Exception e) {
                            throw new JobException("Invalid JSON in 'return_variable': " + e.getMessage(), e);
                        }

                        //check if it is a valid json array or not
                        if (returnVars.isArray()) {

                            ReturnVariableUtils.checkDuplicateReturnVariable(returnVars);
                            for (JsonNode mappingNode : returnVars) {
                                jqQuery = null;
                                pI = null;
                                filePath = null;
                                name = null;
                                path = null;
                                inputOption = null;
                                rawOutput = false;

                                name = mappingNode.has("name") ? mappingNode.get("name").asText().trim() : null;
                                path = mappingNode.has("path") ? mappingNode.get("path").asText().trim() : null;

                                //check if the JSON object have both name and path
                                if (name != null && !name.isBlank() && path != null && !path.isBlank()) {
                                    String inputType = "json"; // default

                                    if (path.startsWith("<")) {
                                        int firstSeparator = path.indexOf("||");

                                        if (firstSeparator == -1 && !path.matches("^<\\s*plain\\s*:??\\s*$")) {
                                            throw new JobArgumentException("Could not create return variable: " + name +
                                                    ". Invalid path format: Missing '||' after input option or unknown input type: " + path);
                                        }

                                        if (firstSeparator != -1) {
                                            String inputSegment = path.substring(1, firstSeparator).trim(); // skip '<'
                                            path = path.substring(firstSeparator + 2).trim(); // rest: jq or file ops or both

                                            if (inputSegment.startsWith("plain:")) {
                                                inputType = "plain";
                                                inputOption = inputSegment.substring("plain:".length()).trim();
                                            } else if (inputSegment.startsWith("json:")) {
                                                inputType = "json";
                                                inputOption = inputSegment.substring("json:".length()).trim();
                                            } else if (inputSegment.matches("^plain\\s*:??\\s*")) {
                                                inputType = "plain";
                                                inputOption = "--response-body";
                                            } else {
                                                inputOption = inputSegment;
                                            }
                                            if (inputOption.isEmpty()) {
                                                inputOption = "--response-body";
                                            }

                                        } else {
                                            // No || after input type, assume just "< plain"
                                            inputType = "plain";
                                            inputOption = "--response-body";
                                            path = ""; // no jq, no PI
                                        }
                                    } else {
                                        // No < at all: default input type and option
                                        inputType = "json";
                                        inputOption = "--response-body";
                                    }
                                    // Now handle split into jq query and file ops
                                    String[] parts = path.split("\\|\\|", 2);
                                    if(parts.length > 0 && (( inputType.equals("plain") && !parts[0].trim().startsWith(">") &&  !parts[0].trim().startsWith("-r"))||inputType.equals("json") )){
                                        jqQuery = parts[0].trim();
                                    }

                                    if (parts.length > 1 || (parts.length == 1 && (
                                            parts[0].trim().startsWith(">")
                                                    || parts[0].trim().startsWith(">>")
                                                    || parts[0].trim().startsWith("-r")
                                                    || parts[0].trim().startsWith("--raw-output")))) {

                                        String processorString = (parts.length > 1) ? parts[1].trim() : parts[0].trim();
                                        String[] processorParts = processorString.split("\\s+");

                                        for (int i = 0; i < processorParts.length; i++) {
                                            String token = processorParts[i].trim();

                                            if (token.equals("-r") || token.equals("--raw-output")) {
                                                rawOutput = true;
                                            } else if ((token.equals(">") || token.equals(">>")) && i + 1 < processorParts.length) {
                                                String potentialFilePath = processorParts[i + 1].trim();
                                                if (potentialFilePath.startsWith("-")) {
                                                    throw new JobArgumentException("Could not create return variable: " + name +
                                                            ". Invalid file path: found flag instead of file name after '" + token + "'");
                                                }
                                                pI = token;
                                                filePath = potentialFilePath;
                                                i++;
                                            } else if (token.matches("^[A-Za-z]:\\\\.*|^/.*")) {
                                                throw new JobArgumentException("File path provided without Processing Instruction (>, >>): " + token);
                                            }
                                        }
                                    }

                                    // check if jq is not null
                                    if(jqQuery!=null && !jqQuery.trim().isEmpty()){
                                        JsonNode jqInput = null;
                                        ObjectNode mergedInput = objectMapper.createObjectNode();
                                        List<JsonNode> jqResult=null;
                                        if (Objects.equals(inputType, "plain")) {
                                            if (responseBody != null && !responseBody.trim().isEmpty()) {
                                                if (contentType != null && contentType.toLowerCase().startsWith("text/")) {
                                                    try {
                                                        jqInput = objectMapper.readTree("\"" + responseBody + "\"");
                                                    } catch (Exception e) {
                                                        throw new JobArgumentException("Could not convert plain response body to JSON string: " + e.getMessage(), e);
                                                    }
                                                } else {
                                                    throw new JobArgumentException("Response body is not of content type text/*: " + contentType);
                                                }
                                            } else {
                                                throw new JobException("Empty Response Body!");
                                            }
                                            jqResult=ReturnVariableUtils.runJqQuery(jqInput,jqQuery, rootScope, name);
                                        }
                                        else {
                                            List<String> inputOptions = null;
                                            if (!inputOption.trim().isEmpty()) {
                                                inputOptions = ReturnVariableUtils.parseInputOptions(inputOption);
                                            }
                                            if (!inputOptions.isEmpty()) {
                                                if (responseBody != null && !responseBody.trim().isEmpty()) {
                                                    if (contentType.contains("application/json")) {
                                                        jqInput = objectMapper.readTree(responseBody);
                                                        if (jqInput.isObject()) {
                                                            mergedInput.setAll((ObjectNode) jqInput);
                                                        }
                                                    }
                                                }
                                                for (String opt : inputOptions) {
                                                    if (opt.startsWith("--response-body") || opt.startsWith("-b"))continue;
                                                    else if (opt.startsWith("--response-header") || opt.startsWith("-s")) {
                                                        ObjectNode headerObject = objectMapper.createObjectNode();
                                                        for (Map.Entry<String, String> entry : resHeaders.entrySet()) {
                                                            headerObject.put(entry.getKey().toLowerCase(), entry.getValue());
                                                        }
                                                        mergedInput.set("js7ResponseHeader", headerObject);
                                                    }
                                                    else if (opt.startsWith("--request-header") || opt.startsWith("-q")) {
                                                        ObjectNode headerObject = objectMapper.createObjectNode();
                                                        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                                                            headerObject.put(entry.getKey().toLowerCase(), entry.getValue());
                                                        }
                                                        mergedInput.set("js7RequestHeader", headerObject);
                                                    }
                                                    else if (opt.equals("--request-body") || opt.equals("-d")) {
                                                        if (bodyStr != null && !bodyStr.trim().isEmpty()) {
                                                            JsonNode requestBodyJson = objectMapper.readTree(bodyStr);
                                                            if (!requestBodyJson.isObject()) {
                                                                throw new JobArgumentException("Request body must be a JSON object. Found: " + requestBodyJson.getNodeType());
                                                            }
                                                            mergedInput.set("js7RequestBody", requestBodyJson);
                                                        } else {
                                                            throw new JobArgumentException("Empty/null Request body." );
                                                        }
                                                    }
                                                    else if (opt.startsWith("--from-json=") || opt.startsWith("-j=")) {
                                                        String jsonStr = opt.substring(opt.indexOf('=') + 1).trim();

                                                        // Handle optional quotes
                                                        if ((jsonStr.startsWith("'") && jsonStr.endsWith("'")) ||
                                                                (jsonStr.startsWith("\"") && jsonStr.endsWith("\""))) {
                                                            jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                                                        }

                                                        try {
                                                            JsonNode fromJsonNode = objectMapper.readTree(jsonStr);

                                                            if (!fromJsonNode.isObject()) {
                                                                throw new JobArgumentException("Expected JSON object for --from-json, but found: " + fromJsonNode.getNodeType());
                                                            }

                                                            // Check for duplicate keys before merging
                                                            Iterator<String> fieldNames = fromJsonNode.fieldNames();
                                                            while (fieldNames.hasNext()) {
                                                                String fieldName = fieldNames.next();
                                                                if (mergedInput.has(fieldName)) {
                                                                    throw new JobArgumentException("Key conflict during input merge: " + fieldName);
                                                                }
                                                            }

                                                            ((ObjectNode) mergedInput).setAll((ObjectNode) fromJsonNode);

                                                        } catch (Exception e) {
                                                            logger.error("Invalid JSON for input option: " + jsonStr);
                                                            throw e;
                                                        }
                                                    }
                                                    else {
                                                        throw new JobArgumentException("Could not create return variable: " + name + ". Unsupported input option: " + opt);
                                                    }
                                                    if (jqInput != null) {
                                                        if (!jqInput.isObject()) {
                                                            throw new JobArgumentException("Input from option [" + opt + "] must be a JSON object. Found: " + jqInput.getNodeType());
                                                        }
                                                        mergedInput.setAll((ObjectNode) jqInput);
                                                    }
                                                }
                                            }
                                            else {
                                                if (responseBody != null && !responseBody.trim().isEmpty()) {
                                                    if (contentType.contains("application/json")) {
                                                        jqInput = objectMapper.readTree(responseBody);
                                                        if (!jqInput.isObject()) {
                                                            throw new JobArgumentException("Response body must be a JSON object.");
                                                        }
                                                        mergedInput.setAll((ObjectNode) jqInput);
                                                    } else {
                                                        throw new JobArgumentException("Could not create return variable: " + name + " because response body is not in JSON format");
                                                    }
                                                } else {
                                                    throw new JobArgumentException("Empty response body, could not create return variable: " + name);
                                                }
                                            }
                                            jqResult=ReturnVariableUtils.runJqQuery(mergedInput,jqQuery, rootScope, name);
                                        }
                                        ReturnVariableUtils.writeToFile(step, logger, name, filePath, pI, jqResult, rawOutput, objectMapper);
                                    }
                                    else if(inputType.equals("plain") ) {
                                        if( contentType != null && contentType.toLowerCase().startsWith("text/")  )
                                            ReturnVariableUtils.writeToFile(step, logger, name, filePath, pI, responseBody, rawOutput, objectMapper);
                                        else
                                            throw new JobArgumentException("Error in extracting return variable " + name+ ", response body is not in text format.");
                                    }
                                }
                            }
                        }
                        else {
                            logger.error("return_variable is not a valid JSON array");
                            step.getOutcome().setReturnCode(1);
                        }
                    }

                } catch (IOException e) {
                    throw new JobException("REST call failed due to I/O error: " + e.getMessage(), e);
                }
            }
        } else {
            throw new JobRequiredArgumentMissingException("Missing request JSON in job arguments.");
        }
    }

    static {
        BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, rootScope);
        objectMapper = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    }
}
