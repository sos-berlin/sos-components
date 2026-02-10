package com.sos.yade.engine.commons.simulators;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.local.LocalProvider;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;

/**
 * <p>
 * Injects a connectivity fault into the provider to deliberately trigger connection-related errors (e.g. unreachable endpoint, authentication failure).
 * </p>
 * <p>
 * This class is intended for start/end simulation and test scenarios only and is used to force failures at the same execution points where real connectivity or
 * network-related errors would normally occur.
 * </p>
 */
public class YADEProviderConnectivityFaultSimulator {

    private static final String CLASS_NAME = YADEProviderConnectivityFaultSimulator.class.getSimpleName();

    private ExecutorService executor;
    private final AtomicInteger activeSimulations = new AtomicInteger(0);
    private static final String DELIMITER_LINE = "---------------------------------------------------------------------";

    private synchronized ExecutorService getExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
        }
        return executor;
    }

    public void simulate(ISOSLogger logger, AYADEProviderDelegator delegator) {
        if (delegator == null || delegator.getArgs().getSimConnFaults().isEmpty() || delegator.getProvider() == null) {
            return;
        }

        if (delegator.getProvider() instanceof LocalProvider) {
            logger.info("[%s][LocalProvider][%s][%s][skip]connectivity fault simulation not supported", delegator.getLabel(), CLASS_NAME, delegator
                    .getArgs().getSimConnFaults().getValue());
            return;
        }

        String[] times = parseTimes(delegator.getArgs().getSimConnFaults().getValue());
        int timesLength = times.length;
        if (timesLength == 0) {
            return;
        }

        activeSimulations.incrementAndGet();
        logger.info("[%s][%s]%s=%s", delegator.getLabel(), CLASS_NAME, delegator.getArgs().getSimConnFaults().getName(), delegator.getArgs()
                .getSimConnFaults().getValue());

        getExecutor().submit(() -> {

            // if (timesLength > 1) {
            // logger.info("[%s][%s][%s]start...", CLASS_NAME, delegator.getLabel(), delegator.getArgs().getSimConnFaults().getValue());
            // }

            Arrays.stream(times).forEachOrdered((t) -> {
                try {
                    long ms = toMillis(t.trim());
                    TimeUnit.MILLISECONDS.sleep(ms);
                    logger.info(DELIMITER_LINE);
                    logger.info("[%s][%s][%ss elapsed]inject connectivity fault now ...", delegator.getLabel(), CLASS_NAME, t);
                    delegator.getProvider().injectConnectivityFault();
                    logger.info(DELIMITER_LINE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("[%s][%s]provider simulation interrupted", delegator.getLabel(), CLASS_NAME);
                } catch (Exception e) {
                    logger.error("[%s][%s][%s]%s", delegator.getLabel(), CLASS_NAME, t, e);
                }
            });

            // if (timesLength > 1) {
            // logger.info("[%s][%s][%s]end", CLASS_NAME, delegator.getLabel(), delegator.getArgs().getSimConnFaults().getValue());
            // }

            if (activeSimulations.decrementAndGet() == 0) {
                shutdown(logger);
            }
        });
    }

    public synchronized void shutdown(ISOSLogger logger) {
        if (executor == null) {
            return;
        }

        logger.info(DELIMITER_LINE);
        try {
            executor.shutdown();
        } catch (Exception e) {
            logger.info("[%s][shutdown]%s", CLASS_NAME, e);
        } finally {
            executor = null;
            logger.info("[%s][shutdown]all simulations finished", CLASS_NAME);
            logger.info(DELIMITER_LINE);
        }
    }

    private static String[] parseTimes(String timesArg) {
        return timesArg.trim().split("\\s*;\\s*");
    }

    private static long toMillis(String seconds) {
        String normalized = seconds.trim().replace(',', '.');
        double sec;
        try {
            sec = Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid seconds value: " + seconds, e);
        }
        return Math.round(sec * 1_000);
    }
}
