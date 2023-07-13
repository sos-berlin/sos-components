class JS7Job extends ABlockingJob {
	myPublicProperty = new Date();
	//constructor(jobEnvironment) {
	//	super(jobEnvironment);
	//}

	onStart() {
		console.log("[onStart]" + this.getJobEnvironment());
	}

	onOrderProcess(step) {
		step.getLogger().info("[onOrderProcess]Hallo from My Job");
		step.getLogger().info("[onOrderProcess][getJobEnvironment]" + this.getJobEnvironment());
		step.getLogger().info("[onOrderProcess][getJobEnvironment.getSystemEncoding]" + this.getJobEnvironment().getSystemEncoding());
		step.getLogger().info("[onOrderProcess][this.myPublicProperty]" + this.myPublicProperty);

		step.getOutcome().setReturnCode(100);
		step.getOutcome().putVariable("val_1", "val_1_value");
		step.getOutcome().setFailed();
	}
}



