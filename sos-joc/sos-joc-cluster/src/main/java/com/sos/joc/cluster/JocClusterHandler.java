package com.sos.joc.cluster;

import java.lang.reflect.Constructor;
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
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.handler.IJocClusterHandler;

public class JocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterHandler.class);

    public static enum PerformType {
        START, STOP
    };

    private final JocConfiguration config;
    private final List<Class<?>> handlerClasses;
    private List<IJocClusterHandler> handlers;
    private boolean active;

    public JocClusterHandler(JocConfiguration jocConfig, List<Class<?>> clusterHandlerClasses) {
        config = jocConfig;
        handlerClasses = clusterHandlerClasses;
    }

    public JocClusterAnswer perform(PerformType type) {
        LOGGER.info(String.format("[perform][active=%s]%s", active, type.name()));

        if (handlerClasses == null || handlerClasses.size() == 0) {
            return JocCluster.getOKAnswer();
        }
        String method = type.name().toLowerCase();
        boolean isStart = type.equals(PerformType.START);

        if (isStart) {
            if (active) {
                return JocCluster.getOKAnswer();
            }
        } else {
            if (!active) {
                return JocCluster.getOKAnswer();
            }
        }

        int size = handlerClasses.size();
        if (size > 0) {
            tryCreateHandlers(size);

            List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
            for (int i = 0; i < handlers.size(); i++) {
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
            return executeTasks(tasks, type);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]already closed", method));
            }
        }
        return JocCluster.getOKAnswer();
    }

    private void tryCreateHandlers(int size) {
        if (handlers == null || handlers.size() != size) {
            handlers = new ArrayList<IJocClusterHandler>();
            for (int i = 0; i < size; i++) {
                Class<?> clazz = handlerClasses.get(i);
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(JocConfiguration.class);
                    ctor.setAccessible(true);
                    IJocClusterHandler h = (IJocClusterHandler) ctor.newInstance(config);
                    handlers.add(h);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[can't create new instance][%s]%s", clazz.getName(), e.toString()));
                }
            }
        }
    }

    private JocClusterAnswer executeTasks(List<Supplier<JocClusterAnswer>> tasks, PerformType type) {
        if (tasks == null || tasks.size() == 0) {
            return JocCluster.getOKAnswer();
        }

        if (type.equals(PerformType.START)) {// TODO set active after CompletableFuture - check answer duration
            active = true;
        } else {
            active = false;
        }
        LOGGER.info(String.format("[%s][active=%s]start ...", type.name(), active));

        ExecutorService es = Executors.newFixedThreadPool(handlers.size());
        List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        JocCluster.shutdownThreadPool("handlers][" + type.name() + "][executeTasks", es, 3); // es.shutdown();

        // for (CompletableFuture<ClusterAnswer> future : futuresList) {
        // try {
        // LOGGER.info(SOSString.toString(future.get()));
        // } catch (Exception e) {
        // LOGGER.error(e.toString(), e);
        // }
        // }
        // handlers = new ArrayList<>();

        LOGGER.info(String.format("[%s][active=%s]completed", type.name(), active));
        return JocCluster.getOKAnswer();// TODO check future results
    }

    public boolean isActive() {
        return active;
    }

}
