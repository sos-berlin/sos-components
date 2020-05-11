package com.sos.jobscheduler.event.master.configuration.master;

import com.sos.commons.util.SOSString;

public class Master {

    private String jobSchedulerId;
    private String uri;// JOC
    private String clusterUri;// between masters
    private String user;
    private String password;
    private boolean useLogin;
    private boolean primary;

    public Master(String jobSchedulerId, String masterUri, String clusterUri) throws Exception {
        this(jobSchedulerId, masterUri, clusterUri, null, null);
    }

    public Master(String id, String masterUri, String masterClusterUri, String masterUser, String masterUserPassword) throws Exception {
        if (id == null) {
            throw new Exception("jobSchedulerId is NULL");
        }
        if (masterUri == null) {
            throw new Exception("masterUri is NULL");
        }

        jobSchedulerId = id.trim();
        uri = masterUri.trim();
        clusterUri = SOSString.isEmpty(masterClusterUri) ? null : masterClusterUri.trim();

        if (masterUser != null) {
            useLogin = true;
            user = masterUser.trim();
            if (masterUserPassword != null) {
                password = masterUserPassword.trim();
            }
        }
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    protected void setJobSchedulerId(String val) {
        jobSchedulerId = val;
    }

    public String getUri() {
        return uri;
    }

    public String getClusterUri() {
        return clusterUri;
    }

    public String getUri4Log() {
        if (clusterUri == null || clusterUri.equals(uri)) {
            return uri;
        }
        return new StringBuilder("uri=").append(uri).append(", clusterUri=").append(clusterUri).toString();
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean useLogin() {
        return useLogin;
    }

    protected void setPrimary(boolean val) {
        primary = val;
    }

    public boolean isPrimary() {
        return primary;
    }

    public String getType() {
        return primary ? "Primary" : "Backup";
    }

    @Override
    public String toString() {
        return String.format("%s, primary=%s", uri, primary);
    }

}
