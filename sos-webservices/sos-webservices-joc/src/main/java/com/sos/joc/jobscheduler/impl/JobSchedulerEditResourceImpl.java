package com.sos.joc.jobscheduler.impl;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.auth.rest.SOSPermissionsCreator;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitControllers;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.joc.Globals;
import com.sos.joc.agents.impl.AgentsResourceStoreImpl;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.ModifyJobSchedulerAudit;
import com.sos.joc.classes.jobscheduler.ControllerAnswer;
import com.sos.joc.classes.jobscheduler.ControllerCallable;
import com.sos.joc.classes.jobscheduler.States;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.ProxiesEdit;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryAgentName;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerEditResource;
import com.sos.joc.model.agent.Agent;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.jobscheduler.ConnectionStateText;
import com.sos.joc.model.jobscheduler.Controller;
import com.sos.joc.model.jobscheduler.JobScheduler200;
import com.sos.joc.model.jobscheduler.RegisterParameter;
import com.sos.joc.model.jobscheduler.RegisterParameters;
import com.sos.joc.model.jobscheduler.Role;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.joc.model.security.SecurityConfigurationMaster;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentName;
import js7.data.agent.AgentRef;
import js7.proxy.javaapi.data.agent.JAgentRef;

@Path("controller")
public class JobSchedulerEditResourceImpl extends JOCResourceImpl implements IJobSchedulerEditResource {

    private static final String API_CALL_REGISTER = "./controller/register";
    private static final String API_CALL_DELETE = "./controller/cleanup";
    private static final String API_CALL_TEST = "./controller/test";

