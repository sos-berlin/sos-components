package com.sos.yade.engine.exceptions;

public class YADEEngineConnectionException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    private boolean alternativeProfile;

    public YADEEngineConnectionException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public YADEEngineConnectionException(Throwable ex) {
        super(ex);
    }

    public boolean needsAlternativeProfile() {
        return alternativeProfile;
    }

    public void setNeedsAlternativeProfile() {
        alternativeProfile = true;
    }

}
