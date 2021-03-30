package com.sos.joc.controllers.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.ControllerAnswer;
import com.sos.joc.classes.jobscheduler.ControllerCallable;
import com.sos.joc.controllers.resource.IControllersResource;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.ControllerId;
import com.sos.joc.model.controller.Controllers;
import com.sos.schema.JsonValidator;

@Path("controllers")
public class ControllersResourceImpl extends JOCResourceImpl implements IControllersResource {

    private static final String API_CALL = "./controllers";

    @Override
    public JOCDefaultResponse postJobschedulerInstancesP(String accessToken, byte[] filterBytes) {
        return postJobschedulerInstances(accessToken, filterBytes, true);
    }

    @Override
    public JOCDefaultResponse postJobschedulerInstances(String accessToken, byte[] filterBytes) {
        return postJobschedulerInstances(accessToken, filterBytes, false);
    }

    public JOCDefaultResponse postJobschedulerInstances(String accessToken, byte[] filterBytes, boolean onlyDb) {
        SOSHibernateSession connection = null;

        try {
            String apiCall = API_CALL;
            if (onlyDb) {
                apiCall += "/p";
            }
            initLogging(apiCall, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId controllerId = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId.getControllerId(), getControllerPermissions(controllerId
                    .getControllerId(), accessToken).getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            Controllers entity = new Controllers();
            entity.setControllers(getControllers(controllerId.getControllerId(), accessToken, connection, onlyDb, jobschedulerUser.getSosShiroCurrentUser()));
            if (onlyDb) {
                entity.setAgents(getAgents(entity.getControllers().stream().map(Controller::getControllerId).collect(Collectors.toSet()), connection));
            }
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
    
    private static List<Agent> getAgents(Set<String> controllerIds, SOSHibernateSession connection) {
        InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
        List<DBItemInventoryAgentInstance> agents = agentDBLayer.getAgentsByControllerIds(controllerIds);
        if (agents != null) {
            Map<String, Set<String>> allAliases = agentDBLayer.getAgentNamesByAgentIds(agents.stream().map(DBItemInventoryAgentInstance::getAgentId)
                    .collect(Collectors.toSet()));
            return agents.stream().map(a -> {
                Agent agent = new Agent();
                agent.setAgentId(a.getAgentId());
                agent.setAgentName(a.getAgentName());
                agent.setAgentNameAliases(allAliases.get(a.getAgentId()));
                agent.setDisabled(a.getDisabled());
                agent.setIsClusterWatcher(a.getIsWatcher());
                agent.setUrl(a.getUri());
                return agent;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private static List<Controller> getControllers(String controllerId, String accessToken, SOSHibernateSession connection, boolean onlyDb,
            SOSShiroCurrentUser user) throws InterruptedException, JocException, Exception {
        return getControllerAnswers(controllerId, accessToken, connection, onlyDb, user).stream()
                .map(Controller.class::cast).collect(Collectors.toList());
    }

    public static List<ControllerAnswer> getControllerAnswers(String controllerId, String accessToken, SOSHibernateSession connection)
            throws InterruptedException, JocException, Exception {
        return getControllerAnswers(controllerId, accessToken, connection, false, null);
    }

    public static List<ControllerAnswer> getControllerAnswers(String jobSchedulerId, String accessToken, SOSHibernateSession connection, boolean onlyDb,
            SOSShiroCurrentUser user) throws InterruptedException, JocException, Exception {
        InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
        //List<DBItemInventoryJSInstance> schedulerInstances = Proxies.getControllerDbInstances().get(jobSchedulerId);
        List<DBItemInventoryJSInstance> schedulerInstances = instanceLayer.getInventoryInstancesByControllerId(jobSchedulerId);
        List<ControllerAnswer> masters = new ArrayList<ControllerAnswer>();
        if (schedulerInstances != null) {
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<ControllerCallable> tasks = new ArrayList<ControllerCallable>();
            for (DBItemInventoryJSInstance schedulerInstance : schedulerInstances) {
                // skip all masters where the user doesn't have the permission to see its status
                if (jobSchedulerId.isEmpty() && user != null && !user.getControllerPermissions(schedulerInstance.getControllerId())
                        .getView()) {
                    continue;
                }
                tasks.add(new ControllerCallable(schedulerInstance, osDBLayer.getInventoryOperatingSystem(schedulerInstance.getOsId()), accessToken,
                        onlyDb));
            }
            if (!tasks.isEmpty()) {
                if (tasks.size() == 1) {
                    masters.add(tasks.get(0).call());
                } else {
                    ExecutorService executorService = Executors.newFixedThreadPool(Math.min(10, tasks.size()));
                    try {
                        for (Future<ControllerAnswer> result : executorService.invokeAll(tasks)) {
                            try {
                                masters.add(result.get());
                            } catch (ExecutionException e) {
                                if (e.getCause() instanceof JocException) {
                                    throw (JocException) e.getCause();
                                } else {
                                    throw (Exception) e.getCause();
                                }
                            }
                        }
                    } finally {
                        executorService.shutdown();
                    }
                }
            }

            if (!onlyDb) {
                for (ControllerAnswer master : masters) {
                    Long osId = osDBLayer.saveOrUpdateOSItem(master.getDbOs());
                    master.setOsId(osId);

                    if (master.dbInstanceIsChanged()) {
                        instanceLayer.updateInstance(master.getDbInstance());
                    }
                }
            }
        }
        return masters;
    }

}
