package com.sos.joc.db.cluster;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.joc.Globals;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.JocConfigurationException;

public class CheckInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInstance.class);

    public static void check() throws JocConfigurationException, SOSHibernateException {

        SOSHibernateSession session = null;

        try {
            String clusterId = Globals.getClusterId();
            Integer ordering = Globals.getOrdering();
            boolean isApiServer = Globals.isApiServer;
            Boolean prevWasApiServer = Globals.prevWasApiServer;

            Optional<DBItemJocInstance> dbInstanceOpt = Optional.empty();

            session = Globals.createSosHibernateStatelessConnection(CheckInstance.class.getSimpleName());
            JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(session);
            List<DBItemJocInstance> instances = dbLayer.getInstances("ordering, id");
            String memberId = Globals.getMemberId();
            if (instances != null && !instances.isEmpty()) {

//                if (instances.stream().map(DBItemJocInstance::getClusterId).distinct().count() > 1) {
//                    LOGGER.error("There are more than one JOC Cluster ID in the JOC_INSTANCES table.");
//                    throw new JocConfigurationException("There are more than one JOC Cluster ID in the JOC_INSTANCES table.");
//                } else {
                    //String dbClusterId = instances.get(0).getClusterId();
                    //if (dbClusterId.equals(clusterId)) {
                        Map<Integer, Long> orderings = instances.stream().collect(Collectors.groupingBy(DBItemJocInstance::getOrdering, Collectors
                                .counting()));
                        Set<Long> changedIds = new HashSet<>();
                        
                        if (orderings.values().stream().filter(v -> v > 1).findAny().isPresent()) {
                            // ordering not unique
                            LOGGER.info("The ordering of the JOC instances are not unique");
                            LOGGER.info("...try to repair the ordering to a strictly monotonous sequence without gaps");
                            int maxOrdering = instances.stream().mapToInt(DBItemJocInstance::getOrdering).max().orElse(0);
                            int curOrdering = -129; // tinyInt [-128, 127]
                            for (DBItemJocInstance instance : instances) {
                                if (curOrdering == instance.getOrdering()) {
                                    instance.setOrdering(++maxOrdering);
                                    changedIds.add(instance.getId());
                                } else {
                                    curOrdering = instance.getOrdering();
                                }
                            }
                            // strictly monotonous sequence without gaps but current memberId get current ordering
                            int index = 0;
                            dbInstanceOpt = instances.stream().filter(i -> memberId.equals(i.getMemberId())).findFirst();
                            if (dbInstanceOpt.isPresent()) {
                                for (DBItemJocInstance instance : instances) {
                                    if (instance.equals(dbInstanceOpt.get())) {
                                        if (ordering != instance.getOrdering()) {
                                            instance.setOrdering(ordering);
                                            changedIds.add(instance.getId());
                                        }
                                    } else {
                                        if (index == ordering) {
                                            index++;
                                        }
                                        if (index != instance.getOrdering()) {
                                            instance.setOrdering(index);
                                            changedIds.add(instance.getId());
                                        }
                                    }
                                    index++;
                                }
                            } else {
                                for (DBItemJocInstance instance : instances) {
                                    if (index != instance.getOrdering()) {
                                        instance.setOrdering(index);
                                        changedIds.add(instance.getId());
                                    }
                                    index++;
                                }
                            }
                        }
                        
                        for (DBItemJocInstance instance : instances) {
                            if (instance.getApiServer() && !"api".equals(instance.getClusterId())) {
                                instance.setClusterId("api");
                                changedIds.add(instance.getId());
                            } else if (!instance.getApiServer() && !"joc".equals(instance.getClusterId())) {
                                instance.setClusterId("joc");
                                changedIds.add(instance.getId());
                            }
                        }
                        
                        // update instances with changed ordering
                        for (DBItemJocInstance instance : instances) {
                            if (changedIds.contains(instance.getId())) {
                                session.update(instance);
                            }
                        }

                        dbInstanceOpt = instances.stream().filter(i -> ordering == i.getOrdering()).findFirst();
                        if (dbInstanceOpt.isPresent()) {
                            DBItemJocInstance dbInstance = dbInstanceOpt.get();
                            // TODO dont't use memberId; use isApiServer, prevWasApiServer
                            if (!memberId.equals(dbInstance.getMemberId())) {
                                if (instanceIsAlive(session, dbInstance)) {
                                    String msg = "There is already a running JOC instance with ID '" + dbInstance.getClusterId() + "#" + ordering
                                            + "'. You can modify the 'ordering' in the joc.properties file.";
                                    LOGGER.error(msg);
                                    throw new JocConfigurationException(msg);
                                } else {
                                    // update memberId, osId, setDataDirectory and delete row with memberId to avoid constraint violation
                                    dbInstanceOpt = instances.stream().filter(i -> memberId.equals(i.getMemberId())).findFirst();
                                    if (dbInstanceOpt.isPresent()) {
                                        session.delete(dbInstanceOpt.get());
                                    }
                                    try {
                                        DBItemInventoryOperatingSystem osItem = JocInstance.getOS(new DBLayerJocCluster(session), Globals
                                                .getHostname());
                                        dbInstance.setOsId(osItem.getId());
                                    } catch (Exception e) {
                                        LOGGER.error("", e);
                                    }
                                    dbInstance.setDataDirectory(Globals.getDataDirectory());
                                    dbInstance.setMemberId(memberId);
                                    dbInstance.setApiServer(isApiServer);
                                    dbInstance.setClusterId(clusterId);
                                    session.update(dbInstance);
                                }
                            }
                        }

//                    } else {
//                        // Check: Only one clusterId has to be in DB
//                        String msg = "A JOC Cluster ID '" + dbClusterId + "' already exists in the database. The JOC Cluster ID ('" + clusterId
//                                + "') of this instance is unequal. You can modify the 'cluster_id' in the joc.properties file.";
//                        LOGGER.error(msg);
//                        throw new JocConfigurationException(msg);
                    //}
                }
//            }

        } finally {
            Globals.disconnect(session);
        }
    }

    public static void stopJOC() {
        String jettyHome = System.getProperty("jetty.home");
        if (jettyHome != null && !jettyHome.isEmpty()) {
            if (SOSShell.IS_WINDOWS) {
                // Path jettyHomePath = Paths.get(jettyHome);
                // Path serviceFolder = jettyHomePath.resolve("..\\service").normalize();
                // Path startJar = jettyHomePath.resolve("jetty").resolve("start.jar");
                // Path javaExe = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java.exe");
                // // TODO determine stop port. It would be offer from the setup (maybe the whole stop command)
                // int jettyStopPort = 40446;
                //
                // if (Files.exists(serviceFolder) && Files.exists(startJar)) {
                // try {
                // Files.list(serviceFolder).map(Path::getFileName).map(Path::toString).filter(f -> f.endsWith(".exe")).filter(f -> f.startsWith(
                // "js7_")).map(f -> f.replaceFirst("w?\\.exe", "")).findAny().ifPresent(serviceName -> {
                // new Thread(() -> {
                // String command = String.format("\"%s\" -jar \"%s\" --stop STOP.KEY=%s STOP.PORT=%d STOP.WAIT=10", javaExe
                // .toString(), startJar.toString(), serviceName, jettyStopPort);
                // executeCommand(command);
                // }).start();
                // });
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
                // }
            } else {
                Path startscript = Paths.get(jettyHome).resolve("bin").resolve("jetty.sh");
                if (Files.exists(startscript)) {
                    System.out.println("...try to stop Jetty in 3s");
                    new Thread(() -> {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            //
                        }
                        executeCommand(startscript.toString() + " stop");
                    }).start();
                }
            }
        }
    }

    private static void executeCommand(String command) {
        SOSCommandResult result = SOSShell.executeCommand(command);
        if (result.getStdErr() != null && !result.getStdErr().isEmpty()) {
            System.out.println("StdErr: " + result.getStdErr());
        }
        if (result.getStdOut() != null && !result.getStdOut().isEmpty()) {
            System.out.println("StdOut: " + result.getStdOut());
        }
        System.out.println("Exit code of '" + result.getCommand() + "': " + result.getExitCode());
    }

    private static boolean instanceIsAlive(SOSHibernateSession session, DBItemJocInstance dbItem) {
        if (dbItem != null && dbItem.getHeartBeat() != null) {
            // dbItem.getHeartBeat() - database UTC datetime
            try {
                if (!JocCluster.isHeartBeatExceeded(session.getCurrentUTCDateTime(), dbItem.getHeartBeat())) {
                    return true;
                }
            } catch (SOSHibernateException e) {
                Instant oneMinuteAgo = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(1));
                if (dbItem.getHeartBeat().toInstant().isAfter(oneMinuteAgo)) {
                    return true;
                }
            }
        }
        return false;
    }

}
