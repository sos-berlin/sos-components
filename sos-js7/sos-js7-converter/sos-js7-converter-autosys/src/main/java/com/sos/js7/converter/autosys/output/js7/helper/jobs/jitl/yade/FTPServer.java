package com.sos.js7.converter.autosys.output.js7.helper.jobs.jitl.yade;

import java.util.ArrayList;
import java.util.List;

import com.sos.js7.converter.autosys.common.v12.job.JobFTP;

// TODO FTPS, reduce fragments
public class FTPServer {

    private String id;

    private String serverName;
    private String serverPort;
    private String serverUser;
    private String transferType;

    private List<JobFTP> fragmentJobs = new ArrayList<>();

    public FTPServer(String id, JobFTP jilJob) {
        this.id = id;
        this.serverName = jilJob.getFtpServerName().getValue();
        this.serverPort = jilJob.getFtpServerPort().getValue();
        this.serverUser = jilJob.getFtpServerUser().getValue();
        this.transferType = jilJob.getFtpTransferType().getValue();
    }

    public static String getId(JobFTP jilJob) {
        StringBuilder sb = new StringBuilder();
        sb.append(jilJob.getFtpServerName().getValue());
        if (!jilJob.getFtpServerPort().isEmpty()) {
            sb.append("-").append(jilJob.getFtpServerPort().getValue());
        }
        if (!jilJob.getFtpTransferType().isEmpty()) {
            sb.append("-").append(jilJob.getFtpTransferType().getValue());
        }
        if (!jilJob.getFtpServerUser().isEmpty()) {
            sb.append("-").append(jilJob.getFtpServerUser().getValue());
        }
        return sb.toString();
    }

    public void addFragmentJob(JobFTP jilJob) {
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

    public String getServerUser() {
        return serverUser;
    }

    public String getTransferType() {
        return transferType;
    }

    public List<JobFTP> getFragmentJobs() {
        return fragmentJobs;
    }
}
