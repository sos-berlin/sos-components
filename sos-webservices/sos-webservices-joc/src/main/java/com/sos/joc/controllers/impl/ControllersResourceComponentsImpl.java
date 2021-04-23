package com.sos.joc.controllers.impl;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.ControllerAnswer;
import com.sos.joc.classes.jobscheduler.States;
import com.sos.joc.controllers.resource.IControllersResourceComponents;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.controller.ClusterNodeStateText;
import com.sos.joc.model.controller.ComponentStateText;
import com.sos.joc.model.controller.Components;
import com.sos.joc.model.controller.ConnectionStateText;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.joc.model.controller.OperatingSystem;
import com.sos.joc.model.controller.Role;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.joc.ControllerConnectionState;
import com.sos.joc.model.joc.DB;
import com.sos.schema.JsonValidator;

@Path("controller")
public class ControllersResourceComponentsImpl extends JOCResourceImpl implements IControllersResourceComponents {

    private static final String API_CALL = "./controller/components";
    
    @Context UriInfo uriInfo;
    @Override
    public JOCDefaultResponse postComponents(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;

        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerIdReq.class);
            ControllerIdReq controllerIdObj = Globals.objectMapper.readValue(filterBytes, ControllerIdReq.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerIdObj.getControllerId(), accessToken)
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);

            Components entity = new Components();

            List<ControllerAnswer> controllers = ControllersResourceImpl.getControllerAnswers(controllerIdObj.getControllerId(), accessToken,
                    connection);
            //TODO controllerConnectionState from database, here a fake
            List<ControllerConnectionState> fakeControllerConnections = controllers.stream().map(c -> {
                ControllerConnectionState s = new ControllerConnectionState();
                s.setRole(c.getRole());
                s.setState(c.getConnectionState());
                return s;
            }).collect(Collectors.toList());
            List<ControllerConnectionState> unknownControllerConnections = controllers.stream().map(c -> {
                ControllerConnectionState s = new ControllerConnectionState();
                s.setRole(c.getRole());
                s.setState(States.getConnectionState(ConnectionStateText.unknown));
                return s;
            }).collect(Collectors.toList());
            
            ClusterType clusterType = getClusterType(controllers);
            entity.setClusterState(States.getClusterState(clusterType));
            entity.setControllers(controllers.stream().map(Controller.class::cast).collect(Collectors.toList()));
            entity.setJocs(setCockpits(connection, fakeControllerConnections, unknownControllerConnections));
            entity.setDatabase(getDB(connection));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private String getUri(String hostname) {
        String baseUri = uriInfo.getBaseUri().normalize().toString().replaceFirst("/joc/api(/.*)?$", "");
        if (baseUri.matches("https?://localhost:.*") && hostname != null && !hostname.equals("unknown")) {
            baseUri = baseUri.replaceFirst("^(https?://)localhost:", "$1" + hostname + ":");
        }
        return baseUri;
    }
    
    private String getHostname() {
        String hostname = "unknown";
        try {
            hostname = SOSShell.getHostname();
        } catch (UnknownHostException e) {
            // LOGGER.error(e.toString(), e);
        }
        return hostname;
    }
    
    private List<Cockpit> setCockpits(SOSHibernateSession connection, List<ControllerConnectionState> fakeControllerConnections, List<ControllerConnectionState> unknownControllerConnections)
            throws DBConnectionRefusedException, DBInvalidDataException {
        JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(connection);
        List<DBItemJocInstance> instances = dbLayer.getInstances();
        DBItemJocCluster activeInstance = dbLayer.getCluster();
        List<Cockpit> cockpits = new ArrayList<>();
        String hostname = getHostname();
        String curMemberId = hostname + ":" + SOSString.hash256(Paths.get(System.getProperty("user.dir")).toString());
        if (instances != null) {
            Boolean isCluster = instances.size() > 1;
            InventoryOperatingSystemsDBLayer dbOsLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<DBItemInventoryOperatingSystem> operatingSystems =  dbOsLayer.getOSItems(instances.stream().map(DBItemJocInstance::getOsId).filter(Objects::nonNull).collect(Collectors.toSet()));
            Map<Long, DBItemInventoryOperatingSystem> osMap = null;
            if (operatingSystems != null) {
                osMap = operatingSystems.stream().collect(Collectors.toMap(DBItemInventoryOperatingSystem::getId, Function.identity())); 
            }
            String version = readVersion();
            long nowSeconds = Instant.now().getEpochSecond();
            // TODO version should be in database
            for (DBItemJocInstance instance : instances) {
                Cockpit cockpit = new Cockpit();
                cockpit.setId(instance.getId());
                cockpit.setMemberId(instance.getMemberId());
                cockpit.setCurrent(curMemberId.equals(instance.getMemberId()));
                if (osMap != null) {
                    DBItemInventoryOperatingSystem osDB = osMap.get(instance.getOsId());
                    if (osDB != null) {
                        cockpit.setHost(osDB.getHostname());
                        OperatingSystem os = new OperatingSystem();
                        os.setArchitecture(osDB.getArchitecture());
                        os.setDistribution(osDB.getDistribution());
                        os.setName(osDB.getName());
                        cockpit.setOs(os);
                    }
                }
                try {
                    cockpit.setSecurityLevel(JocSecurityLevel.fromValue(instance.getSecurityLevel().toUpperCase()));
                } catch (Exception e ){
                    cockpit.setSecurityLevel(JocSecurityLevel.LOW);
                }
                cockpit.setStartedAt(instance.getStartedAt());
                cockpit.setTitle(instance.getTitle());
                cockpit.setVersion(version);
                cockpit.setLastHeartbeat(instance.getHeartBeat());
                
                // determine ClusterNodeState
                Boolean isActive = null;
                if (activeInstance != null) {
                    isActive = instance.getMemberId().equals(activeInstance.getMemberId());
                }
                cockpit.setClusterNodeState(States.getClusterNodeState(isActive, isCluster));
                
                // determine ComponentState/ConnectionState depends on last heart beat
                cockpit.setComponentState(States.getComponentState(ComponentStateText.operational));
                cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.established));
                cockpit.setControllerConnectionStates(fakeControllerConnections);
                
                if (cockpit.getLastHeartbeat() == null) {
                    if (!cockpit.getCurrent()) {
                        cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unknown));
                        cockpit.setComponentState(States.getComponentState(ComponentStateText.unknown));
                        cockpit.setControllerConnectionStates(unknownControllerConnections);
                    }
                } else {
                    long heartBeatSeconds = cockpit.getLastHeartbeat().toInstant().getEpochSecond();
                    if (nowSeconds - heartBeatSeconds <= 31) {
                        //retain unchanged established
                    } else if (nowSeconds - heartBeatSeconds <= 61) {
                        cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unstable));
                    } else {
                        if (!cockpit.getCurrent()) {
                            cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unknown));
                            cockpit.setComponentState(States.getComponentState(ComponentStateText.unknown));
                            cockpit.setControllerConnectionStates(unknownControllerConnections);
                        } else {
                            cockpit.setConnectionState(States.getConnectionState(ConnectionStateText.unstable));
                        }
                    }
                }
                
                if (cockpit.getCurrent()) {
                    String uri = getUri(hostname);
                    if (!uri.equals(instance.getUri())) {
                        instance.setUri(uri);
                        dbLayer.update(instance);
                    }
                }
                cockpit.setUrl(instance.getUri());
                
                cockpits.add(cockpit);
            }
        }
        return cockpits;
    }

    private static String readVersion() {
        String versionFile = "/version.json";
        try {
            InputStream stream = ControllersResourceComponentsImpl.class.getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                return Json.createReader(stream).readObject().getString("version", "unknown");
            }
        } catch (Exception e) {
            //
        }
        return "unknown";
    }

    private static DB getDB(SOSHibernateSession connection) throws SOSHibernateException {
        Enum<Dbms> dbms = connection.getFactory().getDbms();
        String stmt = null;
        String version = null;
        DB db = new DB();

        if (dbms == SOSHibernateFactory.Dbms.MSSQL) {
            db.setDbms("SQL Server");
            stmt = "select CONVERT(varchar(255), @@version)";
        } else if (dbms == SOSHibernateFactory.Dbms.MYSQL) {
            db.setDbms("MySQL");
            stmt = "select version()";
        } else if (dbms == SOSHibernateFactory.Dbms.ORACLE) {
            db.setDbms("Oracle");
            stmt = "select BANNER from v$version";
        } else if (dbms == SOSHibernateFactory.Dbms.PGSQL) {
            db.setDbms("PostgreSQL");
            stmt = "show server_version";
        } else if (dbms == SOSHibernateFactory.Dbms.H2) {
            db.setDbms("H2");
            stmt = "select h2version()";
        }

        if (stmt != null) {
            List<String> result = connection.getResultListNativeQuery(stmt);
            if (!result.isEmpty()) {
                version = result.get(0);
                if (version.contains("\n")) {
                    version = version.substring(0, version.indexOf("\n"));
                }
            }
            if (version != null) {
                if (dbms == SOSHibernateFactory.Dbms.MSSQL) {
                    // only first line
                    version = version.trim().split("\r?\n", 2)[0];
                }
                version = version.trim();
            }
        }
        // TODO different states
        db.setComponentState(States.getComponentState(ComponentStateText.operational));
        db.setConnectionState(States.getConnectionState(ConnectionStateText.established));
        db.setVersion(version);

        return db;
    }

    private static ClusterType getClusterType(List<ControllerAnswer> masters) {
        ClusterType clusterType = null;
        if (!masters.stream().anyMatch(m -> m.getRole() == Role.STANDALONE)) {
            int unreachables = masters.stream().filter(m -> m.getConnectionState().get_text() == ConnectionStateText.unreachable).mapToInt(m -> 1)
                    .sum();
            if (unreachables == masters.size()) {
                //
            } else if (unreachables == 0) {
                Optional<ControllerAnswer> j = masters.stream().filter(m -> m.getClusterNodeState().get_text() == ClusterNodeStateText.active).findAny();
                if (j.isPresent()) {
                    clusterType = j.get().getClusterState();
                } else {
                    clusterType = masters.get(0).getClusterState();
                }
            } else {
                ControllerAnswer j = masters.stream().filter(m -> m.getConnectionState().get_text() != ConnectionStateText.unreachable).findAny().get();
                clusterType = j.getClusterState();
                if (j.isCoupledOrPreparedTobeCoupled()) {
                    int index = masters.indexOf(j);
                    int otherIndex = (index + 1) % 2;
                    if (j.getClusterNodeState().get_text() == ClusterNodeStateText.active) {
                        masters.get(otherIndex).setClusterNodeState(States.getClusterNodeState(false, true));
                    } else {
                        masters.get(otherIndex).setClusterNodeState(States.getClusterNodeState(true, true));
                    }
                    if (j.getClusterState() == ClusterType.PREPARED_TO_BE_COUPLED) {
                        //masters.get(otherIndex).setComponentState(States.getComponentState(ComponentStateText.inoperable));
                    }
                    if (j.getClusterState() == ClusterType.COUPLED) {
                        masters.get(otherIndex).setIsCoupled(true);
                    }
                }
            }
        }
        return clusterType;
    }

}
