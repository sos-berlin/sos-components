package com.sos.yade.engine.exceptions;

import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineConnectionException extends YADEEngineException {

    private static final long serialVersionUID = 1L;

    private boolean alternativeProfile;

    public YADEEngineConnectionException(String msg, Throwable ex, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, ex, returnCode, delegator);
    }

    public YADEEngineConnectionException(Throwable ex, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(ex, returnCode, delegator);
    }

    public boolean needsAlternativeProfile() {
        return alternativeProfile;
    }

    public void setNeedsAlternativeProfile() {
        alternativeProfile = true;
    }

}
