package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.handler.IJocClusterHandler;

public class JocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterHandler.class);

    public static enum PerformType {
        START, STOP
    };

    private final List<IJocClusterHandler> handlers;
    private boolean active;

    public JocClusterHandler(List<IJocClusterHandler> clusterHandlers) {
        handlers = clusterHandlers;
    }

    public JocClusterAnswer perform(PerformType type) {
        if (handlers == null || handlers.size() == 0) {
            return JocCluster.getOKAnswer();
        }
        String method = type.name().toLowerCase();
        boolean isStart = type.equals(PerformType.START);

        if (isStart) {
            if (active) {
                return JocCluster.getOKAnswer();
            }
            LOGGER.info("[activate]start handlers ...");
        } else {
            if (!active) {
                return JocCluster.getOKAnswer();
            }
            LOGGER.info("[deactivate]stop handlers ...");
        }

        int size = handlers.size();
        if (size > 0) {
            List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
            for (int i = 0; i < size; i++) {
                IJocClusterHandler h = handlers.get(i);
                Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                    @Override
                    public JocClusterAnswer get() {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][%s][start]...", method, h.getIdentifier()));
                        }
                        JocClusterAnswer answer = null;
                        if (isStart) {
                            answer = h.start();
                        } else {
                            answer = h.stop();
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][%s][end]", method, h.getIdentifier()));
                        }
                        return answer;
                    }
                };
                tasks.add(task);
            }
            return executeTasks(tasks, isStart);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]already closed", method));
            }
        }
        return JocCluster.getOKAnswer();
    }

    private JocClusterAnswer executeTasks(List<Supplier<JocClusterAnswer>> tasks, boolean isStart) {
        ExecutorService es = Executors.newFixedThreadPool(handlers.size());

        List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        JocCluster.shutdownThreadPool("executeTasks", es, 3); // es.shutdown();

        // for (CompletableFuture<ClusterAnswer> future : futuresList) {
        // try {
        // LOGGER.info(SOSString.toString(future.get()));
        // } catch (Exception e) {
        // LOGGER.error(e.toString(), e);
        // }
        // }
        // handlers = new ArrayList<>();

        if (isStart) {
            active = true;
            LOGGER.info("[activate]completed");
        } else {
            active = false;
            LOGGER.info("[deactivate]completed");
        }
        return JocCluster.getOKAnswer();// TODO check future results
    }

    public boolean isActive() {
        return active;
    }

}
