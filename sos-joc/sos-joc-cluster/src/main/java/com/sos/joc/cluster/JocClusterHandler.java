package com.sos.joc.cluster;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.handler.IJocClusterHandler;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class JocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterHandler.class);

    public static enum PerformType {
        START, STOP
    };

    private final JocCluster cluster;
    private List<IJocClusterHandler> handlers;
    private boolean active;

    public JocClusterHandler(JocCluster jocCluster) {
        cluster = jocCluster;
    }

    public JocClusterAnswer perform(PerformType type) {
        LOGGER.info(String.format("[perform][active=%s]%s", active, type.name()));

        if (cluster.getConfig().getHandlerClasses() == null || cluster.getConfig().getHandlerClasses().size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS_CONFIGURATION);
        }

        String method = type.name().toLowerCase();
        boolean isStart = type.equals(PerformType.START);
        if (isStart) {
            if (active) {
                return JocCluster.getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
            }
        } else {
            if (!active || (handlers == null || handlers.size() == 0)) {
                return JocCluster.getOKAnswer(JocClusterAnswerState.ALREADY_STOPPED);
            }
        }

        final List<ControllerConfiguration> controllers = cluster.getControllers();
        if (isStart) {
            if (controllers == null || controllers.size() == 0) {
                return JocCluster.getErrorAnswer(new Exception("missing controllers"));
            }
        }

        tryCreateHandlers();
        if (handlers.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
        for (int i = 0; i < handlers.size(); i++) {
            IJocClusterHandler h = handlers.get(i);
            Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                @Override
                public JocClusterAnswer get() {
                    Thread.currentThread().setName(h.getIdentifier());

                    LOGGER.info(String.format("[%s][%s]start...", method, h.getIdentifier()));
                    JocClusterAnswer answer = null;
                    if (isStart) {
                        if (!SOSString.isEmpty(h.getControllerApiUser())) {
                            List<ControllerConfiguration> newControllers = new ArrayList<ControllerConfiguration>();
                            for (ControllerConfiguration m : controllers) {
                                newControllers.add(m.copy(h.getControllerApiUser(), h.getControllerApiUserPassword()));
                            }
                            answer = h.start(newControllers);
                        } else {
                            answer = h.start(controllers);
                        }
                    } else {
                        answer = h.stop();
                    }
                    LOGGER.info(String.format("[%s][%s]completed", method, h.getIdentifier()));
                    return answer;
                }
            };
            tasks.add(task);
        }
        return executePerformTasks(tasks, type);
    }

    private void tryCreateHandlers() {
        if (handlers == null) {
            handlers = new ArrayList<IJocClusterHandler>();
            for (int i = 0; i < cluster.getConfig().getHandlerClasses().size(); i++) {
                Class<?> clazz = cluster.getConfig().getHandlerClasses().get(i);
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(JocConfiguration.class);
                    ctor.setAccessible(true);
                    IJocClusterHandler h = (IJocClusterHandler) ctor.newInstance(cluster.getJocConfig());
                    handlers.add(h);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[can't create new instance][%s]%s", clazz.getName(), e.toString()), e);
                }
            }
        }
    }

    private JocClusterAnswer executePerformTasks(List<Supplier<JocClusterAnswer>> tasks, PerformType type) {
        if (tasks == null || tasks.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        if (type.equals(PerformType.START)) {// TODO set active after CompletableFuture - check answer duration
            active = true;
        } else {
            active = false;
        }
        LOGGER.info(String.format("[%s][active=%s]start ...", type.name(), active));

        ExecutorService es = Executors.newFixedThreadPool(handlers.size(), new JocClusterThreadFactory("cluster-handler-" + type.name()));
        List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        JocCluster.shutdownThreadPool(es, 3);

        // for (CompletableFuture<ClusterAnswer> future : futuresList) {
        // try {
        // LOGGER.info(SOSString.toString(future.get()));
        // } catch (Exception e) {
        // LOGGER.error(e.toString(), e);
        // }
        // }
        // handlers = new ArrayList<>();

        LOGGER.info(String.format("[%s][active=%s]completed", type.name(), active));
        if (active) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);// TODO check future results
        } else {
            ThreadHelper.stopThreads(handlers.stream().map(h -> h.getIdentifier()).collect(Collectors.toList()));
            ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "[cluster handlers]after stop");
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);// TODO check future results
        }
    }

    public JocClusterAnswer restartHandler(String identifier) {
        Optional<IJocClusterHandler> oh = handlers.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!oh.isPresent()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("handler not found for %s", identifier)));
        }
        IJocClusterHandler h = oh.get();

        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "[restart " + identifier + "]before stop");
        h.stop();
        ThreadHelper.stopThreads(identifier);
        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "[restart " + identifier + "]after stop");

        h.start(cluster.getControllers());

        return JocCluster.getOKAnswer(JocClusterAnswerState.RESTARTED);
    }

    public boolean isActive() {
        return active;
    }

}
