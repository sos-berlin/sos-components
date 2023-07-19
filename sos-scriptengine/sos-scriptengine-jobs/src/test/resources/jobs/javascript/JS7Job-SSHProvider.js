class JS7JobArguments {

	includedArguments = [js7.IncludableArgument.SSH_PROVIDER];

	// see JS7JobWithCredentialStore
	//includedArguments = [js7.IncludableArgument.SSH_PROVIDER, js7.IncludableArgument.CREDENTIAL_STORE];
}

/** without credential store arguments */
class JS7Job extends js7.Job {
	declaredArguments = new JS7JobArguments();

	processOrder(step) {
		var ssha = step.getIncludedArguments(js7.IncludableArgument.SSH_PROVIDER);
		var sshProvider = new com.sos.commons.vfs.ssh.SSHProvider(ssha);


		sshProvider.connect();

		sshProvider.disconnect();

	}
}

/** with credential store arguments 
 * 1 - activate includedArguments = [js7.IncludableArgument.SSH_PROVIDER, js7.IncludableArgument.CREDENTIAL_STORE]
 * 2 - rename the existing JS7Job class above to another name 
 * 3 - rename JS7JobWithCredentialStore class to JS7Job
*/
class JS7JobWithCredentialStore extends js7.Job {
	declaredArguments = new JS7JobArguments();

	processOrder(step) {
		var ssha = step.getIncludedArguments(js7.IncludableArgument.SSH_PROVIDER);
		var csa = step.getIncludedArguments(js7.IncludableArgument.CREDENTIAL_STORE)

		var sshProvider = new com.sos.commons.vfs.ssh.SSHProvider(ssha, csa);
		sshProvider.connect();

		sshProvider.disconnect();

	}
}
