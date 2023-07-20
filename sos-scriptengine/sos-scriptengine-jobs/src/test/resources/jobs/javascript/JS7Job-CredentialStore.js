class JS7JobArguments {

	includedArguments = [js7.IncludableArgument.CREDENTIAL_STORE];
}

class JS7Job extends js7.Job {
	declaredArguments = new JS7JobArguments();

	processOrder(step) {
		var csa = step.getIncludedArguments(js7.IncludableArgument.CREDENTIAL_STORE);
		if (csa.getFile().getValue() != null) {//credential_store_file,credential_store_entry_path etc arguments are set
			var resolver = csa.newResolver();
			var title = resolver.resolve("cs://@title");
			var url = resolver.resolve("cs://@url");
			step.getLogger().info("title=" + title + ", url=" + url);
		}
	}
}

/** without includedArguments 
 * 1 - rename the existing JS7Job class above to another name 
 * 2 - rename JS7JobWithoutIncludedArguments class to JS7Job
*/
class JS7JobWithoutIncludedArguments extends js7.Job {

	processOrder(step) {
		var csa = new com.sos.commons.credentialstore.CredentialStoreArguments();
		csa.setFile("database.kdbx");
		csa.setPassword("password");
		csa.setEntryPath("/server/SFTP/localhost");
		//csa.setKeyFile("database.key");

		var resolver = csa.newResolver();
		var title = resolver.resolve("cs://@title");
		var url = resolver.resolve("cs://@url");
		step.getLogger().info("title=" + title + ", url=" + url);
	}
}
