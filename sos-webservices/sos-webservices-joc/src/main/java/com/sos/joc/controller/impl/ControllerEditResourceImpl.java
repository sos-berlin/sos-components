package com.sos.joc.controller.impl;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.command.Overview;
import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.jobscheduler.ControllerAnswer;
import com.sos.joc.classes.jobscheduler.ControllerCallable;
import com.sos.joc.classes.jobscheduler.States;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.ProxiesEdit;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.controller.resource.IControllerEditResource;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.agent.Agent;
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

import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

@Path("controller")
public class ControllerEditResourceImpl extends JOCResourceImpl implements IControllerEditResource {

    private static final String API_CALL_REGISTER = "./controller/register";
    private static final String API_CALL_DELETE = "./controller/cleanup";
    private static final String API_CALL_TEST = "./controller/test";

    @Override
    public JOCDefaultResponse registerController(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REGISTER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RegisterParameters.class);
            RegisterParameters body = Globals.objectMapper.readValue(filterBytes, RegisterParameters.class);
            
            String controllerId = body.getControllerId();
            if (controllerId == null) {
                controllerId = ""; 
            }
            
            if (body.getAgents() == null) {
                body.setAgents(Collections.emptyList());
            }
            Agent clusterWatcher = body.getClusterWatcher();
            // only for compatibility
            if (clusterWatcher == null && body.getAgents().size() == 1) {
                clusterWatcher = body.getAgents().get(0); 
            }
            if (body.getControllers().size() < 2) {
                clusterWatcher = null;
            }
            boolean requestWithEmptyControllerId = controllerId.isEmpty();
            int index = 0;
            for (RegisterParameter controller : body.getControllers()) {
                
                if (index == 1 && controller.getUrl().equals(body.getControllers().get(0).getUrl())) {
                    throw new JocBadRequestException("The cluster members must have the different URLs"); 
                }
                if (index == 1 && controller.getRole().equals(body.getControllers().get(0).getRole())) {
                    throw new JocBadRequestException("The members of a Controller Cluster must have different roles."); 
                }
                if (index == 1 && body.getControllers().stream().anyMatch(c -> Role.STANDALONE.equals(c.getRole()))) {
                    throw new JocBadRequestException("The members of a Controller Cluster must have roles PRIMARY and BACKUP."); 
                }
//                if (index == 1 && (body.getAgents().isEmpty() || !body.getAgents()
//                        .stream().anyMatch(Agent::getIsClusterWatcher))) {
//                    throw new JobSchedulerBadRequestException("A Controller Cluster needs at least one Agent Cluster Watcher.");
//                }
//                if (index == 1 && !body.getAgents().isEmpty() && body.getAgents()
//                        .stream().filter(Agent::getIsClusterWatcher).count() > 0) {
//                    throw new JobSchedulerBadRequestException("Only one Agent may be a Cluster Watcher.");
//                }

                URI otherUri = index == 0 ? null : body.getControllers().get(0).getUrl();
                Controller jobScheduler = testConnection(controller.getUrl(), controllerId, otherUri);
                if (jobScheduler.getConnectionState().get_text() == ConnectionStateText.unreachable) {
                    throw new ControllerConnectionRefusedException(controller.getUrl().toString());
                }
                
                controllerId = jobScheduler.getControllerId();
                index++;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REGISTER);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            
            if (!requestWithEmptyControllerId) { // try update controllers with given controllerId
                Integer securityLevel = instanceDBLayer.getSecurityLevel(controllerId);
                if (securityLevel != null && securityLevel != Globals.getJocSecurityLevel().intValue()) {
                    throw new JocObjectAlreadyExistException(String.format("Controller with Id '%s' is already configured with a different security level '%s'.",
                            controllerId, JocSecurityLevel.fromValue(securityLevel)));
                }
            }
            
