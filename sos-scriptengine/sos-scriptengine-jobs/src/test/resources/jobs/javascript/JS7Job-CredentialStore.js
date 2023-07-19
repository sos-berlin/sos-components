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

