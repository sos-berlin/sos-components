class JS7Job extends ABlockingJob {
	myPublicProperty = new Date();
	LOGGER = org.slf4j.LoggerFactory.getLogger("JS7JobLogger");
	//constructor(jobEnvironment) {
	//	super(jobEnvironment);
	//}

	onStart() {
		this.LOGGER.info("[onStart]" + this.getJobEnvironment().getJobKey());
	}

	onStop() {
		this.LOGGER.info("[onStop]" + this.getJobEnvironment().getJobKey());
	}

	onOrderProcess(step) {
		step.getLogger().info("[onOrderProcess]Hallo from My Job");
		step.getLogger().info("[onOrderProcess][getJobEnvironment]" + this.getJobEnvironment());
		step.getLogger().info("[onOrderProcess][getJobEnvironment.getSystemEncoding]" + this.getJobEnvironment().getSystemEncoding());
		step.getLogger().info("[onOrderProcess][this.myPublicProperty]" + this.myPublicProperty);

		//java.lang.Thread.sleep(5*1000);

		var h = new Helper();
		h.logPublicMethods(step.getLogger(), "this.getJobEnvironment()", this.getJobEnvironment());
		h.logPublicMethods(step.getLogger(), "step", step);

		step.getOutcome().setReturnCode(100);
		step.getOutcome().putVariable("var_1", "var_1_value");
		//step.getOutcome().setFailed();
	}
}

function Sleep(milliseconds) {
	return new Promise(resolve => setTimeout(resolve, milliseconds));
}

class Helper {
	regExp = new RegExp("equals|toString|hashCode|getClass|notify|notifyAll|wait");

	logPublicMethods(logger, title, o) {
		logger.info("---------------Public Methods " + title + "--");
		var pm = com.sos.commons.util.SOSReflection.getAllMethods(o.getClass());
		for (var i in pm) {
			var m = pm[i];
			if (this.regExp.test(m.getName())) {
				continue;
			}
			logger.info(" " + m);
		}
	}
}
