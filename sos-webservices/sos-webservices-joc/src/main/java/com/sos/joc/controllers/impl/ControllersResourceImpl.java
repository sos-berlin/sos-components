package com.sos.joc.controllers.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.ControllerAnswer;
import com.sos.joc.classes.jobscheduler.ControllerCallable;
import com.sos.joc.classes.proxy.Proxies;
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
            ControllerId controllerIdObj = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            
            String controllerId = controllerIdObj.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getView()).collect(
                                Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet(); 
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            Controllers entity = new Controllers();
            entity.setControllers(getControllers(allowedControllers, accessToken, connection, onlyDb));
            if (onlyDb) {
                Map<Boolean, List<Agent>> agents = getAgents(entity.getControllers().stream().map(Controller::getControllerId).collect(Collectors
                        .toSet()), connection);
                entity.setAgents(agents.get(false));
                entity.setClusterAgents(agents.get(true));
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
    
    private static Map<Boolean, List<Agent>> getAgents(Set<String> controllerIds, SOSHibernateSession connection) {
        InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
        List<String> clusterAgentIds = agentDBLayer.getClusterAgentIds(controllerIds, true);
        List<DBItemInventoryAgentInstance> agents = agentDBLayer.getAgentsByControllerIds(controllerIds);
        if (agents != null) {
            Map<String, Set<String>> allAliases = agentDBLayer.getAgentNamesByAgentIds(controllerIds);
            return agents.stream().map(a -> {
                Agent agent = new Agent();
                agent.setAgentId(a.getAgentId());
                agent.setAgentName(a.getAgentName());
                agent.setAgentNameAliases(allAliases.get(a.getAgentId()));
                agent.setHidden(a.getDisabled());
                agent.setIsClusterWatcher(a.getIsWatcher());
                if (clusterAgentIds.contains(a.getAgentId())) {
                    agent.setUrl(null);
                } else {
                    agent.setUrl(a.getUri());
                }
                return agent;
            }).collect(Collectors.groupingBy(a -> a.getUrl() == null));
        }
        return Collections.emptyMap();
    }

    private static List<Controller> getControllers(Set<String> allowedControllers, String accessToken, SOSHibernateSession connection, boolean onlyDb)
            throws InterruptedException, JocException, Exception {
        return getControllerAnswers(allowedControllers, accessToken, connection, onlyDb).stream().map(Controller.class::cast).collect(Collectors
                .toList());
    }

    public static List<ControllerAnswer> getControllerAnswers(String controllerId, String accessToken, SOSHibernateSession connection)
            throws InterruptedException, JocException, Exception {
        return getControllerAnswers(Collections.singleton(controllerId), accessToken, connection, false);
    }

    public static List<ControllerAnswer> getControllerAnswers(Set<String> allowedControllers, String accessToken, SOSHibernateSession connection,
            boolean onlyDb) throws InterruptedException, JocException, Exception {
        InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
        List<DBItemInventoryJSInstance> schedulerInstances = instanceLayer.getInventoryInstancesByControllerIds(allowedControllers);
        List<ControllerAnswer> masters = new ArrayList<ControllerAnswer>();
        if (schedulerInstances != null) {
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<ControllerCallable> tasks = new ArrayList<ControllerCallable>();
            for (DBItemInventoryJSInstance schedulerInstance : schedulerInstances) {
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
