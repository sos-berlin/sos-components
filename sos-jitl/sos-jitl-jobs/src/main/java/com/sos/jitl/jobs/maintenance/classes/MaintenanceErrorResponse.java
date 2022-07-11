package com.sos.jitl.jobs.maintenance.classes;

public class MaintenanceErrorResponse {

    private String message="";
    private String role="";
    private MaintenanceError error=new MaintenanceError();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public MaintenanceError getError() {
        return error;
    }

    public void setError(MaintenanceError error) {
        this.error = error;
    }

}
