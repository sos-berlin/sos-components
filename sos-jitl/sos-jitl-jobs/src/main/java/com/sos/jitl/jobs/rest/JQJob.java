package com.sos.jitl.jobs.rest;

import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobException;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;

public class JQJob extends Job<JQJobArguments> {

	public static final ObjectMapper objectMapper;
	private static final Scope rootScope = Scope.newEmptyScope();

	public JQJob(JobContext jobContext) {
		super(jobContext);
	}

	public void processOrder(OrderProcessStep<JQJobArguments> step) throws Exception {

		JQJobArguments myArgs = (JQJobArguments) step.getDeclaredArguments();
		OrderProcessStepLogger logger = step.getLogger();

		// Return Variable
		String jqQuery = null;
		String pI = null;
		String filePath = null;
		String out = myArgs.getOutputVariable().getValue();
		String path = null;
		String name = null;
		boolean rawOutput = false;

		if (out != null && !out.trim().isEmpty()) {
			JsonNode returnVars;
			try {
				returnVars = objectMapper.readTree(out);
			} catch (Exception e) {
				throw new JobException("Invalid JSON in 'out': " + e.getMessage(), e);
			}

			if (returnVars.isArray()) {
				ReturnVariableUtils.checkDuplicateReturnVariable(returnVars);
				for (JsonNode mappingNode : returnVars) {
					jqQuery = null;
					pI = null;
					filePath = null;
					name = null;
					path = null;
					rawOutput = false;

					name = mappingNode.has("name") ? mappingNode.get("name").asText().trim() : null;
					path = mappingNode.has("path") ? mappingNode.get("path").asText().trim() : null;

					// check if the JSON object have both name and path
					if (name != null && !name.isBlank() && path != null && !path.isBlank()) {
						// Now handle split into jq query and file ops
						String[] parts = path.split("\\|\\|", 2);
						jqQuery = parts[0].trim();
						String processorString = (parts.length == 2) ? parts[1].trim() : "";
						if (!processorString.isBlank()) {

							String[] tokens = processorString.split("\\s+");

							for (int i = 0; i < tokens.length; i++) {
								String token = tokens[i].trim();

								if (token.equals("-r") || token.equals("--raw-output")) {
									rawOutput = true;
									continue;
								}
								if ((token.equals(">") || token.equals(">>"))) {

									if (i + 1 >= tokens.length) {
										throw new JobArgumentException(
												"Missing file name after '" + token + "' for variable: " + name);
									}

									String potentialFilePath = tokens[i + 1].trim();

									// File path must NOT look like a flag
									if (potentialFilePath.startsWith("-")) {
										throw new JobArgumentException(
												"Invalid file path: found flag instead of file name after '" + token
														+ "'");
									}

									pI = token;
									filePath = potentialFilePath;
									i++; // skip next token
									continue;
								}

								if (token.matches("^[A-Za-z]:\\\\.*|^/.*")) {
									throw new JobArgumentException(
											"File path provided without Processing Instruction (>, >>): " + token);
								}
							}
						}
					}

					if (jqQuery != null && !jqQuery.trim().isEmpty()) {						
						List<JsonNode> jqResult = null;
						String jsonStr = myArgs.getInputVariable().getValue().trim();

						// Handle optional quotes
						if ((jsonStr.startsWith("'") && jsonStr.endsWith("'"))
								|| (jsonStr.startsWith("\"") && jsonStr.endsWith("\""))) {
							jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
						}
						JsonNode fromJsonNode = objectMapper.readTree(jsonStr);
						ObjectNode mergedInput = objectMapper.createObjectNode();
						if (fromJsonNode.isObject()) {
						    mergedInput.setAll((ObjectNode) fromJsonNode);
						}
						else if (fromJsonNode.isArray()) {
						    mergedInput.set("root", fromJsonNode);  // wrap the array
						}
						else {
						    throw new JobException("Unsupported JSON type for input: " + fromJsonNode.getNodeType());
						}


						jqResult = ReturnVariableUtils.runJqQuery(mergedInput, jqQuery, rootScope, name);

						ReturnVariableUtils.writeToFile(step, logger, name, filePath, pI, jqResult, rawOutput,
								objectMapper);
					} else {
						throw new JobArgumentException(
								"Error in extracting return variable " + name + "In put json is empty or invalid");
					}
				}
			}
		} else {
			logger.error("out is not a valid JSON array");
			step.getOutcome().setReturnCode(1);
		}
	}

	static {
		BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_7, rootScope);
		objectMapper = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
	}
}
