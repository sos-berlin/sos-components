var step = js7OrderProcessStep;
var logger = step.getLogger();

logger.info("----------------------------JobEnvironment--");
logger.info("jobKey=" + js7JobEnvironment.getJobKey());
logger.info("systemEncoding=" + js7JobEnvironment.getSystemEncoding());
logger.info("payload=" + js7JobEnvironment.getPayload());
if (js7JobEnvironment.getPayload() != null && js7JobEnvironment.getPayload() instanceof com.sos.commons.hibernate.SOSHibernateFactory) {
	var f = js7JobEnvironment.getPayload();
	var s = f.openStatelessSession();
	var sql = "select TEXT_VALUE from JOC_VARIABLES where NAME='version'";
	var r = s.getSingleValueNativeQuery(sql);
	s.close();

	logger.info("[" + sql + "]" + r);
}


var mre = new RegExp("equals|toString|hashCode|getClass|notify|notifyAll|wait");
logger.info(" ");
logger.info(" ");
logger.info("----------------------------Public Methods--");
logger.info("---------------------------------JobEnvironment[SOS]--");
var pm = com.sos.commons.util.SOSReflection.getAllMethods(js7JobEnvironment.getClass());
for (var i in pm) {
	var m = pm[i];
	if (mre.test(m.getName())) {
		continue;
	}
	logger.info(" " + m);
}

logger.info("---------------------------------Step[SOS]--");
var pm = com.sos.commons.util.SOSReflection.getAllMethods(step.getClass());
for (var i in pm) {
	var m = pm[i];
	if (mre.test(m.getName())) {
		continue;
	}
	logger.info(" " + m);
}
logger.info(" ");
logger.info(" ");
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

logger.info("----------------------------getJobResourcesArgumentsAsNameDetailValueMap--");
var jobResources = step.getJobResourcesArgumentsAsNameDetailValueMap();
for (var i in jobResources) {
	logger.info(" " + i + "=[" + jobResources[i] + "]");
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


step.getOutcome().putVariable("var_1", "var_1_value");
step.getOutcome().putVariable("var_2", "var_2_value");
step.getOutcome().setReturnCode(100);
//step.getOutcome().setFailed();