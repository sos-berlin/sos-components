package com.sos.yade.engine.result;

import com.sos.yade.commons.result.YadeTransferResult;

public class YadeEngineTransferResult {

    private final YadeTransferResult transfer;

    private String mandator;
    private String controllerId;
    private String workflow;
    private String job;
    private String jobPosition;

    public YadeEngineTransferResult(YadeTransferResult transfer) {
        this.transfer = transfer;
    }

    public YadeTransferResult getTransfer() {
        return transfer;
    }

    public String getMandator() {
        return mandator;
    }

    public void setMandator(String val) {
        mandator = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String val) {
        workflow = val;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String val) {
        job = val;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String val) {
        jobPosition = val;
    }

}
