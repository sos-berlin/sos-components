function getJS7Job(jobEnvironment) {
	return new JS7Job(jobEnvironment);
}

var js7 = {

	DisplayMode: {
		MASKED: 'MASKED',
		UNMASKED: 'UNMASKED',
		UNKNOWN: 'UNKNOWN'

	},

	IncludableArgument: {
		CREDENTIAL_STORE: 'CREDENTIAL_STORE',
		SSH_PROVIDER: 'SSH_PROVIDER'
	},

	JobArgument: class {
		name;
		required;
		defaultValue;
		displayMode;

		constructor(name, required = false, defaultValue = null, displayMode = js7.DisplayMode.UNMASKED) {
			this.name = name;
			this.required = required;
			this.defaultValue = defaultValue;
			this.displayMode = displayMode;
		}

	},

	Job: class {
		#jobEnvironment;
		declaredArguments;

		constructor(jobEnvironment) {
			this.#jobEnvironment = jobEnvironment;
		}

		processOrder(_step) { }

		getJobEnvironment() {
			return this.#jobEnvironment;
		}

		getDeclaredArguments() {
			return this.declaredArguments;
		}
	}
}