package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.command.Overview;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.controller.ControllerAnswer;
import com.sos.joc.classes.controller.ControllerCallable;
import com.sos.joc.classes.controller.States;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.ProxiesEdit;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.controller.resource.IControllerEditResource;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
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
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.joc.impl.StateImpl;
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

import jakarta.ws.rs.Path;
import js7.proxy.javaapi.JControllerProxy;

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
            filterBytes = initLogging(API_CALL_REGISTER, filterBytes, accessToken);
            
            if (!StateImpl.isActive(API_CALL_REGISTER, null)) {
                throw new JocServiceException("Registering the Controllers is possible only in the active JOC node.");
            }
            
            JsonValidator.validateFailFast(filterBytes, RegisterParameters.class);
            RegisterParameters body = Globals.objectMapper.readValue(filterBytes, RegisterParameters.class);
            
            String controllerId = body.getControllerId();
            if (controllerId == null) {
                controllerId = ""; 
            }
            
            if (body.getControllers().size() == 2) {
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
            
            if (!requestWithEmptyControllerId) { // try update controllers with given controllerId
                Integer securityLevel = instanceDBLayer.getSecurityLevel(controllerId);
                if (securityLevel != null && securityLevel != Globals.getJocSecurityLevel().intValue()) {
                    throw new JocObjectAlreadyExistException(String.format(
                            "Controller with ID '%s' is already configured with a different security level '%s'.", controllerId, JocSecurityLevel
                                    .fromValue(securityLevel)));
                }
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(body.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            List<DBItemInventoryJSInstance> instances = new ArrayList<>();
            DBItemInventoryOperatingSystem osSystem = null;
                        
            boolean firstController = instanceDBLayer.isEmpty();
            
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
                                instance = setInventoryInstance(dbControllers.get(0), controller, controllerId);
                            } else {
                                if (!uriChanged) {
                                    uriChanged = !dbControllers.get(1).getUri().equalsIgnoreCase(controller.getUrl().toString());
                                }
                                if (controller.getClusterUrl() == null) {
                                    controller.setClusterUrl(controller.getUrl());
                                }
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
            
                
            instances = instances.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!instances.isEmpty()) {
                ProxiesEdit.update(instances);
            }
            
            JControllerProxy proxy = null;
            
            if (dbControllers.size() == 2) {
                try {
                    proxy = Proxy.of(controllerId);
                    ClusterWatch.getInstance().appointNodes(controllerId, null, proxy, new JocInstancesDBLayer(connection), accessToken,
                            getJocError());
                } catch (JocBadRequestException e) {
                }
            }
            
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
    
    @Override
    public JOCDefaultResponse deleteController(String accessToken, byte[] filterBytes) {
        return unregisterController(accessToken, filterBytes); //alias for cleanup
    }

    @Override
    public JOCDefaultResponse unregisterController(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL_DELETE, filterBytes, accessToken);
            
            if (!StateImpl.isActive(API_CALL_DELETE, null)) {
                throw new JocServiceException("Deregistering the Controllers is possible only in the active JOC node.");
            }
            
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter controllerObj = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
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
            filterBytes = initLogging(API_CALL_TEST, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, TestConnect.class);
            TestConnect jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, TestConnect.class);
            
            String controllerId = jobSchedulerBody.getControllerId();
            if (controllerId == null) {
                controllerId = ""; 
            }
            
            if (!isUrl.test(jobSchedulerBody.getUrl())) {
                throw new JocBadRequestException("$.url: does not match the url pattern " + isUrlPattern);
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getControllers()
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
