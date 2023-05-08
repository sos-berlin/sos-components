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

import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.embedded.IJocEmbeddedService;

public class JocClusterEmbeddedServicesHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterEmbeddedServicesHandler.class);

    public static enum PerformType {
        START, STOP
    };

    public static final String THREAD_GROUP_NAME = "embedded_services";

    private final JocCluster cluster;
    private final ThreadGroup parentThreadGroup;

    private List<IJocEmbeddedService> services;
    private boolean active;

    protected JocClusterEmbeddedServicesHandler(JocCluster jocCluster) {
        cluster = jocCluster;
        parentThreadGroup = new ThreadGroup(THREAD_GROUP_NAME);
    }

    protected synchronized JocClusterAnswer perform(StartupMode mode, PerformType type) {
        LOGGER.info(String.format("[%s][perform][active=%s]%s", mode, active, type.name()));

        if (cluster.getConfig().getEmbeddedServices() == null || cluster.getConfig().getEmbeddedServices().size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_CONFIGURATION);
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

        tryCreateServices();
        if (services.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
        for (int i = 0; i < services.size(); i++) {
            IJocEmbeddedService s = services.get(i);
            Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                @Override
                public JocClusterAnswer get() {
                    JocClusterServiceLogger.setLogger();
                    LOGGER.info(String.format("[%s][%s][%s]start...", mode, method, s.getIdentifier()));
                    JocClusterServiceLogger.removeLogger();
                    JocClusterAnswer answer = null;
                    if (isStart) {
                        answer = s.start(mode);
                    } else {
                        answer = s.stop(mode);
                    }
                    JocClusterServiceLogger.setLogger();
                    LOGGER.info(String.format("[%s][%s][%s]completed", mode, method, s.getIdentifier()));
                    JocClusterServiceLogger.removeLogger();
                    return answer;
                }
            };
            tasks.add(task);
        }
        return performServices(mode, tasks, type);
    }

    private JocClusterAnswer performServices(StartupMode mode, List<Supplier<JocClusterAnswer>> tasks, PerformType type) {
        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][%s][active=%s]start...", mode, type.name(), active));

        if (type.equals(PerformType.START)) {// TODO set active after CompletableFuture - check answer duration
            active = true;
        } else {
            active = false;
            // ThreadHelper.print(mode, "before stop active services");
        }

        ExecutorService es = Executors.newFixedThreadPool(services.size(), new JocClusterThreadFactory(parentThreadGroup, "cluster-es-" + type.name()
                .toLowerCase()));
        List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        JocCluster.shutdownThreadPool(mode, es, 3);

        LOGGER.info(String.format("[%s][%s][active=%s][completed]%s", mode, type.name(), active, cluster.getJocConfig().getMemberId()));
        if (active) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);// TODO check future results
        } else {
            services = null;
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);// TODO check future results
        }
    }

    private void tryCreateServices() {
        if (services == null) {
            services = new ArrayList<>();

            for (int i = 0; i < cluster.getConfig().getEmbeddedServices().size(); i++) {
                Class<?> clazz = cluster.getConfig().getEmbeddedServices().get(i);
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(JocConfiguration.class, ThreadGroup.class);
                    ctor.setAccessible(true);
                    IJocEmbeddedService s = (IJocEmbeddedService) ctor.newInstance(cluster.getJocConfig(), parentThreadGroup);
                    services.add(s);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[can't create new instance][%s]%s", clazz.getName(), e.toString()), e);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void updateService(String identifier, StartupMode mode, AConfigurationSection configuration) {
        Optional<IJocEmbeddedService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            JocClusterServiceLogger.setLogger();
            LOGGER.error((String.format("handler not found for %s", identifier)));
            JocClusterServiceLogger.removeLogger();
            return;
        }
        IJocEmbeddedService s = os.get();
        s.update(mode, configuration);
    }

    @SuppressWarnings("unused")
    private JocClusterAnswer restartService(String identifier, StartupMode mode) {
        Optional<IJocEmbeddedService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("handler not found for %s", identifier)));
        }

        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][restart][%s]start...", mode, identifier));
        JocClusterServiceLogger.removeLogger();

        IJocEmbeddedService s = os.get();
        JocServiceAnswer answer = s.getInfo();
        if (!answer.getState().equals(JocServiceAnswerState.RELAX)) {
            if (answer.getDiff() < 0) {
                JocClusterServiceLogger.setLogger();
                LOGGER.info(String.format("[%s][restart][%s][service status %s][last activity start=%s, end=%s]wait 30s and ask again...", mode,
                        identifier, answer.getState(), answer.getLastActivityStart(), answer.getLastActivityEnd()));
                cluster.waitFor(10);
                answer = s.getInfo();
                if (answer.getState().equals(JocServiceAnswerState.RELAX)) {
                    LOGGER.info(String.format("[%s][restart][%s]service status %s", mode, identifier, answer.getState()));
                } else {
                    String msg = String.format("[%s][restart][%s][service status %s][last activity start=%s, end=%s]force restart", mode, identifier,
                            answer.getState(), answer.getLastActivityStart(), answer.getLastActivityEnd());
                    LOGGER.info(msg);
                }
            }
        }

        JocClusterServiceLogger.setLogger(identifier);
        ThreadHelper.print(mode, "[" + identifier + "]before stop");
        JocClusterServiceLogger.removeLogger();

        s.stop(mode);

        JocClusterServiceLogger.setLogger(identifier);
        ThreadHelper.tryStop(mode, s.getThreadGroup());
        ThreadHelper.print(mode, "[" + identifier + "]after stop");
        JocClusterServiceLogger.removeLogger();

        try {
            s.start(mode);
        } catch (Exception e) {
            JocClusterServiceLogger.setLogger();
            LOGGER.error(String.format("[%s][restart][%s]%s", mode, identifier, e.toString()), e);
            JocClusterServiceLogger.removeLogger();

            return JocCluster.getErrorAnswer(e);
        }

        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][restart][%s]completed", mode, identifier));
        JocClusterServiceLogger.removeLogger();

        return JocCluster.getOKAnswer(JocClusterAnswerState.RESTARTED);
    }

    public boolean isActive() {
        return active;
    }

    public List<IJocEmbeddedService> getServices() {
        return services;
    }

}
