package com.sos.jitl.jobs.runreports;

import java.util.List;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class RunReportJobArguments extends JobArguments {

    private JobArgument<List<String>> reportPaths = new JobArgument<List<String>>("report_paths", false);
    private JobArgument<List<String>> reportFolders = new JobArgument<List<String>>("report_folders", false);

    public JobArgument<List<String>> getReportPaths() {
        return reportPaths;
    }

    public void setReportPaths(JobArgument<List<String>> reportPaths) {
        this.reportPaths = reportPaths;
    }

    public JobArgument<List<String>> getReportFolders() {
        return reportFolders;
    }

    public void setReportFolders(JobArgument<List<String>> reportFolders) {
        this.reportFolders = reportFolders;
    }

}
