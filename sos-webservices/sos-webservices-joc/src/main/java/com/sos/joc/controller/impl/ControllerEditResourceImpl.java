package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.controller.model.command.Overview;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.controller.ControllerAnswer;
import com.sos.joc.classes.controller.ControllerCallable;
import com.sos.joc.classes.controller.States;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.ProxiesEdit;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.controller.resource.IControllerEditResource;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.joc.impl.StateImpl;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.RegisterClusterWatchAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.controller.ConnectionStateText;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.JobScheduler200;
import com.sos.joc.model.controller.RegisterParameter;
import com.sos.joc.model.controller.RegisterParameters;
import com.sos.joc.model.controller.Role;
import com.sos.joc.model.controller.TestConnect;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerApi;
import reactor.core.publisher.Flux;

@Path("controller")
public class ControllerEditResourceImpl extends JOCResourceImpl implements IControllerEditResource {

    private static final String API_CALL_REGISTER = "./controller/register";
    private static final String API_CALL_DELETE = "./controller/unregister";
    private static final String API_CALL_TEST = "./controller/test";
    private static final String isUrlPattern = "^https?://[^\\s]+$";
    private static final Predicate<String> isUrl = Pattern.compile(isUrlPattern).asPredicate();

    @Override
    public JOCDefaultResponse registerController(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REGISTER, filterBytes, accessToken);
            
            if (!StateImpl.isActive(API_CALL_REGISTER, null)) {
                throw new JocServiceException("Registering the Controllers is possible only in the active JOC node.");
            }
            
            JsonValidator.validateFailFast(filterBytes, RegisterParameters.class);
            RegisterParameters body = Globals.objectMapper.readValue(filterBytes, RegisterParameters.class);
            
            String controllerId = body.getControllerId();
            if (controllerId == null) {
                controllerId = ""; 
            }
            
            RegisterClusterWatchAgent clusterWatcher = body.getClusterWatcher();
            
            if (body.getControllers().size() < 2) {
                clusterWatcher = null;
            } else {
                AgentHelper.throwJocMissingLicenseException("missing license for Controller cluster");
            }
            boolean requestWithEmptyControllerId = controllerId.isEmpty();
            int index = 0;
            for (RegisterParameter controller : body.getControllers()) {
                
                if (!isUrl.test(controller.getUrl())) {
                    throw new JocBadRequestException("$.controllers[" + index + "].url: does not match the url pattern " + isUrlPattern);
                }
                
                if (controller.getClusterUrl() != null && !controller.getClusterUrl().isEmpty() && !isUrl.test(controller.getClusterUrl())) {
                    throw new JocBadRequestException("$.controllers[" + index + "].clusterUrl: does not match the url pattern " + isUrlPattern);
                }
                
                if (index == 1 && controller.getUrl().equals(body.getControllers().get(0).getUrl())) {
                    throw new JocBadRequestException("The cluster members must have the different URLs"); 
                }
                if (index == 1 && controller.getRole().equals(body.getControllers().get(0).getRole())) {
                    throw new JocBadRequestException("The members of a Controller Cluster must have different roles."); 
                }
                if (index == 1 && body.getControllers().stream().anyMatch(c -> Role.STANDALONE.equals(c.getRole()))) {
                    throw new JocBadRequestException("The members of a Controller Cluster must have roles PRIMARY and BACKUP."); 
                }

                String otherUri = index == 0 ? null : body.getControllers().get(0).getUrl();
                Controller jobScheduler = testConnection(controller.getUrl(), controllerId, otherUri, false);
                if (jobScheduler.getConnectionState().get_text() == ConnectionStateText.unreachable) {
                    if (requestWithEmptyControllerId) {
                        throw new ControllerConnectionRefusedException(controller.getUrl().toString());
//                    } else {
//                        LOGGER.warn("");
                    }
                } else {
                    if (requestWithEmptyControllerId) {
                        controllerId = jobScheduler.getControllerId();
                    }
                }
                
                index++;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REGISTER);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            
            if (!requestWithEmptyControllerId) { // try update controllers with given controllerId
                Integer securityLevel = instanceDBLayer.getSecurityLevel(controllerId);
                if (securityLevel != null && securityLevel != Globals.getJocSecurityLevel().intValue()) {
                    throw new JocObjectAlreadyExistException(String.format(
                            "Controller with ID '%s' is already configured with a different security level '%s'.", controllerId, JocSecurityLevel
                                    .fromValue(securityLevel)));
                }
            }
            
