class JS7JobArguments {
	my_arg1 = new js7.JobArgument("my_arg1", false,new java.io.File("x.txt"));
	my_arg2 = new js7.JobArgument("my_arg2", true, "x", js7.DisplayMode.UNMASKED);
    my_arg3 = new js7.JobArgument("op_arg_final", false);
    my_arg4 = new js7.JobArgument("op_arg_string", false);
    my_arg5 = new js7.JobArgument("op_arg_numeric", false);
    my_arg6 = new js7.JobArgument("op_arg_boolean", false);
    my_arg7 = new js7.JobArgument("op_arg_list", false);

	//includedArguments = [js7.IncludableArgument.CREDENTIAL_STORE, js7.IncludableArgument.SSH_PROVIDER];
}

class JS7Job extends js7.Job {
	declaredArguments = new JS7JobArguments();

	processOrder(step) {
		step.getLogger().info("[onOrderProcess]Hallo from My Job");
		step.getLogger().info("[onOrderProcess][getJobEnvironment]" + this.getJobEnvironment());
		step.getLogger().info("[onOrderProcess][getJobEnvironment.getSystemEncoding]" + this.getJobEnvironment().getSystemEncoding());

		//java.lang.Thread.sleep(5*1000);
		var da = step.getDeclaredArgument(this.declaredArguments.my_arg2.name);
		step.getLogger().info("[onOrderProcess][declaredArgument=" + da.getName() + "]" + da.getValue());
		step.getLogger().info("[onOrderProcess][declaredArgumentValue]" + (typeof step.getDeclaredArgumentValue(this.declaredArguments.my_arg2.name)));

		var lh = new LogHelper();
		lh.logPublicMethods(step.getLogger(), "this.getJobEnvironment()", this.getJobEnvironment());
		lh.logPublicMethods(step.getLogger(), "step", step);
		lh.logArguments(step);

		step.getOutcome().setReturnCode(100);
		step.getOutcome().putVariable("var_1", "var_1_value");
		//step.getOutcome().setFailed();
	}
}

class LogHelper {
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
