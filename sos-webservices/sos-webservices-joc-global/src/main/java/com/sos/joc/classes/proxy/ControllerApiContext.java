package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.ControllerConnectionRefusedException;

import js7.data_for_java.auth.JAdmission;
import js7.data_for_java.auth.JHttpsConfig;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JProxyContext;
import scala.collection.JavaConverters;

public class ControllerApiContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerApiContext.class);

    private ControllerApiContext() {
    }

    protected static JControllerApi newControllerApi(JProxyContext proxyContext, ProxyCredentials credentials)
            throws ControllerConnectionRefusedException {
        LOGGER.info(String.format("connect ControllerApi of %s", toString(credentials)));
        checkCredentials(credentials);
        List<JAdmission> admissions = null;
        if (credentials.getBackupUrl() != null) {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()), JAdmission.of(credentials.getBackupUrl(),
                    credentials.getAccount()));
        } else {
            admissions = Collections.singletonList(JAdmission.of(credentials.getUrl(), credentials.getAccount()));
        }
        return proxyContext.newControllerApi(admissions, credentials.getHttpsConfig());
    }

    private static String toString(ProxyCredentials credentials) {
        if (credentials.getBackupUrl() != null) {
            return String.format("'%s' cluster (%s, %s)", credentials.getControllerId(), credentials.getUrl(), credentials.getBackupUrl());
        } else {
            return String.format("'%s' (%s)", credentials.getControllerId(), credentials.getUrl());
        }
    }

    private static void checkCredentials(ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        if (credentials.getUrl() == null) {
            throw new ControllerConnectionRefusedException("URL is undefined");
        } else if (credentials.getUrl().startsWith("https://") || (credentials.getBackupUrl() != null && credentials.getBackupUrl().startsWith(
                "https://"))) {
            JHttpsConfig httpsConfig = credentials.getHttpsConfig();
            if (httpsConfig.asScala().trustStoreRefs() == null || JavaConverters.asJava(httpsConfig.asScala().trustStoreRefs()).isEmpty()) {
                throw new ControllerConnectionRefusedException("Couldn't find required truststore");
            } else if (credentials.getAccount().toScala().isEmpty() && !httpsConfig.asScala().keyStoreRef().nonEmpty()) {
                throw new ControllerConnectionRefusedException("Neither account is specified nor client certificate couldn't find");
            }
        }
    }
    
//    private static Map<String, ClusterState> getClusterStatus(ProxyCredentials credentials) throws InterruptedException, Exception {
//        if (credentials.getBackupUrl() == null) { //standalone controller
//            throw new JocBadRequestException("There is no Controller cluster configured with the Id: " + credentials.getControllerId());
//        }
//        List<ControllerClusterCallable> tasks = Arrays.asList(new ControllerClusterCallable("Primary", credentials.getUrl()),
//                new ControllerClusterCallable("Backup", credentials.getBackupUrl()));
//        List<ClusterState> controllers = new ArrayList<>(2);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//        try {
//            for (Future<ClusterState> result : executorService.invokeAll(tasks)) {
//                try {
//                    controllers.add(result.get());
//                } catch (ExecutionException e) {
//                    if (e.getCause() != null) {
//                        throw (Exception) e.getCause();
//                    } else {
//                        throw e; 
//                    }
//                }
//            }
//        } finally {
//            executorService.shutdown();
//        }
//        return controllers.stream().collect(Collectors.toMap(ClusterState::getId, Function.identity()));
//    }
//    
//    private static void f(ProxyCredentials credentials) throws InterruptedException, Exception {
//        Map<String, ClusterState> clusterStates = getClusterStatus(credentials);
//
//        Optional<String> activeIdOpt = clusterStates.values().stream().map(ClusterState::getSetting).map(ClusterSetting::getActiveId).filter(
//                Objects::nonNull).findAny();
//        if (activeIdOpt.isPresent()) {
//            ClusterState activeClusterState = clusterStates.get(activeIdOpt.get());
//        }
//        
//    }

//    private static List<ClusterState> getClusterStatus(String controllerId) throws InterruptedException, JocException, Exception {
//        List<DBItemInventoryJSInstance> schedulerInstances = Proxies.getControllerDbInstances().get(controllerId);
//        if (schedulerInstances == null || schedulerInstances.size() < 2) { // standalone controller
//            return null;
//        }
//        //List<ControllerClusterCallable> tasks = schedulerInstances.stream().map(DBItemInventoryJSInstance::getUri).map(i -> i).collect(Collectors.toList());
//        List<ClusterState> masters = new ArrayList<>(schedulerInstances.size());
//
//        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(10, tasks.size()));
//        try {
//            for (Future<ClusterState> result : executorService.invokeAll(tasks)) {
//                try {
//                    masters.add(result.get());
//                } catch (ExecutionException e) {
//                    if (e.getCause() instanceof JocException) {
//                        throw (JocException) e.getCause();
//                    } else {
//                        throw (Exception) e.getCause();
//                    }
//                }
//            }
//        } finally {
//            executorService.shutdown();
//        }
//        return masters;
//    }
}
