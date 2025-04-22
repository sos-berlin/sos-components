package com.sos.yade.engine.commons.helpers;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineJumpHostConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineSourceConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineTargetConnectionException;

public class YADEProviderDelegatorHelper {

    public static void ensureConnected(ISOSLogger logger, AYADEProviderDelegator delegator) throws YADEEngineConnectionException {
        if (delegator == null) {
            return;
        }

        YADESourceTargetArguments args = delegator.getArgs();
        // without retry
        if (!args.isRetryOnConnectionErrorEnabled()) {
            try {
                delegator.getProvider().ensureConnected();
            } catch (Throwable e) {
                throwConnectionException(delegator, e);
            }
            return;
        }

        // with retry
        int maxRetries = args.getConnectionErrorRetryCountMax().getValue().intValue();
        long retryInterval = ASOSArguments.asSeconds(args.getConnectionErrorRetryInterval(), 0L);
        for (int retryCounter = 0; retryCounter <= maxRetries; retryCounter++) {
            try {
                delegator.getProvider().ensureConnected();
                return;
            } catch (Throwable e) {
                if (retryCounter == maxRetries) {
                    throwConnectionException(delegator, e);
                }
                logger.info("[%s][retry=%s in %ss]%s", delegator.getLabel(), retryCounter + 1, retryInterval, e.toString(), e);
                YADEClientHelper.waitFor(retryInterval);
            }
        }
    }

    /** Provider Disconnect does not throw exceptions - but logs with the type (source/destination) when occurred/disconnect executed
     * 
     * @param source
     * @param target */
    public static void disconnect(IYADEProviderDelegator... delegators) {
        for (IYADEProviderDelegator d : delegators) {
            if (d != null && d.getProvider() != null) {
                d.getProvider().disconnect();
            }
        }
    }

    private static void throwConnectionException(AYADEProviderDelegator delegator, Throwable e) throws YADEEngineConnectionException {
        YADEEngineConnectionException ex = getConnectionException(delegator, e);
        if (ex != null) {
            throw ex;
        }
    }

    public static YADEEngineConnectionException getConnectionException(AYADEProviderDelegator delegator, Throwable ex) {
        if (delegator == null) {
            return null;
        }
        if (delegator.isJumpHost()) {
            return new YADEEngineJumpHostConnectionException(ex.getMessage(), ex.getCause());
        }
        if (delegator instanceof YADESourceProviderDelegator) {
            return new YADEEngineSourceConnectionException(ex.getCause());
        }
        return new YADEEngineTargetConnectionException(ex.getMessage(), ex.getCause());
    }

    public static boolean isConnectionException(Throwable cause) {
        if (cause == null) {
            return false;
        }
        Throwable e = cause;
        while (e != null) {
            if (e instanceof YADEEngineConnectionException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    public static boolean isSourceOrTargetNotConnected(IYADEProviderDelegator sourceDelegator, IYADEProviderDelegator targetDelegator) {
        return !sourceDelegator.getProvider().isConnected() || (targetDelegator != null && !targetDelegator.getProvider().isConnected());
    }

}
