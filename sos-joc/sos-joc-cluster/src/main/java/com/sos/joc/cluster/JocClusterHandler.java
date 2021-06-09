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
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer.JocServiceAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public class JocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterHandler.class);

    public static enum PerformType {
        START, STOP
    };

    private final JocCluster cluster;
    private List<IJocClusterService> services;
    private boolean active;

    protected JocClusterHandler(JocCluster jocCluster) {
        cluster = jocCluster;
    }

    protected JocClusterAnswer perform(StartupMode mode, PerformType type, ConfigurationGlobals configurations) {
        LOGGER.info(String.format("[%s][perform][active=%s]%s", mode, active, type.name()));

        if (cluster.getConfig().getServices() == null || cluster.getConfig().getServices().size() == 0) {
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

        cluster.readCurrentDbInfos();

        final List<ControllerConfiguration> controllers = cluster.getControllers();
        if (isStart) {
            if (controllers == null || controllers.size() == 0) {
                return JocCluster.getErrorAnswer(new Exception("missing controllers"));
            }
        }

        tryCreateServices();
        if (services.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
        for (int i = 0; i < services.size(); i++) {
            IJocClusterService s = services.get(i);
            Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                @Override
                public JocClusterAnswer get() {
                    AJocClusterService.setLogger();
                    LOGGER.info(String.format("[%s][%s][%s]start...", mode, method, s.getIdentifier()));
                    AJocClusterService.clearLogger();
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
                            answer = s.start(newControllers, configuration, mode);
                        } else {
                            answer = s.start(controllers, configuration, mode);
                        }
                    } else {
                        answer = s.stop(mode);
                    }
                    AJocClusterService.setLogger();
                    LOGGER.info(String.format("[%s][%s][%s]completed", mode, method, s.getIdentifier()));
                    AJocClusterService.clearLogger();
                    return answer;
                }
            };
            tasks.add(task);
        }
        return performServices(mode, tasks, type);
    }

    private JocClusterAnswer performServices(StartupMode mode, List<Supplier<JocClusterAnswer>> tasks, PerformType type) {
        if (tasks == null || tasks.size() == 0) {
            return JocCluster.getErrorAnswer(JocClusterAnswerState.MISSING_HANDLERS);
        }

        if (type.equals(PerformType.START)) {// TODO set active after CompletableFuture - check answer duration
            active = true;
        } else {
            active = false;
            // ThreadHelper.print(mode, "before stop active services");
        }

        AJocClusterService.setLogger();
        LOGGER.info(String.format("[%s][%s][active=%s]start...", mode, type.name(), active));

        ExecutorService es = Executors.newFixedThreadPool(services.size(), new JocClusterThreadFactory(cluster.getConfig().getThreadGroup(),
                "cluster-" + type.name().toLowerCase()));
        List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(Collectors
                .toList());
        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
        JocCluster.shutdownThreadPool(mode, es, 3);

        // for (CompletableFuture<ClusterAnswer> future : futuresList) {
        // try {
        // LOGGER.info(SOSString.toString(future.get()));
        // } catch (Exception e) {
        // LOGGER.error(e.toString(), e);
        // }
        // }
        // handlers = new ArrayList<>();

        LOGGER.info(String.format("[%s][%s][active=%s][completed]%s", mode, type.name(), active, cluster.getJocConfig().getMemberId()));
        if (active) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);// TODO check future results
        } else {
            ThreadHelper.tryStopChilds(mode, cluster.getConfig().getThreadGroup());

            ThreadHelper.print(mode, "after stop active services");
            services = null;
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);// TODO check future results
        }
    }

    private void tryCreateServices() {
        if (services == null) {
            services = new ArrayList<IJocClusterService>();
            for (int i = 0; i < cluster.getConfig().getServices().size(); i++) {
                Class<?> clazz = cluster.getConfig().getServices().get(i);
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(JocConfiguration.class, ThreadGroup.class);
                    ctor.setAccessible(true);
                    IJocClusterService s = (IJocClusterService) ctor.newInstance(cluster.getJocConfig(), cluster.getConfig().getThreadGroup());
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

    public void updateService(String identifier, String controllerId, Action action) {
        Optional<IJocClusterService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            AJocClusterService.setLogger();
            LOGGER.error((String.format("handler not found for %s", identifier)));
            AJocClusterService.clearLogger();
            return;
        }
        IJocClusterService s = os.get();
        s.update(cluster.getControllers(), controllerId, action);
    }

    public JocClusterAnswer restartService(String identifier, StartupMode mode, AConfigurationSection configuration) {
        Optional<IJocClusterService> os = services.stream().filter(h -> h.getIdentifier().equals(identifier)).findAny();
        if (!os.isPresent()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("handler not found for %s", identifier)));
        }

        AJocClusterService.setLogger();
        LOGGER.info(String.format("[%s][restart][%s]start...", mode, identifier));
        AJocClusterService.clearLogger();

        IJocClusterService s = os.get();
        JocServiceAnswer answer = s.getInfo();
        if (!answer.getState().equals(JocServiceAnswerState.RELAX)) {
            AJocClusterService.setLogger();
            LOGGER.info(String.format("[%s][restart][%s][service status %s][last activity start=%s, end=%s]wait 60s and ask again...", mode,
                    identifier, answer.getState(), answer.getLastActivityStart(), answer.getLastActivityEnd()));
            cluster.waitFor(60);
            answer = s.getInfo();
            if (answer.getState().equals(JocServiceAnswerState.RELAX)) {
                LOGGER.info(String.format("[%s][restart][%s]service status %s", mode, identifier, answer.getState()));
            } else {
                String msg = String.format("[%s][restart][%s][service status %s][last activity start=%s, end=%s]force restart", mode, identifier,
                        answer.getState(), answer.getLastActivityStart(), answer.getLastActivityEnd());
                LOGGER.info(msg);

                // String msg = String.format("[%s][restart][%s][skip][service status %s][last activity start=%s, end=%s]", mode, identifier, answer
                // .getState(), answer.getLastActivityStart(), answer.getLastActivityEnd());
                // LOGGER.error(msg);
                // AJocClusterService.clearLogger();
                // return JocCluster.getErrorAnswer(msg);
            }
        }

        AJocClusterService.setLogger(identifier);
        ThreadHelper.print(mode, "[" + identifier + "]before stop");
        AJocClusterService.clearLogger();

        s.stop(mode);

        AJocClusterService.setLogger(identifier);
        ThreadHelper.tryStop(mode, s.getThreadGroup());
        ThreadHelper.print(mode, "[" + identifier + "]after stop");
        AJocClusterService.clearLogger();

        try {
            s.start(cluster.getControllers(), configuration, mode);
        } catch (Exception e) {
            AJocClusterService.setLogger();
            LOGGER.error(String.format("[%s][restart][%s]%s", mode, identifier, e.toString()), e);
            AJocClusterService.clearLogger();

            return JocCluster.getErrorAnswer(e);
        }

        AJocClusterService.setLogger();
        LOGGER.info(String.format("[%s][restart][%s]completed", mode, identifier));
        AJocClusterService.clearLogger();

        return JocCluster.getOKAnswer(JocClusterAnswerState.RESTARTED);
    }

    public boolean isActive() {
        return active;
    }

    public List<IJocClusterService> getServices() {
        return services;
    }

}
