package com.sos.jobscheduler.event.master.configuration.master;

public class Master {

    private String jobSchedulerId;
    private String uri;
    private String user;
    private String password;
    private boolean useLogin;
    private boolean primary;

    public Master(String jobSchedulerId, String masterUri) throws Exception {
        this(jobSchedulerId, masterUri, null, null);
    }

    public Master(String id, String masterUri, String masterUser, String masterUserPassword) throws Exception {
        if (id == null) {
            throw new Exception("jobSchedulerId is NULL");
        }
        if (masterUri == null) {
            throw new Exception("jobSchedulerId is NULL");
        }

        jobSchedulerId = id.trim();
        uri = masterUri.trim();

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

    @Override
    public String toString() {
        return String.format("%s, primary=%s", uri, primary);
    }

}
