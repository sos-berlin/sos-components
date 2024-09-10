package com.sos.joc.cluster;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.common.JocClusterServiceActivity;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class JocClusterActiveMemberHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterActiveMemberHandler.class);

    public static enum PerformType {
        START, STOP
    };

    private final JocCluster cluster;
    private List<IJocActiveMemberService> services;
    private boolean active;

    protected JocClusterActiveMemberHandler(JocCluster jocCluster) {
        cluster = jocCluster;
    }

    protected synchronized JocClusterAnswer perform(StartupMode mode, PerformType type, ConfigurationGlobals configurations) {
        LOGGER.info(String.format("[%s][perform][active=%s]%s", mode, active, type.name()));

        if (cluster.getConfig().getActiveMemberServices() == null || cluster.getConfig().getActiveMemberServices().size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterState.MISSING_CONFIGURATION);
        }

        String method = type.name().toLowerCase();
        boolean isStart = type.equals(PerformType.START);
        if (isStart) {
            if (!StartupMode.automatic.equals(mode)) {
                cluster.getConfig().rereadClusterMode();
            }
            if (active) {
                return JocCluster.getOKAnswer(JocClusterState.ALREADY_STARTED);
            }
            if (StartupMode.manual_switchover.equals(mode) || StartupMode.automatic_switchover.equals(mode)) {
                if (!cluster.getConfig().getClusterModeResult().getUse()) {
                    return JocCluster.getErrorAnswer(JocClusterState.MISSING_LICENSE);
                }
            }
        } else {
            if (!active || (services == null || services.size() == 0)) {
                return JocCluster.getOKAnswer(JocClusterState.ALREADY_STOPPED);
            }
        }

        cluster.readCurrentDbInfos();

        final List<ControllerConfiguration> controllers = cluster.getControllers();
        if (isStart) {
            if (controllers == null || controllers.size() == 0) {
                return JocCluster.getErrorAnswer(new Exception("missing controllers"));
            }
        }

        tryCreateServices();
        if (services.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterState.MISSING_HANDLERS);
        }

        ScheduledExecutorService heartBeat = scheduleHeartBeat(mode, method);
        List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
        for (int i = 0; i < services.size(); i++) {
            IJocActiveMemberService s = services.get(i);
            Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                @Override
                public JocClusterAnswer get() {
                    JocClusterServiceLogger.setLogger();
                    LOGGER.info(String.format("[%s][%s][%s]start...", mode, method, s.getIdentifier()));
                    JocClusterServiceLogger.removeLogger();
                    JocClusterAnswer answer = null;
                    if (isStart) {
                        AConfigurationSection configuration = null;
                        try {
                            configuration = configurations.getConfigurationSection(DefaultSections.valueOf(s.getIdentifier()));
                        } catch (Throwable e) {
                        }
                        if (!SOSString.isEmpty(s.getControllerApiUser())) {
                            List<ControllerConfiguration> newControllers = new ArrayList<ControllerConfiguration>();
                            for (ControllerConfiguration m : controllers) {
                                newControllers.add(m.copy(s.getControllerApiUser(), s.getControllerApiUserPassword()));
                            }
                            answer = s.start(mode, newControllers, configuration);
                        } else {
                            answer = s.start(mode, controllers, configuration);
                        }
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
        return performServices(mode, tasks, type, heartBeat);
    }

    private ScheduledExecutorService scheduleHeartBeat(StartupMode mode, String method) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1, new JocClusterThreadFactory(cluster.getConfig().getThreadGroup(),
                JocClusterConfiguration.IDENTIFIER + "-s"));
        threadPool.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                JocClusterServiceLogger.setLogger(JocClusterConfiguration.IDENTIFIER);
                cluster.updateHeartBeat(mode, method, 3, true);
            }
        }, 5 /* start delay */, cluster.getConfig().getPollingInterval() /* duration */, TimeUnit.SECONDS);
        return threadPool;
    }

    private JocClusterAnswer performServices(StartupMode mode, List<Supplier<JocClusterAnswer>> tasks, PerformType type,
            ScheduledExecutorService heartBeat) {
        String logPrefix = "[" + mode + "]";
        if (tasks == null || tasks.size() == 0) {
            JocCluster.shutdownThreadPool(logPrefix, heartBeat, 1);
            return JocCluster.getErrorAnswer(JocClusterState.MISSING_HANDLERS);
        }

        if (type.equals(PerformType.START)) {// TODO set active after CompletableFuture - check answer duration
            active = true;
        } else {
            active = false;
            // ThreadHelper.print(mode, "before stop active services");
        }

        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][%s][active=%s]start...", mode, type.name(), active));

        ExecutorService es = Executors.newFixedThreadPool(services.size(), new JocClusterThreadFactory(cluster.getConfig().getThreadGroup(),
                "cluster-" + type.name().toLowerCase()));
        List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        JocCluster.shutdownThreadPool(logPrefix, es, 3);
        JocCluster.shutdownThreadPool(logPrefix, heartBeat, 1);

        // for (CompletableFuture<ClusterAnswer> future : futuresList) {
        // try {
        // LOGGER.info(SOSString.toString(future.get()));
        // } catch (Exception e) {
        // LOGGER.error(e.toString(), e);
        // }
        // }
        // handlers = new ArrayList<>();

        cluster.updateHeartBeat(mode, type.name().toLowerCase(), 2, false);

        LOGGER.info(String.format("[%s][%s][active=%s][completed]%s", mode, type.name(), active, cluster.getJocConfig().getMemberId()));
        if (active) {
            return JocCluster.getOKAnswer(JocClusterState.STARTED);// TODO check future results
        } else {
            ThreadHelper.tryStopChilds(mode, cluster.getConfig().getThreadGroup(), Collections.singleton(
                    JocClusterEmbeddedServicesHandler.THREAD_GROUP_NAME));

            ThreadHelper.print(mode, "after stop active services");
            services = null;
            return JocCluster.getOKAnswer(JocClusterState.STOPPED);// TODO check future results
        }
    }

    private void tryCreateServices() {
        if (services == null) {
            services = new ArrayList<IJocActiveMemberService>();
            for (int i = 0; i < cluster.getConfig().getActiveMemberServices().size(); i++) {
                Class<?> clazz = cluster.getConfig().getActiveMemberServices().get(i);
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(JocConfiguration.class, ThreadGroup.class);
                    ctor.setAccessible(true);
                    IJocActiveMemberService s = (IJocActiveMemberService) ctor.newInstance(cluster.getJocConfig(), cluster.getConfig()
                            .getThreadGroup());
                    services.add(s);
                } catch (Throwable e) {
                    LOGGER.error(String.format("[can't create new instance][%s]%s", clazz.getName(), e.toString()), e);
                }
            }
        }
    }

    public void updateControllerInfos() {
        cluster.readCurrentDbInfos();
    }

    public void updateService(StartupMode mode, String identifier, String controllerId, Action action) {
        Optional<IJocActiveMemberService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            JocClusterServiceLogger.setLogger();
            LOGGER.error((String.format("handler not found for %s", identifier)));
            JocClusterServiceLogger.removeLogger();
            return;
        }
        IJocActiveMemberService s = os.get();
        s.update(mode, cluster.getControllers(), controllerId, action);
    }

    public void updateService(StartupMode mode, String identifier, AConfigurationSection configuration) {
        Optional<IJocActiveMemberService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            JocClusterServiceLogger.setLogger();
            LOGGER.error((String.format("handler not found for %s", identifier)));
            JocClusterServiceLogger.removeLogger();
            return;
        }
        IJocActiveMemberService s = os.get();
        s.update(mode, configuration);
    }

    public JocClusterAnswer runServiceNow(StartupMode mode, String identifier, AConfigurationSection configuration) {
        Optional<IJocActiveMemberService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("[runServiceNow]handler not found for %s", identifier)));
        }
        JocClusterAnswer answer = new JocClusterAnswer(JocClusterState.RUNNING);

        IJocActiveMemberService s = os.get();
        JocClusterServiceActivity activity = s.getActivity();
        if (activity.isBusy()) {
            answer.setState(JocClusterState.ALREADY_RUNNING);
        } else {
            JocClusterServiceLogger.setLogger();
            LOGGER.info(String.format("[%s][runServiceNow][%s]start...", mode, identifier));
            JocClusterServiceLogger.removeLogger();

            s.runNow(mode, configuration);
        }

        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][runServiceNow][%s]%s", mode, identifier, SOSString.toString(answer)));
        JocClusterServiceLogger.removeLogger();
        return answer;
    }

    public JocClusterAnswer restartService(StartupMode mode, String identifier, AConfigurationSection configuration) {
        Optional<IJocActiveMemberService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("handler not found for %s", identifier)));
        }

        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][restart][%s]start...", mode, identifier));
        JocClusterServiceLogger.removeLogger();

        IJocActiveMemberService s = os.get();
        JocClusterServiceActivity activity = s.getActivity();
        if (activity.isBusy()) {
            if (activity.getDiff() < 0) {
                int waitFor = 10;
                JocClusterServiceLogger.setLogger();
                LOGGER.info(String.format("[%s][restart][%s][service status %s][last activity start=%s, end=%s]wait %s s and ask again...", mode,
                        identifier, activity.getState(), activity.getLastStart(), activity.getLastEnd(), waitFor));
                cluster.waitFor(waitFor);
                activity = s.getActivity();
                if (activity.isBusy()) {
                    String msg = String.format("[%s][restart][%s][service status %s][last activity start=%s, end=%s]force restart", mode, identifier,
                            activity.getState(), activity.getLastStart(), activity.getLastEnd());
                    LOGGER.info(msg);
                } else {
                    LOGGER.info(String.format("[%s][restart][%s]service status %s", mode, identifier, activity.getState()));
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
            s.start(mode, cluster.getControllers(), configuration);
        } catch (Exception e) {
            JocClusterServiceLogger.setLogger();
            LOGGER.error(String.format("[%s][restart][%s]%s", mode, identifier, e.toString()), e);
            JocClusterServiceLogger.removeLogger();

            return JocCluster.getErrorAnswer(e);
        }

        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[%s][restart][%s]completed", mode, identifier));
        JocClusterServiceLogger.removeLogger();

        return JocCluster.getOKAnswer(JocClusterState.RESTARTED);
    }

    public boolean isActive() {
        return active;
    }

    public List<IJocActiveMemberService> getServices() {
        return services;
    }

}