    @Override
    public JOCDefaultResponse storeJobscheduler(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REGISTER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RegisterParameters.class);
            RegisterParameters jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, RegisterParameters.class);
            
            checkRequiredComment(jobSchedulerBody.getAuditLog());
            String controllerId = jobSchedulerBody.getControllerId();
            if (jobSchedulerBody.getAgents() == null) {
                jobSchedulerBody.setAgents(Collections.emptyList());
            }
            boolean requestWithEmptyControllerId = controllerId.isEmpty();
            int index = 0;
            for (RegisterParameter controller : jobSchedulerBody.getControllers()) {
                
                if (index == 1 && controller.getUrl().equals(jobSchedulerBody.getControllers().get(0).getUrl())) {
                    throw new JobSchedulerBadRequestException("The cluster members must have the different URLs"); 
                }
                if (index == 1 && controller.getRole().equals(jobSchedulerBody.getControllers().get(0).getRole())) {
                    throw new JobSchedulerBadRequestException("The members of a Controller Cluster must have different roles."); 
                }
                if (index == 1 && jobSchedulerBody.getControllers().stream().anyMatch(c -> Role.STANDALONE.equals(c.getRole()))) {
                    throw new JobSchedulerBadRequestException("The members of a Controller Cluster must have roles PRIMARY and BACKUP."); 
                }
                if (index == 1 && (jobSchedulerBody.getAgents().isEmpty() || !jobSchedulerBody.getAgents()
                        .stream().anyMatch(Agent::getIsClusterWatcher))) {
                    throw new JobSchedulerBadRequestException("A Controller Cluster needs at least one Agent Cluster Watcher.");
                }
                if (index == 1 && !jobSchedulerBody.getAgents().isEmpty() && jobSchedulerBody.getAgents()
                        .stream().filter(Agent::getIsClusterWatcher).count() > 0) {
                    throw new JobSchedulerBadRequestException("Only one Agent may be a Cluster Watcher.");
                }

                URI otherUri = index == 0 ? null : jobSchedulerBody.getControllers().get(0).getUrl();
                Controller jobScheduler = testConnection(controller.getUrl(), controllerId, otherUri);
                if (jobScheduler.getConnectionState().get_text() == ConnectionStateText.unreachable) {
                    throw new JobSchedulerConnectionRefusedException(controller.getUrl().toString());
                }
                
                controllerId = jobScheduler.getControllerId();
                index++;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REGISTER);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            if (!requestWithEmptyControllerId) { // try update controllers with given controllerId
                Integer securityLevel = instanceDBLayer.getSecurityLevel(controllerId);
                if (securityLevel != null && securityLevel != Globals.getJocSecurityLevel().intValue()) {
                    throw new JocObjectAlreadyExistException(String.format("Controller with Id '%s' is already configured with a different security level '%s'.",
                            controllerId, JocSecurityLevel.fromValue(securityLevel)));
                }
            }
            
            Set<String> agentIds = jobSchedulerBody.getAgents().stream().map(Agent::getAgentId).collect(Collectors.toSet());
            
            for (String agentId : agentIds) {
                CheckJavaVariableName.test("Agent ID", agentId);
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getPermissonsJocCockpit(controllerId, accessToken).getJS7Controller()
                    .getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryJSInstance> instances = new ArrayList<>();
            DBItemInventoryOperatingSystem osSystem = null;
            
            agentDBLayer.agentIdAlreadyExists(agentIds, controllerId);
            
            boolean firstController = instanceDBLayer.isEmpty();
            boolean clusterUriChanged = false;
            
            ModifyJobSchedulerAudit jobSchedulerAudit = new ModifyJobSchedulerAudit(jobSchedulerBody);
            logAuditMessage(jobSchedulerAudit);

            // sorted by isPrimary first
            List<DBItemInventoryJSInstance> dbControllers = instanceDBLayer.getInventoryInstancesByControllerId(controllerId);
            
            if (requestWithEmptyControllerId) { // try insert of new Controllers
                if (dbControllers != null && !dbControllers.isEmpty()) {
                    throw new JocObjectAlreadyExistException(String.format("Controller(s) with id '%s' already exists", controllerId));
                }
                if (jobSchedulerBody.getControllers().size() == 1) {  // standalone
                    RegisterParameter controller = jobSchedulerBody.getControllers().get(0);
                    controller.setRole(Role.STANDALONE);
                    instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                } else {
                    for (RegisterParameter controller : jobSchedulerBody.getControllers()) {
                        instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                    }
                }
                
            } else { // try update controllers with given controllerId
                if (jobSchedulerBody.getControllers().size() == 1) {  // standalone from request
                    RegisterParameter controller = jobSchedulerBody.getControllers().get(0);
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
                        for (RegisterParameter controller : jobSchedulerBody.getControllers()) {
                            instances.add(storeNewInventoryInstance(instanceDBLayer, osDBLayer, controller, controllerId));
                        }
                    } else {
                        DBItemInventoryJSInstance instance = null;
                        boolean uriChanged = false;
                        for (RegisterParameter controller : jobSchedulerBody.getControllers()) {
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
            
            //sort Agents from request
            //jobSchedulerBody.getAgents().stream().sorted(Comparator.comparing(Agent::getAgentId));
            Map<String, Agent> agentMap = jobSchedulerBody.getAgents().stream().collect(Collectors.toMap(Agent::getAgentId, Function.identity()));
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Arrays.asList(controllerId));
            Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(agentIds);
            boolean watcherUpdateRequired = false;
            if (dbAgents != null && !dbAgents.isEmpty()) {
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    Agent agent = agentMap.remove(dbAgent.getAgentId());
                    if (agent == null) {
                        // throw something?
                        continue;
                    }
                    boolean dbUpdateRequired = false;
                    if (dbAgent.getDisabled() != agent.getDisabled()) {
                        dbAgent.setDisabled(agent.getDisabled());
                        dbUpdateRequired = true;
                    }
                    if (dbAgent.getIsWatcher() != agent.getIsClusterWatcher()) {
                        dbAgent.setIsWatcher(agent.getIsClusterWatcher());
                        dbUpdateRequired = true;
                        watcherUpdateRequired = true;
                    }
                    if (!dbAgent.getAgentName().equals(agent.getAgentName())) {
                        dbAgent.setIsWatcher(agent.getIsClusterWatcher());
                        dbUpdateRequired = true;
                    }
                    if (!dbAgent.getUri().equals(agent.getUrl())) {
                        dbAgent.setUri(agent.getUrl());
                        dbUpdateRequired = true;
                    }
                    if (dbUpdateRequired) {
                        agentDBLayer.updateAgent(dbAgent);
                    }
                    AgentsResourceStoreImpl.updateAliases(connection, agent, allAliases.get(agent.getAgentId()));
                }
            }
            
            for (Agent agent : agentMap.values()) {
                DBItemInventoryAgentInstance dbAgent = new DBItemInventoryAgentInstance();
                dbAgent.setId(null);
                dbAgent.setAgentId(agent.getAgentId());
                dbAgent.setAgentName(agent.getAgentName());
                dbAgent.setControllerId(controllerId);
                dbAgent.setDisabled(agent.getDisabled());
                dbAgent.setIsWatcher(agent.getIsClusterWatcher());
                if (agent.getIsClusterWatcher()) {
                    watcherUpdateRequired = true;
                }
                dbAgent.setOsId(0L);
                dbAgent.setStartedAt(null);
                dbAgent.setUri(agent.getUrl());
                dbAgent.setVersion(null);
                agentDBLayer.saveAgent(dbAgent);
                AgentsResourceStoreImpl.updateAliases(connection, agent, allAliases.get(agent.getAgentId()));
            }
            
            instances = instances.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!instances.isEmpty()) {
                // appointClusterNodes is called in Proxy when coupled with controller if cluster TYPE:Empty
                ProxiesEdit.update(instances);
            }
            if (clusterUriChanged || watcherUpdateRequired) {
                try {
                    JobSchedulerResourceModifyJobSchedulerClusterImpl.appointNodes(controllerId, agentDBLayer, getJocError());
                } catch (JobSchedulerBadRequestException e) {
                }
            }
            
            final String cId = controllerId;
            List<DBItemInventoryAgentInstance> dbAvailableAgents = agentDBLayer.getAgentsByControllerIds(Arrays.asList(controllerId), false, true);
            if (dbAvailableAgents != null) {
                List<JAgentRef> agentRefs = dbAvailableAgents.stream().map(a -> JAgentRef.apply(AgentRef.apply(AgentName.of(a.getAgentId()), Uri.of(a
                        .getUri())))).collect(Collectors.toList());
                ControllerApi.of(controllerId).updateAgentRefs(agentRefs).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getJocError(),
                        cId));
            }
            
            storeAuditLogEntry(jobSchedulerAudit);
            
            if (firstController) { //GUI needs permissions directly for the first controller(s)
                List<SecurityConfigurationMaster> listOfMasters = new  ArrayList<SecurityConfigurationMaster>();
                SecurityConfigurationMaster securityConfigurationMaster = new SecurityConfigurationMaster();
                securityConfigurationMaster.setMaster(controllerId);
                listOfMasters.add(securityConfigurationMaster);
                
                SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(getJobschedulerUser().getSosShiroCurrentUser());
                SOSPermissionJocCockpitControllers sosPermissionControllers = sosPermissionsCreator.createJocCockpitPermissionControllerObjectList(accessToken,listOfMasters);
                return JOCDefaultResponse.responseStatus200(sosPermissionControllers);
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
    public JOCDefaultResponse deleteJobscheduler(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getPermissonsJocCockpit(jobSchedulerBody.getControllerId(), accessToken)
                    .getJS7Controller().getAdministration().isRemoveOldInstances());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("controllerId", jobSchedulerBody.getControllerId());
            checkRequiredComment(jobSchedulerBody.getAuditLog());
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            
            List<DBItemInventoryJSInstance> instances = instanceDBLayer.getInventoryInstancesByControllerId(jobSchedulerBody.getControllerId());
            if (instances != null) {
               for (DBItemInventoryJSInstance instance : instances) {
                   instanceDBLayer.deleteInstance(instance);
                   if (!instanceDBLayer.isOperatingSystemUsed(instance.getOsId())) {
                       osDBLayer.deleteOSItem(osDBLayer.getInventoryOperatingSystem(instance.getOsId()));
                   }
                   //TODO some other tables should maybe deleted !!!
               }
               ProxiesEdit.remove(jobSchedulerBody.getControllerId());
            }
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIds(Arrays.asList(jobSchedulerBody.getControllerId()));
            if (dbAgents != null) {
                Map<String, Set<DBItemInventoryAgentName>> allAliases = agentDBLayer.getAgentNameAliases(dbAgents.stream().map(
                        DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toSet()));
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    agentDBLayer.deleteInstance(dbAgent);
                    Set<DBItemInventoryAgentName> dbAliase = allAliases.get(dbAgent.getAgentId());
                    for (DBItemInventoryAgentName item : dbAliase) {
                        connection.delete(item);
                    }
                }
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
    public JOCDefaultResponse testConnectionJobscheduler(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_TEST, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            String controllerId = jobSchedulerBody.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getPermissonsJocCockpit(controllerId, accessToken).getJS7Controller()
                    .getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("url", jobSchedulerBody.getUrl());
            
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
    
    private Controller testConnection(URI jobschedulerURI, String controllerId, URI otherJobschedulerURI) throws JobSchedulerInvalidResponseDataException {
        Controller jobScheduler = new Controller();
        jobScheduler.setUrl(jobschedulerURI.toString());
        jobScheduler.setIsCoupled(null);
        Overview answer = null;
        try {
            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(jobschedulerURI, getAccessToken());
            jocJsonCommand.setUriBuilderForOverview();
            answer = jocJsonCommand.getJsonObjectFromGet(Overview.class);
        } catch (JobSchedulerInvalidResponseDataException e) {
            throw e;
        } catch (JocException e) {
        }
        if (answer != null) {
            if (!controllerId.isEmpty() && !controllerId.equals(answer.getId())) {
                if (otherJobschedulerURI != null) {
                    throw new JobSchedulerInvalidResponseDataException(String.format(
                            "The cluster members must have the same Controller Id: %1$s -> %2$s, %3$s -> %4$s", otherJobschedulerURI.toString(),
                            controllerId, jobschedulerURI, answer.getId()));
                } else {
                    throw new JobSchedulerInvalidResponseDataException(String.format(
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
            RegisterParameter controller, String jobschedulerId) throws DBInvalidDataException, DBConnectionRefusedException,
            JocObjectAlreadyExistException, JobSchedulerInvalidResponseDataException {
        DBItemInventoryJSInstance instance = setInventoryInstance(null, controller, jobschedulerId);
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
    
    private DBItemInventoryJSInstance setInventoryInstance(DBItemInventoryJSInstance instance, RegisterParameter controller, String jobschedulerId) {
        if (instance == null) {
            instance = new DBItemInventoryJSInstance();
            instance.setId(null);
            instance.setOsId(0L);
            instance.setStartedAt(null);
            instance.setVersion(null);
        }
        Role role = controller.getRole();
        instance.setSecurityLevel(Globals.getJocSecurityLevel().intValue());
        instance.setControllerId(jobschedulerId);
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
