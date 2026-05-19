package com.sos.joc.classes.controller;

import java.util.Optional;

public class ControllerCommandResponse {
    
    private final String controllerId;
    private final Optional<Exception> exception;

    public ControllerCommandResponse(String controllerId, Optional<Exception> exception) {
        this.controllerId = controllerId;
        this.exception = exception;
    }
    
    public ControllerCommandResponse(String controllerId) {
        this.controllerId = controllerId;
        this.exception = Optional.empty();
    }
    
    public String getControllerId() {
        return controllerId;
    }
    
    public Optional<Exception> getException() {
        return exception;
    }

    public boolean hasException() {
        return exception.isPresent();
    }
}
