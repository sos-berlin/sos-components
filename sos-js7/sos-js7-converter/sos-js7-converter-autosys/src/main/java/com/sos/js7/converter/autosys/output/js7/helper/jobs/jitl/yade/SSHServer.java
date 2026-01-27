package com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.yade;

import java.util.ArrayList;
import java.util.List;

import com.sos.js7.converter.autosys.common.v12.job.JobSCP;

// TODO FTPS, reduce fragments
public class SSHServer {

    private String id;

    private String serverName;
    private String serverPort;

    private List<JobSCP> fragmentJobs = new ArrayList<>();

    public SSHServer(String id, JobSCP jilJob) {
        this.id = id;
        this.serverName = jilJob.getScpServerName().getValue();
        this.serverPort = jilJob.getScpServerPort().getValue();
    }

    public static String getId(JobSCP jilJob) {
        StringBuilder sb = new StringBuilder();
        sb.append(jilJob.getScpServerName().getValue());
        if (!jilJob.getScpServerPort().isEmpty()) {
            sb.append("-").append(jilJob.getScpServerPort().getValue());
        }
        return sb.toString();
    }

    public void addFragmentJob(JobSCP jilJob) {
        fragmentJobs.add(jilJob);
    }

    public String getId() {
        return id;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public List<JobSCP> getFragmentJobs() {
        return fragmentJobs;
    }
}
