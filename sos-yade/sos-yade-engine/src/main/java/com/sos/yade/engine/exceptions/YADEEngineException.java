package com.sos.yade.engine.exceptions;

import com.sos.commons.exception.SOSException;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;

public class YADEEngineException extends SOSException {

    private static final long serialVersionUID = 1L;

    private YADEReturnCode returnCode;
    private IYADEProviderDelegator delegator;

    public YADEEngineException(String msg, YADEReturnCode returnCode) {
        this(msg, returnCode, (IYADEProviderDelegator) null);
    }

    public YADEEngineException(String msg, IYADEProviderDelegator delegator) {
        this(msg, YADEReturnCode.DEFAULT_ERROR, delegator);
    }

    public YADEEngineException(String msg, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg);
        this.returnCode = returnCode;
        this.delegator = delegator;
    }

    public YADEEngineException(String msg, Throwable e) {
        this(msg, e, YADEReturnCode.DEFAULT_ERROR);
    }

    public YADEEngineException(String msg, Throwable e, YADEReturnCode returnCode) {
        this(msg, e, returnCode, (IYADEProviderDelegator) null);
    }

    public YADEEngineException(String msg, Throwable e, IYADEProviderDelegator delegator) {
        this(msg, e, YADEReturnCode.DEFAULT_ERROR, delegator);
    }

    public YADEEngineException(String msg, Throwable e, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(msg, e);
        this.returnCode = getReturnCode(e, returnCode, delegator);
        this.delegator = delegator;
    }

    public YADEEngineException(Throwable e) {
        this(e, YADEReturnCode.DEFAULT_ERROR);
    }

    public YADEEngineException(Throwable e, YADEReturnCode returnCode) {
        this(e, returnCode, (IYADEProviderDelegator) null);
    }

    public YADEEngineException(Throwable e, IYADEProviderDelegator delegator) {
        this(e, YADEReturnCode.DEFAULT_ERROR, delegator);
    }

    public YADEEngineException(Throwable e, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        super(e);
        this.returnCode = getReturnCode(e, returnCode, delegator);
        this.delegator = delegator;
    }

    public YADEReturnCode getReturnCode() {
        if (returnCode == null) {
            returnCode = YADEReturnCode.DEFAULT_ERROR;
        }
        return returnCode;
    }

    public void setReturnCode(YADEReturnCode val) {
        returnCode = val;
    }

    public IYADEProviderDelegator getDelegator() {
        return delegator;
    }

    public static YADEReturnCode getSourceConnectionErrorReturnCode(ProviderConnectException e) {
        return e.isAuthenticationException() ? YADEReturnCode.SOURCE_AUTHENTICATION_ERROR : YADEReturnCode.SOURCE_CONNECTION_ERROR;
    }

    public static YADEReturnCode getTargetConnectionErrorReturnCode(ProviderConnectException e) {
        return e.isAuthenticationException() ? YADEReturnCode.TARGET_AUTHENTICATION_ERROR : YADEReturnCode.TARGET_CONNECTION_ERROR;
    }

    public static YADEReturnCode getJumpHostConnectionErrorReturnCode(ProviderConnectException e) {
        return e.isAuthenticationException() ? YADEReturnCode.JUMP_AUTHENTICATION_ERROR : YADEReturnCode.JUMP_CONNECTION_ERROR;
    }

    private static YADEReturnCode getReturnCode(Throwable e, YADEReturnCode returnCode, IYADEProviderDelegator delegator) {
        if (!YADEReturnCode.DEFAULT_ERROR.equals(returnCode)) {
            return returnCode;
        }
        if (e instanceof YADEEngineException) {
            returnCode = ((YADEEngineException) e).getReturnCode();
        } else if (delegator != null && e instanceof ProviderException) {
            returnCode = getConnectionErrorReturnCode(delegator, (ProviderException) e);
        }
        return returnCode == null ? YADEReturnCode.DEFAULT_ERROR : returnCode;
    }

    public static YADEReturnCode getConnectionErrorReturnCode(IYADEProviderDelegator delegator, ProviderException e) {
        if (delegator == null || !(e instanceof ProviderConnectException)) {
            return null;
        }

        ProviderConnectException ce = (ProviderConnectException) e;
        if (delegator.isJumpHost()) {
            return getJumpHostConnectionErrorReturnCode(ce);
        } else {
            if (delegator.isSource()) {
                return getSourceConnectionErrorReturnCode(ce);
            } else {
                return getTargetConnectionErrorReturnCode(ce);
            }
        }
    }

}
