package com.sos.yade.engine.commons.helpers;

import java.util.concurrent.Callable;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEArguments.RetryOnConnectionError;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADEProviderDelegatorFactory;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.exceptions.YADEEngineJumpHostConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineSourceConnectionException;
import com.sos.yade.engine.exceptions.YADEEngineTargetConnectionException;

public class YADEProviderDelegatorHelper {

    public static AYADEProviderDelegator ensureConnectedOnStart(ISOSLogger logger, YADEArguments args, AYADEProviderDelegator delegator)
            throws YADEEngineConnectionException {

        if (delegator == null) { // target can be null
            return null;
        }

        boolean hasAlternatives = delegator.getProvider().getArguments().hasAlternatives();
        try {
            delegator.getProvider().setLogConnectFailedMsgDisabled(hasAlternatives);
            ensureConnected(logger, delegator, null, args.getRetryOnConnectionError());
            delegator.getProvider().setLogConnectFailedMsgDisabled(false);
        } catch (Exception e) {
            try {
                if (!hasAlternatives) {
                    throw e;
                }

                if (hasAlternatives) {
                    logger.info(e);
                    delegator.getProvider().disconnect();

                    int i = 0;
                    int max = delegator.getProvider().getArguments().getAlternatives().size();

                    for (AProviderArguments a : delegator.getProvider().getArguments().getAlternatives()) {
                        i++;
                        AYADEProviderDelegator newDelegator = null;
                        try {
                            newDelegator = YADEProviderDelegatorFactory.reassignDelegator(logger, args, delegator, a);
                            logger.info("[try alternative][" + i + "][" + a.getKey().getValue() + "]" + YADEClientBannerWriter.getProtocolInfo(logger,
                                    newDelegator.getLabel(), newDelegator.getArgs()));

                            newDelegator.getProvider().setLogConnectFailedMsgDisabled(true);
                            ensureConnected(logger, newDelegator, null, args.getRetryOnConnectionError());
                            newDelegator.getProvider().setLogConnectFailedMsgDisabled(false);
                            return newDelegator;
                        } catch (YADEEngineInitializationException ee) {
                            // credential store file not found etc...
                            if (i >= max) {
                                throw new YADEEngineConnectionException(ee, delegator.getConnectionErrorReturnCode(), delegator);
                            }
                            logger.info(ee);
                        } catch (Exception ee) {
                            if (newDelegator != null) {
                                newDelegator.getProvider().disconnect();
                            }

                            if (i >= max) {
                                throw ee;
                            }

                            logger.info(ee);
                        }
                    }
                }
            } catch (YADEEngineConnectionException ex) {
                if (delegator.useJumpInitialSourceTargetConnectionErrorCode()) {
                    ex.setReturnCode(YADEReturnCode.JUMP_INITIAL_SOURCE_TARGET_CONNECTION_ERROR);
                }

                if (args.getAlternativeProfile().isEmpty()) {
                    throw ex;
                }
                ex.setNeedsAlternativeProfile();
                throw ex;
            } catch (Exception ex) {
                YADEReturnCode rc = delegator.useJumpInitialSourceTargetConnectionErrorCode()
                        ? YADEReturnCode.JUMP_INITIAL_SOURCE_TARGET_CONNECTION_ERROR : delegator.getConnectionErrorReturnCode();

                YADEEngineConnectionException exx = new YADEEngineConnectionException(ex, rc, delegator);
                if (args.getAlternativeProfile().isEmpty()) {
                    throw ex;
                }

                exx.setNeedsAlternativeProfile();
                throw exx;
            }
        }

        return delegator;
    }

    public static void ensureConnected(ISOSLogger logger, AYADEProviderDelegator delegator, final RetryOnConnectionError retry)
            throws YADEEngineConnectionException {
        ensureConnected(logger, delegator, null, retry);
    }

    public static void ensureConnected(ISOSLogger logger, AYADEProviderDelegator delegator, String action, final RetryOnConnectionError retry)
            throws YADEEngineConnectionException {

        if (delegator == null) { // target can be null
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[%s][ensureConnected]retry.isEnabled=%s", delegator.getLabel(), retry.isEnabled());
        }

        // without retry
        if (!retry.isEnabled()) {
            try {
                delegator.getProvider().ensureConnected();
            } catch (ProviderConnectException e) {
                throwConnectionException(delegator, e);
            }
            return;
        }
        // with retry
        for (int retryCounter = 0; retryCounter <= retry.getMaxRetries(); retryCounter++) {
            try {
                delegator.getProvider().ensureConnected();
                return;
            } catch (ProviderConnectException e) {
                if (retryCounter == retry.getMaxRetries()) {
                    throwConnectionException(delegator, e);
                }
                String actionLog = action == null ? "" : "[" + action + "]";
                logger.info("[%s]%s[Retry=%s/%s starts in %ss]due to %s", delegator.getLabel(), actionLog, (retryCounter + 1), retry.getMaxRetries(),
                        retry.getInterval(), e.toString());
                YADEClientHelper.waitFor(retry.getInterval());
            }
        }
    }

    public static void executeOperation(ISOSLogger logger, AYADEProviderDelegator delegator, RetryOnConnectionError retry, Callable<Void> operation)
            throws Exception {
        try {
            operation.call();
        } catch (Exception e) {
            if (delegator.getProvider().isConnected()) {
                throw e;
            }
            logger.info("  " + e.getMessage());
            ensureConnected(logger, delegator, retry);
            operation.call();
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

    private static void throwConnectionException(AYADEProviderDelegator delegator, ProviderConnectException e) throws YADEEngineConnectionException {
        YADEEngineConnectionException ex = getConnectionException(delegator, e);
        if (ex != null) {
            throw ex;
        }
    }

    public static YADEEngineConnectionException getConnectionException(AYADEProviderDelegator delegator, ProviderConnectException ex) {
        if (delegator == null) {
            return null;
        }
        if (delegator.isJumpHost()) {
            return new YADEEngineJumpHostConnectionException(ex, delegator);
        }
        if (delegator instanceof YADESourceProviderDelegator) {
            return new YADEEngineSourceConnectionException(ex, delegator);
        }
        return new YADEEngineTargetConnectionException(ex, delegator);
    }

    @SuppressWarnings("unused")
    private static boolean isConnectionException(Throwable cause) {
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

    public static boolean isSourceConnected(IYADEProviderDelegator sourceDelegator) {
        return sourceDelegator.getProvider().isConnected();
    }

    public static boolean isTargetConnected(IYADEProviderDelegator targetDelegator) {
        if (targetDelegator == null) {
            return true;
        }
        return targetDelegator.getProvider().isConnected();
    }

    @SuppressWarnings("unused")
    private static boolean isSourceOrTargetNotConnected(IYADEProviderDelegator sourceDelegator, IYADEProviderDelegator targetDelegator) {
        return !sourceDelegator.getProvider().isConnected() || (targetDelegator != null && !targetDelegator.getProvider().isConnected());
    }

}