            ClusterAgent cWatcher = null;
            if (clusterWatcher != null) {
                SOSCheckJavaVariableName.test("Agent ID", clusterWatcher.getAgentId());
                if (!isUrl.test(clusterWatcher.getUrl())) {
                    throw new JocBadRequestException("$.clusterWatcher.url: does not match the url pattern " + isUrlPattern);
                }
                agentDBLayer.agentIdAlreadyExists(Collections.singleton(clusterWatcher.getAgentId()), controllerId);
                
                cWatcher = new ClusterAgent();
                cWatcher.setAgentId(clusterWatcher.getAgentId());
                cWatcher.setAgentName(clusterWatcher.getAgentName());
                cWatcher.setUrl(clusterWatcher.getUrl());
                cWatcher.setIsClusterWatcher(true);
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(body.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<DBItemInventoryJSInstance> instances = new ArrayList<>();
            DBItemInventoryOperatingSystem osSystem = null;
                        
            boolean firstController = instanceDBLayer.isEmpty();
            boolean clusterUriChanged = false;
            
            // sorted by isPrimary first
            List<DBItemInventoryJSInstance> dbControllers = instanceDBLayer.getInventoryInstancesByControllerId(controllerId);
            
            if (requestWithEmptyControllerId) { // try insert of new Controllers
                if (!dbControllers.isEmpty()) {
                    throw new JocObjectAlreadyExistException(String.format("Controller(s) with ID '%s' already exists", controllerId));
                }
                if (body.getControllers().size() == 1) {  // standalone
                    RegisterParameter controller = body.getControllers().get(0);
                    controller.setRole(Role.STANDALONE);
                    instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                } else {
                    for (RegisterParameter controller : body.getControllers()) {
                        instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                    }
                }
                
            } else { // try update controllers with given controllerId
                if (dbControllers.isEmpty()) {
                    throw new DBMissingDataException(String.format("Couldn't find Controller(s) with ID '%s'. Don't specify the \"controllerId\" in the request for a new registration.", controllerId));
                }
                if (body.getControllers().size() == 1) {  // standalone from request
                    RegisterParameter controller = body.getControllers().get(0);
                    controller.setRole(Role.STANDALONE);
                    if (dbControllers.size() == 2) { // but cluster in DB
                        for (DBItemInventoryJSInstance dbController : dbControllers) {
                            instanceDBLayer.deleteInstance(dbController); 
                        }
                        instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                    } else {
                        boolean uriChanged = !dbControllers.get(0).getUri().equalsIgnoreCase(controller.getUrl().toString());
                        DBItemInventoryJSInstance instance = setInventoryInstance(dbControllers.get(0), controller, controllerId);
                        if (uriChanged) {
                            instances.add(instance);
                        }
                        osSystem = osDBLayer.getInventoryOperatingSystem(instance.getOsId());
                        ControllerAnswer jobschedulerAnswer = new ControllerCallable(instance, osSystem, accessToken).call();
                        
                        Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
                        jobschedulerAnswer.setOsId(osId);
                        
                        instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
                    }
                } else { // cluster from request
                    if (dbControllers.size() == 1) { // but standalone in DB
                        instanceDBLayer.deleteInstance(dbControllers.get(0));
                        clusterUriChanged = true;
                        for (RegisterParameter controller : body.getControllers()) {
                            instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                        }
                    } else {
                        DBItemInventoryJSInstance instance = null;
                        boolean uriChanged = false;
                        for (RegisterParameter controller : body.getControllers()) {
                            if (Role.PRIMARY.equals(controller.getRole())) {
                                if (!uriChanged) {
                                    uriChanged = !dbControllers.get(0).getUri().equalsIgnoreCase(controller.getUrl().toString());
                                }
                                if (controller.getClusterUrl() == null) {
                                    controller.setClusterUrl(controller.getUrl());
                                }
                                clusterUriChanged = !dbControllers.get(0).getClusterUri().equalsIgnoreCase(controller.getClusterUrl().toString());
                                instance = setInventoryInstance(dbControllers.get(0), controller, controllerId);
                            } else {
                                if (!uriChanged) {
                                    uriChanged = !dbControllers.get(1).getUri().equalsIgnoreCase(controller.getUrl().toString());
                                }
                                if (controller.getClusterUrl() == null) {
                                    controller.setClusterUrl(controller.getUrl());
                                }
                                clusterUriChanged = !dbControllers.get(1).getClusterUri().equalsIgnoreCase(controller.getClusterUrl().toString());
                                instance = setInventoryInstance(dbControllers.get(1), controller, controllerId);
                            }
                            instances.add(instance);
                            osSystem = osDBLayer.getInventoryOperatingSystem(instance.getOsId());
                            ControllerAnswer jobschedulerAnswer = new ControllerCallable(instance, osSystem, accessToken).call();
                            
                            Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
                            jobschedulerAnswer.setOsId(osId);
                            
                            instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
                        }
                        if (!uriChanged) {
                            instances.clear();
                        }
                    }
                }
            }
            
            List<ClusterAgent> agentWatchers = new ArrayList<>();
            boolean controllerUpdateRequired = false;
            boolean updateAgentRequired = false;
            
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Collections.singleton(controllerId));
            
            if (clusterWatcher != null) {
                final String agentId = clusterWatcher.getAgentId();
                //List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Collections.singleton(controllerId));
                boolean clusterWatcherIsNew = true;
                
                if (dbAgents != null && !dbAgents.isEmpty()) {
                    Optional<DBItemInventoryAgentInstance> dbAgentOpt = dbAgents.stream().filter(a -> a.getAgentId().equals(agentId)).findAny();
                    if (dbAgentOpt.isPresent()) { // cluster watcher is not new
                        clusterWatcherIsNew = false;
                        DBItemInventoryAgentInstance dbAgent = dbAgentOpt.get();
                        dbAgents.remove(dbAgent);
                        boolean watcherIsChanged = false;
                        if (!dbAgent.getIsWatcher()) { // cluster watcher is not already cluster watcher
                            controllerUpdateRequired = true;
                            watcherIsChanged = true;
                            dbAgent.setIsWatcher(true);
                        }
                        if (!dbAgent.getAgentName().equals(clusterWatcher.getAgentName())) {
                            watcherIsChanged = true;
                            dbAgent.setAgentName(clusterWatcher.getAgentName());
                        }
                        if (!dbAgent.getUri().equals(clusterWatcher.getUrl())) {
                            controllerUpdateRequired = true;
                            updateAgentRequired = true;
                            watcherIsChanged = true;
                            dbAgent.setUri(clusterWatcher.getUrl());
                            dbAgent.setDeployed(false);
                            
                            DBItemInventorySubAgentInstance primaryDirector = agentDBLayer.getDirectorInstance(dbAgent.getAgentId(),
                                    SubagentDirectorType.PRIMARY_DIRECTOR.intValue());
                            if (primaryDirector != null) {
                                SubAgent subAgent = new SubAgent();
                                subAgent.setSubagentId(primaryDirector.getSubAgentId());
                                cWatcher.setSubagents(Collections.singletonList(subAgent));

                                primaryDirector.setUri(clusterWatcher.getUrl());
                                primaryDirector.setDeployed(false);

                                agentDBLayer.getSession().update(primaryDirector);
                            }
                            cWatcher.setDisabled(dbAgent.getDisabled());
                        }
                        if (watcherIsChanged) {
                            agentDBLayer.updateAgent(dbAgent);
                        }
                    }
                    for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                        if (dbAgent.getIsWatcher()) {
                            controllerUpdateRequired = true;
                            dbAgent.setIsWatcher(false);
                            agentDBLayer.updateAgent(dbAgent);
                        }
                    }
                }
                
                if (clusterWatcherIsNew) {
                    
                    if (clusterWatcher.getPrimaryDirectorId() == null || clusterWatcher.getPrimaryDirectorId().isEmpty()) {
                        clusterWatcher.setPrimaryDirectorId(clusterWatcher.getAgentId());
                    } else {
                        SOSCheckJavaVariableName.test("Primary Director ID", clusterWatcher.getPrimaryDirectorId());
                        // TODO check uniqueness
                    }
                    
                    int position = agentDBLayer.getAgentMaxOrdering();
                    controllerUpdateRequired = true;
                    updateAgentRequired = true;
                    DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                    dbAgent.setId(null);
                    dbAgent.setAgentId(clusterWatcher.getAgentId());
                    dbAgent.setAgentName(clusterWatcher.getAgentName());
                    dbAgent.setControllerId(controllerId);
                    dbAgent.setHidden(false);
                    dbAgent.setDisabled(false);
                    dbAgent.setDeployed(false);
                    dbAgent.setIsWatcher(true);
                    dbAgent.setOsId(0L);
                    dbAgent.setStartedAt(null);
                    dbAgent.setUri(clusterWatcher.getUrl());
                    dbAgent.setVersion(null);
                    dbAgent.setTitle(null);
                    dbAgent.setOrdering(++position);
                    agentDBLayer.saveAgent(dbAgent);
                    
                    if (!clusterWatcher.getAsStandaloneAgent()) {
                        int position2 = agentDBLayer.getSubagentMaxOrdering();
                        DBItemInventorySubAgentInstance dbSubAgent = new DBItemInventorySubAgentInstance();
                        dbSubAgent.setAgentId(clusterWatcher.getAgentId());
                        dbSubAgent.setDeployed(false);
                        dbSubAgent.setDisabled(false);
                        dbSubAgent.setId(null);
                        dbSubAgent.setIsDirector(SubagentDirectorType.PRIMARY_DIRECTOR);
                        dbSubAgent.setIsWatcher(true);
                        dbSubAgent.setOrdering(++position2);
                        dbSubAgent.setOsId(0L);
                        dbSubAgent.setSubAgentId(clusterWatcher.getPrimaryDirectorId());
                        dbSubAgent.setUri(clusterWatcher.getUrl());
                        dbSubAgent.setModified(Date.from(Instant.now()));
                        agentDBLayer.getSession().save(dbSubAgent);
                        
                        SubAgent subAgent = new SubAgent();
                        subAgent.setSubagentId(clusterWatcher.getPrimaryDirectorId());
                        cWatcher.setSubagents(Collections.singletonList(subAgent));
                    }
                    
                    cWatcher.setDisabled(false);
                }
                if (updateAgentRequired) {
                    agentWatchers.add(cWatcher);
                }
                
            } else {
                if (dbAgents != null) {
                    for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                        if (dbAgent.getIsWatcher()) {
                            controllerUpdateRequired = true;
                            dbAgent.setIsWatcher(false);
                            agentDBLayer.updateAgent(dbAgent);
                        }
                    }
                }
            }
            
            
                
                
            instances = instances.stream().filter(Objects::nonNull).collect(Collectors.toList());
