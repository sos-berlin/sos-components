package com.sos.jitl.jobs.checkhistory;

import java.nio.file.Path;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class CheckHistoryJobArguments extends JobArguments {

	private JobArgument<Path> credentialStoreFile = new JobArgument<Path>("credential_store_file", false,
			Job.getAgentHibernateFile());
	private JobArgument<Path> credentialStoreKeyFile = new JobArgument<Path>("credential_store_key_file", false);
	private JobArgument<String> credentialStorePassword = new JobArgument<String>("credential_store_password", false);
	private JobArgument<String> credentialStoreEntryPath = new JobArgument<String>("credential_store_entry_path",
			false);
	private JobArgument<String> controller = new JobArgument<String>("controller", false);
	private JobArgument<String> workflowPath = new JobArgument<String>("workflow_path", false);
	private JobArgument<String> jocUrl = new JobArgument<String>("joc_url", false);
	private JobArgument<String> account = new JobArgument<String>("account", false);
	private JobArgument<String> password = new JobArgument<String>("password", false);
	private JobArgument<String> query = new JobArgument<String>("query", false);
	private JobArgument<String> interval = new JobArgument<String>("interval", false);
	private JobArgument<String> fromDate = new JobArgument<String>("fromDate", false);
	private JobArgument<String> toDate = new JobArgument<String>("toDate", false);
	private JobArgument<String> keystore_password = new JobArgument<String>("keystore_password", false);
	private JobArgument<String> keyPassword = new JobArgument<String>("key_password", false);
	private JobArgument<Path> keystorePath = new JobArgument<Path>("keystore_path", false);
	private JobArgument<String> keystoreType = new JobArgument<String>("keystore_type", false);
	private JobArgument<String> truststorePassword = new JobArgument<String>("truststore_password", false);
	private JobArgument<Path> truststorePath = new JobArgument<Path>("truststore_path", false);
	private JobArgument<String> truststoreType = new JobArgument<String>("truststore_type", false);

	public CheckHistoryJobArguments() {
		super(new SOSCredentialStoreArguments());
	}

	public String getController() {
		return controller.getValue();
	}

	public void setController(String controller) {
		this.controller.setValue(controller);
	}


	public Path credentialStoreFile() {
		return credentialStoreFile.getValue();
	}

	public void setCredentialStoreFile(Path credentialStoreFile) {
		this.credentialStoreFile.setValue(credentialStoreFile);
	}

	public String getCredentialStorePassword() {
		return credentialStorePassword.getValue();
	}

	public void setCredentialStorePassword(String credentialStorePassword) {
		this.credentialStorePassword.setValue(credentialStorePassword);
	}

	public Path getCredentialStoreKeyFile() {
		return credentialStoreKeyFile.getValue();
	}

	public void setCredentialStoreKeyFile(Path credentialStoreKeyFile) {
		this.credentialStoreKeyFile.setValue(credentialStoreKeyFile);
	}

	public String getCredentialStoreEntryPath() {
		return credentialStoreEntryPath.getValue();
	}

	public void setCredentialStoreEntryPath(String credentialStoreEntryPath) {
		this.credentialStoreEntryPath.setValue(credentialStoreEntryPath);
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

	public String getKeystore_password() {
		return keystore_password.getValue();
	}

	public void setKeystore_password(String keystore_password) {
		this.keystore_password.setValue(keystore_password);
	}

	public String getKeyPassword() {
		return keyPassword.getValue();
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword.setValue(keyPassword);
	}

	public Path getKeystorePath() {
		return keystorePath.getValue();
	}

	public void setKeystorePath(Path keystorePath) {
		this.keystorePath.setValue(keystorePath);
	}

	public String getKeystoreType() {
		return keystoreType.getValue();
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType.setValue(keystoreType);
	}

	public String getTruststorePassword() {
		return truststorePassword.getValue();
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword.setValue(truststorePassword);
	}

	public Path getTruststorePath() {
		return truststorePath.getValue();
	}

	public void setTruststorePath(Path truststorePath) {
		this.truststorePath.setValue(truststorePath);
	}

	public String getTruststoreType() {
		return truststoreType.getValue();
	}

	public void setTruststoreType(String truststoreType) {
		this.truststoreType.setValue(truststoreType);
	}

}
