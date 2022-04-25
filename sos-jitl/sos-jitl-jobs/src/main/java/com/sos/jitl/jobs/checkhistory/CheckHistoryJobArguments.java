package com.sos.jitl.jobs.checkhistory;

import java.nio.file.Path;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class CheckHistoryJobArguments extends JobArguments {

	private JobArgument<String> controller = new JobArgument<String>("controller", false);
	private JobArgument<String> workflowPath = new JobArgument<String>("workflow_path", false);
	private JobArgument<String> jocUrl = new JobArgument<String>("joc_url", false);
	private JobArgument<String> account = new JobArgument<String>("account", false);
	private JobArgument<String> password = new JobArgument<String>("password", false);
	private JobArgument<String> query = new JobArgument<String>("query", false);
	private JobArgument<String> interval = new JobArgument<String>("interval", false);
	private JobArgument<String> fromDate = new JobArgument<String>("fromDate", false);
	private JobArgument<String> toDate = new JobArgument<String>("toDate", false);

	public CheckHistoryJobArguments() {
		super(new SOSCredentialStoreArguments());
	}

	public String getController() {
		return controller.getValue();
	}

	public void setController(String controller) {
		this.controller.setValue(controller);
	}


	public String getWorkflowPath() {
		return workflowPath.getValue();
	}

	public void setWorkflowPath(String workflowPath) {
		this.workflowPath.setValue(workflowPath);
	}

	public String getJocUrl() {
		return jocUrl.getValue();
	}

	public void setJocUrl(String jocUrl) {
		this.jocUrl.setValue(jocUrl);
	}

	public String getAccount() {
		return account.getValue();
	}

	public void setAccount(String account) {
		this.account.setValue(account);
	}

	public String getPassword() {
		return password.getValue();
	}

	public void setPassword(String password) {
		this.password.setValue(password);
	}

	public String getQuery() {
		return query.getValue();
	}

	public void setQuery(String query) {
		this.query.setValue(query);
	}

	public String getInterval() {
		return interval.getValue();
	}

	public void setInterval(String interval) {
		this.interval.setValue(interval);
	}

	public String getFromDate() {
		return fromDate.getValue();
	}

	public void setFromDate(String fromDate) {
		this.fromDate.setValue(fromDate);
	}

	public String getToDate() {
		return toDate.getValue();
	}

	public void setToDate(String toDate) {
		this.toDate.setValue(toDate);
	}


}