            if (clusterWatcher != null) {
                clusterWatcher.setIsClusterWatcher(true);
                CheckJavaVariableName.test("Agent ID", clusterWatcher.getAgentId());
                agentDBLayer.agentIdAlreadyExists(Arrays.asList(clusterWatcher.getAgentId()), controllerId);
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
                if (dbControllers != null && !dbControllers.isEmpty()) {
                    throw new JocObjectAlreadyExistException(String.format("Controller(s) with id '%s' already exists", controllerId));
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
                                uriChanged = !dbControllers.get(0).getUri().equalsIgnoreCase(controller.getUrl().toString());
                                clusterUriChanged = !dbControllers.get(0).getClusterUri().equalsIgnoreCase(controller.getClusterUrl().toString());
                                instance = setInventoryInstance(dbControllers.get(0), controller, controllerId);
                            } else {
                                uriChanged = !dbControllers.get(1).getUri().equalsIgnoreCase(controller.getUrl().toString());
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
            
            List<JAgentRef> agentRefs = new ArrayList<>();
            boolean controllerUpdateRequired = false;
            boolean updateAgentRequired = false;
            
            if (clusterWatcher != null) {
                final String agentId = clusterWatcher.getAgentId();
                List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Arrays.asList(controllerId));
                boolean clusterWatcherIsNew = true;
                
                if (dbAgents != null && !dbAgents.isEmpty()) {
                    Optional<DBItemInventoryAgentInstance> dbAgentOpt = dbAgents.stream().filter(a -> a.getAgentId().equals(agentId)).findAny();
                    if (dbAgentOpt.isPresent()) { // cluster watcher is not new
                        clusterWatcherIsNew = false;
                        DBItemInventoryAgentInstance dbAgent = dbAgentOpt.get();
                        dbAgents.remove(dbAgent);
                        boolean watcherIsChanged = false;
                        if (!dbAgent.getIsWatcher()) { // cluster watcher is already cluster watcher
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
                        }
                        if (watcherIsChanged) {
                            agentDBLayer.updateAgent(dbAgent);
                        }
                    }
                    for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                        if (dbAgent.getIsWatcher()) {
                            dbAgent.setIsWatcher(false);
                            agentDBLayer.updateAgent(dbAgent);
                        }
                    }
                }
                
                if (clusterWatcherIsNew) {
                    controllerUpdateRequired = true;
                    updateAgentRequired = true;
                    DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                    dbAgent.setId(null);
                    dbAgent.setAgentId(clusterWatcher.getAgentId());
                    dbAgent.setAgentName(clusterWatcher.getAgentName());
                    dbAgent.setControllerId(controllerId);
                    dbAgent.setDisabled(false);
                    dbAgent.setIsWatcher(true);
                    dbAgent.setOsId(0L);
                    dbAgent.setStartedAt(null);
                    dbAgent.setUri(clusterWatcher.getUrl());
                    dbAgent.setVersion(null);
                    agentDBLayer.saveAgent(dbAgent);
                }
                if (updateAgentRequired) {
                    agentRefs.add(JAgentRef.of(AgentPath.of(clusterWatcher.getAgentId()), Uri.of(clusterWatcher.getUrl())));
                }
            }
            
            
                
                
            instances = instances.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!instances.isEmpty()) {
                // appointClusterNodes is called in Proxy when coupled with controller if cluster TYPE:Empty
                ProxiesEdit.update(instances);
            }
            if (clusterUriChanged || controllerUpdateRequired) {
                try {
                    ControllerResourceModifyClusterImpl.appointNodes(controllerId, agentDBLayer, accessToken, getJocError());
                } catch (JocBadRequestException e) {
                }
            }
            
            if (!agentRefs.isEmpty()) {
                final String cId = controllerId;
                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(agentRefs).map(JUpdateItemOperation::addOrChangeSimple)).thenAccept(
                        e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), cId));
            }
            
            if (firstController) { // GUI needs permissions directly for the first controller(s)
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getJobschedulerUser().getSosShiroCurrentUser()
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

    @Override
    public JOCDefaultResponse deleteController(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
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
    
    private Controller testConnection(URI controllerURI, String controllerId, URI otherControllerURI) throws ControllerInvalidResponseDataException {
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
        }
        if (answer != null) {
            if (!controllerId.isEmpty() && !controllerId.equals(answer.getId())) {
                if (otherControllerURI != null) {
                    throw new ControllerInvalidResponseDataException(String.format(
                            "The cluster members must have the same Controller Id: %1$s -> %2$s, %3$s -> %4$s", otherControllerURI.toString(),
                            controllerId, controllerURI, answer.getId()));
                } else {
                    throw new ControllerInvalidResponseDataException(String.format(
                            "Connection was successful but controllerId '%s' of URL '%s' is not the expected controllerId '%s'", answer.getId(),
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