//            boolean proxyIsUpdated = false;
            if (!instances.isEmpty()) {
                ProxiesEdit.update(instances);
//                proxyIsUpdated = true;
            }
            
            JControllerApi controllerApi = null;
            
            //if (clusterUriChanged || controllerUpdateRequired) {
            if (dbControllers.size() == 2) {
                try {
                    controllerApi = ControllerApi.of(controllerId);
                    ClusterWatch.getInstance().appointNodes(controllerId, controllerApi, agentDBLayer, accessToken, getJocError());
                } catch (JocBadRequestException e) {
                }
            }
            
            if (!agentWatchers.isEmpty()) {
                final String cId = controllerId;

                // TODO consider old Agent cannot convert to new Agents
                Map<AgentPath, JAgentRef> knownAgents = getKnownAgents(controllerId);
                if (controllerApi == null) {
                    controllerApi = ControllerApi.of(controllerId);
                }

                controllerApi.updateItems(Flux.fromStream(agentWatchers.stream().map(a -> {
                    JAgentRef agentRef = knownAgents.get(AgentPath.of(a.getAgentId()));

                    String subagentIdFromRequest = a.getAgentId();
                    if (a.getSubagents() != null && !a.getSubagents().isEmpty()) {
                        if (a.getSubagents().get(0).getSubagentId() != null && !a.getSubagents().get(0).getSubagentId().isEmpty()) {
                            subagentIdFromRequest = a.getSubagents().get(0).getSubagentId();
                        }
                    }
                    SubagentId subagentId = agentRef != null && agentRef.director().isPresent() ? agentRef.director().get() : SubagentId.of(
                            subagentIdFromRequest);
                    return Arrays.asList(JUpdateItemOperation.addOrChangeSimple(createAgent(a, subagentId)), JUpdateItemOperation
                            .addOrChangeSimple(createSubagentDirector(a, subagentId)));
                
                }).flatMap(List::stream))).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), null);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_REGISTER);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            List<String> agentIds = agentWatchers.stream().map(ClusterAgent::getAgentId).distinct().collect(Collectors.toList());
                            List<String> subagentIds = agentWatchers.stream().map(ClusterAgent::getSubagents).filter(Objects::nonNull).filter(l -> !l
                                    .isEmpty()).map(l -> l.get(0)).map(SubAgent::getSubagentId).distinct().collect(Collectors.toList());
                            dbLayer1.setAgentsDeployed(agentIds);
                            dbLayer1.setSubAgentsDeployed(subagentIds);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(cId, agentIds));
                        } catch (Exception e1) {
                            Globals.rollback(connection1);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), null);
                        } finally {
                            Globals.disconnect(connection1);
                        }
                    }
                });
            }
            
