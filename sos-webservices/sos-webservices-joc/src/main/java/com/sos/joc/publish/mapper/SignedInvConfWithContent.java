package com.sos.joc.publish.mapper;

import com.sos.joc.model.publish.ControllerObject;

public class SignedInvConfWithContent {

    private ControllerObject controllerCfg;
    private String originalContent;
    
    
    public SignedInvConfWithContent (ControllerObject controllerCfg, String originalContent) {
        this.controllerCfg = controllerCfg;
        this.originalContent = originalContent;
    }
    
    public ControllerObject getControllerCfg() {
        return controllerCfg;
    }
    
    public String getOriginalContent() {
        return originalContent;
    }

}
