class JS7Job extends js7.Job {

	processOrder(step) {
		var apiExecutor = new com.sos.js7.job.jocapi.ApiExecutor(step.getLogger());
		var accessToken = null;
		try {
			accessToken = apiExecutor.login().getAccessToken();
			step.getLogger().info("[accessToken]" + accessToken);

			var response = apiExecutor.post(accessToken, "/monitoring/controllers", '{"controllerId":"' + step.getControllerId() + '"}');
			step.getLogger().info("[response.getResponseBody]" + response.getResponseBody());
		}
		catch (e) {
			throw e;
		}
		finally {
			apiExecutor.logout(accessToken);
			apiExecutor.close();
		}

	}
}

