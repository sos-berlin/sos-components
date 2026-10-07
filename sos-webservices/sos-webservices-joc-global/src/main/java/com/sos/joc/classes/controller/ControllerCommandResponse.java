package com.sos.joc.classes.controller;

import java.util.Optional;

import com.sos.joc.classes.publish.record.DeployTransportRecord;

public class ControllerCommandResponse {
    
    private final String controllerId;
    private final Optional<Exception> exception;
    private final Optional<DeployTransportRecord> deployTransportRecord;

    public ControllerCommandResponse(String controllerId) {
        this.controllerId = controllerId;
        this.exception = Optional.empty();
        this.deployTransportRecord = Optional.empty();
    }
    
    public ControllerCommandResponse(String controllerId, Optional<Exception> exception) {
        this.controllerId = controllerId;
        this.exception = exception;
        this.deployTransportRecord = Optional.empty();
    }
    
    public ControllerCommandResponse(String controllerId, Optional<Exception> exception, Optional<DeployTransportRecord> deployTransportRecord) {
        this.controllerId = controllerId;
        this.exception = exception;
        this.deployTransportRecord = deployTransportRecord;
    }
    
    public String getControllerId() {
        return controllerId;
    }
    
    public Optional<Exception> getException() {
        return exception;
    }
    
    public Optional<DeployTransportRecord> getDeployTransportRecord() {
        return deployTransportRecord;
    }

    public boolean hasException() {
        return exception.isPresent();
    }
}
