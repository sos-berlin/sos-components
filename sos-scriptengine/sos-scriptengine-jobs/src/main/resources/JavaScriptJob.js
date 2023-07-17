function getJS7Job(jobEnvironment) {
	return new JS7Job(jobEnvironment);
}

class ABlockingJob {
	#jobEnvironment;

	constructor(jobEnvironment) {
		this.#jobEnvironment = jobEnvironment;
	}

	onOrderProcess(_step) { }

	getJobEnvironment() {
		return this.#jobEnvironment;
	}
}
