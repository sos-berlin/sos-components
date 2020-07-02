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
import com.sos.joc.cluster.IJocClusterService;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class JocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterHandler.class);

    public static enum PerformType {
        START, STOP
    };

    private final JocCluster cluster;
    private List<IJocClusterService> services;
    private boolean active;

    public JocClusterHandler(JocCluster jocCluster) {
        cluster = jocCluster;
    }

    public JocClusterAnswer perform(PerformType type) {
        LOGGER.info(String.format("[perform][active=%s]%s", active, type.name()));

        if (cluster.getConfig().getServices() == null || cluster.getConfig().getServices().size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS_CONFIGURATION);
        }

        String method = type.name().toLowerCase();
        boolean isStart = type.equals(PerformType.START);
        if (isStart) {
            if (active) {
                return JocCluster.getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
            }
        } else {
            if (!active || (services == null || services.size() == 0)) {
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
        if (services.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
        for (int i = 0; i < services.size(); i++) {
            IJocClusterService s = services.get(i);
            Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                @Override
                public JocClusterAnswer get() {
                    Thread.currentThread().setName(s.getIdentifier());

                    LOGGER.info(String.format("[%s][%s]start...", method, s.getIdentifier()));
                    JocClusterAnswer answer = null;
                    if (isStart) {
                        if (!SOSString.isEmpty(s.getControllerApiUser())) {
                            List<ControllerConfiguration> newControllers = new ArrayList<ControllerConfiguration>();
                            for (ControllerConfiguration m : controllers) {
                                newControllers.add(m.copy(s.getControllerApiUser(), s.getControllerApiUserPassword()));
                            }
                            answer = s.start(newControllers);
                        } else {
                            answer = s.start(controllers);
                        }
                    } else {
                        answer = s.stop();
                    }
                    LOGGER.info(String.format("[%s][%s]completed", method, s.getIdentifier()));
                    return answer;
                }
            };
            tasks.add(task);
        }
        return performServices(tasks, type);
    }

    private void tryCreateHandlers() {
        if (services == null) {
            services = new ArrayList<IJocClusterService>();
            for (int i = 0; i < cluster.getConfig().getServices().size(); i++) {
                Class<?> clazz = cluster.getConfig().getServices().get(i);
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(JocConfiguration.class);
                    ctor.setAccessible(true);
                    IJocClusterService s = (IJocClusterService) ctor.newInstance(cluster.getJocConfig());
                    services.add(s);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[can't create new instance][%s]%s", clazz.getName(), e.toString()), e);
                }
            }
        }
    }

    private JocClusterAnswer performServices(List<Supplier<JocClusterAnswer>> tasks, PerformType type) {
        if (tasks == null || tasks.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        if (type.equals(PerformType.START)) {// TODO set active after CompletableFuture - check answer duration
            active = true;
        } else {
            active = false;
        }
        LOGGER.info(String.format("[%s][active=%s]start ...", type.name(), active));

        ExecutorService es = Executors.newFixedThreadPool(services.size(), new JocClusterThreadFactory("cluster-handler-" + type.name()));
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

        LOGGER.info(String.format("[%s][active=%s][completed]%s", type.name(), active, cluster.getJocConfig().getMemberId()));
        if (active) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);// TODO check future results
        } else {
            ThreadHelper.stopThreads(services.stream().map(h -> h.getIdentifier()).collect(Collectors.toList()));
            ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "[cluster handlers]after stop");
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);// TODO check future results
        }
    }

    public JocClusterAnswer restartService(String identifier) {
        Optional<IJocClusterService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("handler not found for %s", identifier)));
        }
        IJocClusterService s = os.get();

        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "[restart " + identifier + "]before stop");
        s.stop();
        ThreadHelper.stopThreads(identifier);
        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "[restart " + identifier + "]after stop");

        s.start(cluster.getControllers());

        return JocCluster.getOKAnswer(JocClusterAnswerState.RESTARTED);
    }

    public boolean isActive() {
        return active;
    }

}
