var logger = step.getLogger();
logger.info("----------------------------Infos--");
logger.info("controllerId=" + step.getControllerId());
logger.info("agentId=" + step.getAgentId());
logger.info("workflow name=" + step.getWorkflowName() + ",versionId=" + step.getWorkflowVersionId() + ",position=" + step.getWorkflowPosition());
logger.info("orderId=" + step.getOrderId());
logger.info("job name=" + step.getJobName() + ", label=" + step.getJobInstructionLabel());

var args = step.getAllArgumentsAsNameValueMap();
args.remove("script");
logger.info("----------------------------getAllArgumentsAsNameValueMap--");
logger.info("getAllArgumentsAsNameValueMap:" + args);
logger.info("getAllArgumentsAsNameValueMap iterator:");
for (var i in args) {
	logger.info(" " + i + "=" + args[i]);
}

logger.info("----------------------------getAllArguments--");
args = step.getAllArguments();
logger.info("getAllArguments:" + args);
logger.info("getAllArguments iterator:");
for (var i in args) {
	logger.info(" " + i + "=" + args[i]);
}

logger.info("----------------------------getLastOutcomes--");
var lastOutcomes = step.getLastOutcomes();
for (var i in lastOutcomes) {
	logger.info(" " + i + "=" + lastOutcomes[i]);
}

logger.info("----------------------------getLastSucceededOutcomes--");
lastOutcomes = step.getLastSucceededOutcomes();
for (var i in lastOutcomes) {
	logger.info(" " + i + "=[" + lastOutcomes[i] + "]");
}

logger.info("----------------------------getLastFailedOutcomes--");
lastOutcomes = step.getLastFailedOutcomes();
for (var i in lastOutcomes) {
	logger.info(" " + i + "=[" + lastOutcomes[i] + "]");
}

logger.info("----------------------------getEngineHistoricOutcomes--");
lastOutcomes = step.getEngineHistoricOutcomes();
for (var i in lastOutcomes) {
	logger.info(" " + lastOutcomes[i]);
}


outcome.putVariable("var_1", "var_1_value");
outcome.putVariable("var_2", "var_2_value");
outcome.setReturnCode(100);