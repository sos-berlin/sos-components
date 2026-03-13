package com.sos.jitl.jobs.diagnosis;

import com.sos.js7.job.JobArguments;
import com.sos.js7.job.JobArgument;

public class CollectLogsArguments extends JobArguments{

	private final JobArgument<String> archiveLogs = new JobArgument<>("archive_logs", false);

	public JobArgument<String> getArchiveLogs() {
		return archiveLogs;
	}
	
}