//            if (!proxyIsUpdated && controllerUpdateRequired) {
//                ProxiesEdit.update(controllerId);
//            }
            
            if (firstController) { // GUI needs permissions directly for the first controller(s)
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getJobschedulerUser().getSOSAuthCurrentAccount()
                        .getSosPermissionJocCockpitControllers()));
            } else {
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private Map<AgentPath, JAgentRef> getKnownAgents(String controllerId) {
        // TODO consider old Agent cannot convert to new Agents
        Map<AgentPath, JAgentRef> knownAgents = Collections.emptyMap();
        try {
            JControllerState currentState = Proxy.of(controllerId).currentState();
            knownAgents = currentState.pathToAgentRef();
        } catch (Exception e) {
            //
        }
        return knownAgents;
    }
    
    private static JAgentRef createAgent(Agent a, SubagentId subagentId) {
        return JAgentRef.of(AgentPath.of(a.getAgentId()), subagentId);
    }
    
    private static JSubagentItem createSubagentDirector(Agent a, SubagentId subagentId) {
        return JSubagentItem.of(subagentId, AgentPath.of(a.getAgentId()), Uri.of(a.getUrl()), a.getDisabled());
    }
    
    @Override
    public JOCDefaultResponse deleteController(String accessToken, byte[] filterBytes) {
        return unregisterController(accessToken, filterBytes); //alias for cleanup
    }

    @Override
    public JOCDefaultResponse unregisterController(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            
            if (!StateImpl.isActive(API_CALL_DELETE, null)) {
                throw new JocServiceException("Deregistering the Controllers is possible only in the active JOC node.");
            }
            
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter controllerObj = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String controllerId = controllerObj.getControllerId();
            storeAuditLog(controllerObj.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            for (ProxyUser user : ProxyUser.values()) {
                Proxy.close(controllerId, user);
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            
            
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Collections.singleton(controllerId));
            if (dbAgents != null) {
                //throw new ControllerConflictException("Agents has to be removed before the Controller"); 
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    agentDBLayer.deleteInstance(dbAgent);
                }
            }
            List<DBItemInventoryJSInstance> instances = instanceDBLayer.getInventoryInstancesByControllerId(controllerId);
            if (instances != null) {
               for (DBItemInventoryJSInstance instance : instances) {
                   instanceDBLayer.deleteInstance(instance);
                   if (!instanceDBLayer.isOperatingSystemUsed(instance.getOsId())) {
                       osDBLayer.deleteOSItem(osDBLayer.getInventoryOperatingSystem(instance.getOsId()));
                   }
                   //TODO some other tables should maybe deleted !!!
               }
               ProxiesEdit.remove(controllerId);
            }
            Globals.commit(connection);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    @Override
    public JOCDefaultResponse testControllerConnection(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_TEST, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, TestConnect.class);
            TestConnect jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, TestConnect.class);
            
            String controllerId = jobSchedulerBody.getControllerId();
            if (controllerId == null) {
                controllerId = ""; 
            }
            
            if (!isUrl.test(jobSchedulerBody.getUrl())) {
                throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Controller jobScheduler = testConnection(jobSchedulerBody.getUrl(), controllerId, null);
            
            JobScheduler200 entity = new JobScheduler200();
            entity.setController(jobScheduler);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private Controller testConnection(String controllerURI, String controllerId, String otherControllerURI) throws JocException {
        return testConnection(controllerURI, controllerId, otherControllerURI, true);
    }
    
    private Controller testConnection(String controllerURI, String controllerId, String otherControllerURI, boolean withThrow) throws JocException {
        Controller jobScheduler = new Controller();
        jobScheduler.setUrl(controllerURI.toString());
        jobScheduler.setIsCoupled(null);
        Overview answer = null;
        try {
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(controllerURI, getAccessToken());
            jocJsonCommand.setUriBuilderForOverview();
            answer = jocJsonCommand.getJsonObjectFromGet(Overview.class);
        } catch (ControllerInvalidResponseDataException e) {
            throw e;
        } catch (JocException e) {
            if (withThrow) {
                throw e;
            }
        }
        if (answer != null) {
            if (!controllerId.isEmpty() && !controllerId.equals(answer.getId())) {
                if (otherControllerURI != null) {
                    throw new ControllerInvalidResponseDataException(String.format(
                            "The cluster members must have the same Controller ID: %1$s -> %2$s, %3$s -> %4$s", otherControllerURI.toString(),
                            controllerId, controllerURI, answer.getId()));
                } else {
                    throw new ControllerInvalidResponseDataException(String.format(
                            "Connection was successful but Controller ID '%s' of URL '%s' is not the expected Controller ID '%s'", answer.getId(),
                            jobScheduler.getUrl(), controllerId));
                }
            }
            jobScheduler.setControllerId(answer.getId());
            jobScheduler.setConnectionState(States.getConnectionState(ConnectionStateText.established));
        } else {
            jobScheduler.setConnectionState(States.getConnectionState(ConnectionStateText.unreachable));
        }
        return jobScheduler;
    }
    
    private DBItemInventoryJSInstance storeNewInventoryInstance(InventoryInstancesDBLayer instanceDBLayer, InventoryOperatingSystemsDBLayer osDBLayer,
            RegisterParameter controller, String controllerId) throws DBInvalidDataException, DBConnectionRefusedException,
            JocObjectAlreadyExistException, ControllerInvalidResponseDataException {
        DBItemInventoryJSInstance instance = setInventoryInstance(null, controller, controllerId);
        Long newId = instanceDBLayer.saveInstance(instance);
        instance.setId(newId);

        ControllerAnswer jobschedulerAnswer = new ControllerCallable(instance, null, getAccessToken()).call();

        Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
        jobschedulerAnswer.setOsId(osId);

        if (jobschedulerAnswer.dbInstanceIsChanged()) {
            instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
        }
        return instance;
    }
    
    private DBItemInventoryJSInstance setInventoryInstance(DBItemInventoryJSInstance instance, RegisterParameter controller, String controllerId) {
        if (instance == null) {
            instance = new DBItemInventoryJSInstance();
            instance.setId(null);
            instance.setOsId(0L);
            instance.setStartedAt(null);
            instance.setVersion(null);
            instance.setJavaVersion(null);
        }
        Role role = controller.getRole();
        instance.setSecurityLevel(Globals.getJocSecurityLevel().intValue());
        instance.setControllerId(controllerId);
        instance.setUri(controller.getUrl().toString());
        if (controller.getTitle() == null || controller.getTitle().isEmpty()) {
            instance.setTitle(role.value());
        } else {
            instance.setTitle(controller.getTitle());
        }
        instance.setIsPrimary(role != Role.BACKUP);
        instance.setIsCluster(role != Role.STANDALONE);
        if (instance.getIsCluster()) {
            if (controller.getClusterUrl() != null) {
                instance.setClusterUri(controller.getClusterUrl().toString());
            } else {
                instance.setClusterUri(controller.getUrl().toString());
            }
        } else {
            instance.setClusterUri(null);
        }
        return instance;
    }

}
