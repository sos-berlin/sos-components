class JS7Job extends ABlockingJob {

	onOrderProcess(step) {
		step.getLogger().info("[onOrderProcess]Hallo from My Job");
		step.getLogger().info("[onOrderProcess][getJobEnvironment]" + this.getJobEnvironment());
		step.getLogger().info("[onOrderProcess][getJobEnvironment.getSystemEncoding]" + this.getJobEnvironment().getSystemEncoding());
		step.getLogger().info("[onOrderProcess][this.myPublicProperty]" + this.myPublicProperty);

		//java.lang.Thread.sleep(5*1000);

		var h = new Helper();
		h.logPublicMethods(step.getLogger(), "this.getJobEnvironment()", this.getJobEnvironment());
		h.logPublicMethods(step.getLogger(), "step", step);
		h.logArguments(step);

		step.getOutcome().setReturnCode(100);
		step.getOutcome().putVariable("var_1", "var_1_value");
		//step.getOutcome().setFailed();
	}
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

	logArguments(step) {
		step.getLogger().info("---------------All Arguments--");
		var args = step.getAllArguments();
		step.getLogger().info("getAllArguments:");
		for (var a in args) {
			step.getLogger().info(" " + a + "=" + args[a]);
		}

		step.getLogger().info("---------------Declared Arguments--");
		args = step.getDeclaredArguments();
		step.getLogger().info("getDeclaredArguments:");
		step.getLogger().info(" " + args);
		for (var a in args) {
			step.getLogger().info(" " + a + "=" + args[a]);
		}

		args = step.getAllDeclaredArguments();
		step.getLogger().info("getAllDeclaredArguments:");
		for (var a in args) {
			step.getLogger().info(" " + args[a]);
		}
	}
}
