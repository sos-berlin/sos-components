package com.sos.joc.jobscheduler.impl;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.auth.rest.SOSPermissionsCreator;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpitMasters;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.model.command.Overview;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyJobSchedulerAudit;
import com.sos.joc.classes.jobscheduler.MasterAnswer;
import com.sos.joc.classes.jobscheduler.MasterCallable;
import com.sos.joc.classes.jobscheduler.States;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.exceptions.UnknownJobSchedulerMasterException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerEditResource;
import com.sos.joc.model.jobscheduler.ConnectionStateText;
import com.sos.joc.model.jobscheduler.JobScheduler200;
import com.sos.joc.model.jobscheduler.Master;
import com.sos.joc.model.jobscheduler.RegisterParameter;
import com.sos.joc.model.jobscheduler.RegisterParameters;
import com.sos.joc.model.jobscheduler.Role;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;

@Path("jobscheduler")
public class JobSchedulerEditResourceImpl extends JOCResourceImpl implements IJobSchedulerEditResource {

    private static final String API_CALL_REGISTER = "./jobscheduler/register";
    private static final String API_CALL_DELETE = "./jobscheduler/cleanup";
    private static final String API_CALL_TEST = "./jobscheduler/test";

    @Override
    public JOCDefaultResponse storeJobscheduler(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            JsonValidator.validateFailFast(filterBytes, RegisterParameters.class);
            RegisterParameters jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, RegisterParameters.class);
            
            checkRequiredParameter("masters", jobSchedulerBody.getMasters());
            checkRequiredComment(jobSchedulerBody.getAuditLog());
            String jobschedulerId = null;
            int index = 0;
            Set<Long> ids = new HashSet<Long>();
            Set<URI> uris = new HashSet<URI>();
            for (RegisterParameter master : jobSchedulerBody.getMasters()) {
                checkRequiredParameter("url", master.getUrl());
                checkRequiredParameter("role", master.getRole());
                
                if (index == 1 && master.getUrl().equals(jobSchedulerBody.getMasters().get(0).getUrl())) {
                    throw new JobSchedulerBadRequestException("The cluster members must have the different URLs"); 
                }

                Master jobScheduler = testConnection(master.getUrl());
                if (jobScheduler.getConnectionState().get_text() == ConnectionStateText.unreachable) {
                    throw new JobSchedulerConnectionRefusedException(master.getUrl().toString());
                }
                
                if (jobschedulerId == null) {
                    jobschedulerId = jobScheduler.getJobschedulerId();
                }
                if (index == 1 && !jobschedulerId.equals(jobScheduler.getJobschedulerId())) {
                    throw new JobSchedulerInvalidResponseDataException(String.format(
                            "The cluster members must have the same JobScheduler Id: %1$s -> %2$s, %3$s -> %4$s", jobSchedulerBody.getMasters().get(0)
                                    .getUrl().toString(), jobschedulerId, master.getUrl().toString(), jobScheduler.getJobschedulerId()));
                }
                if (master.getId() == null) {
                    master.setId(0L); 
                }
                ids.add(master.getId());
                uris.add(master.getUrl());
                index++;
            }
            //TODO permission for editing JobScheduler instance
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_REGISTER, jobSchedulerBody, accessToken, "", getPermissonsJocCockpit(jobschedulerId,
                    accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REGISTER);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            DBItemInventoryInstance instance = null;
            DBItemOperatingSystem osSystem = null;
            
            boolean firstMaster = instanceDBLayer.isEmpty();
            
            ModifyJobSchedulerAudit jobSchedulerAudit = new ModifyJobSchedulerAudit(jobSchedulerBody);
            logAuditMessage(jobSchedulerAudit);

            if (jobSchedulerBody.getMasters().size() == 1) {
                RegisterParameter master = jobSchedulerBody.getMasters().get(0);
                
                if (master.getId() == 0L) { //new
                    if (!instanceDBLayer.instanceAlreadyExists(uris, ids)) {
                        storeNewInventoryInstance(instanceDBLayer, osDBLayer, master, jobschedulerId);
                    }
                } else {
                    //update instance and delete possibly other instance with same (old) jobschedulerId
                    instance = instanceDBLayer.getInventoryInstance(master.getId());
                    if (instance == null) {
                        throw new UnknownJobSchedulerMasterException(getUnknownJobSchedulerMasterMessage(master.getId()));
                    }
                    DBItemInventoryInstance otherClusterMember = instanceDBLayer.getOtherClusterMember(instance.getSchedulerId(), instance.getId());
                    if (otherClusterMember != null ) {
                        ids.add(otherClusterMember.getId());
                    }
                    instanceDBLayer.instanceAlreadyExists(uris, ids);
                    instanceDBLayer.deleteInstance(otherClusterMember);
                    
                    instance = setInventoryInstance(instance, master, jobschedulerId);
                    osSystem = osDBLayer.getInventoryOperatingSystem(instance.getOsId());
                    MasterAnswer jobschedulerAnswer = new MasterCallable(instance, osSystem, accessToken).call();
                    
                    Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
                    jobschedulerAnswer.setOsId(osId);
                    
                    instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
                    
                    //delete (old) OS if unused
                    if (otherClusterMember != null && !instanceDBLayer.isOperatingSystemUsed(otherClusterMember.getOsId())) {
                        osDBLayer.deleteOSItem(osDBLayer.getInventoryOperatingSystem(otherClusterMember.getOsId()));
                    }
                }
            } else {
                instanceDBLayer.instanceAlreadyExists(uris, ids);
                //special case : Urls have changed vice versa inside a cluster; avoid constraint violation
                List<DBItemInventoryInstance> instances = new ArrayList<DBItemInventoryInstance>();
                index = 0;
                boolean internalUrlChangeInCluster = false;
                for (RegisterParameter master : jobSchedulerBody.getMasters()) {
                    if (master.getId() == 0L) { //new (standalone -> cluster)
                        instances.add(null);
                        storeNewInventoryInstance(instanceDBLayer, osDBLayer, master, jobschedulerId);
                    } else {
                        instance = instanceDBLayer.getInventoryInstance(master.getId());
                        if (instance == null) {
                            throw new UnknownJobSchedulerMasterException(getUnknownJobSchedulerMasterMessage(master.getId()));
                        }
                        instances.add(instance);
                        if (instance.getUri().equals(jobSchedulerBody.getMasters().get(index == 0 ? 1 : 0).getUrl().toString().toLowerCase())) {
                            internalUrlChangeInCluster = true; 
                        }
                    }
                    index++;
                }
                index = 0;
                for (DBItemInventoryInstance inst : instances) {
                    if (inst != null) {
                        RegisterParameter master = jobSchedulerBody.getMasters().get(index); 
                        instance = setInventoryInstance(inst, master, jobschedulerId);
                        if (internalUrlChangeInCluster) {
                            instance.setId(jobSchedulerBody.getMasters().get(index == 0 ? 1 : 0).getId());
                        }
                        osSystem = osDBLayer.getInventoryOperatingSystem(instance.getOsId());
                        
                        MasterAnswer jobschedulerAnswer = new MasterCallable(instance, osSystem, accessToken).call();
                        
                        Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
                        jobschedulerAnswer.setOsId(osId);
                        
                        instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
                    }
                    index++;
                }
            }
            
            storeAuditLogEntry(jobSchedulerAudit);
            
            if (firstMaster) { //GUI needs permissions directly for the first master(s)
                SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(getJobschedulerUser().getSosShiroCurrentUser());
                SOSPermissionJocCockpitMasters sosPermissionMasters = sosPermissionsCreator.createJocCockpitPermissionMasterObjectList(accessToken);
                return JOCDefaultResponse.responseStatus200(sosPermissionMasters);
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
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_DELETE, jobSchedulerBody, accessToken, "",
                    getPermissonsJocCockpit(jobSchedulerBody.getJobschedulerId(), accessToken).getJobschedulerMaster().getAdministration()
                            .isRemoveOldInstances());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("jobSchedulerId", jobSchedulerBody.getJobschedulerId());
            checkRequiredComment(jobSchedulerBody.getAuditLog());
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            
            List<DBItemInventoryInstance> instances = instanceDBLayer.getInventoryInstancesBySchedulerId(jobSchedulerBody.getJobschedulerId());
            if (instances != null) {
               for (DBItemInventoryInstance instance : instances) {
                   instanceDBLayer.deleteInstance(instance);
                   if (!instanceDBLayer.isOperatingSystemUsed(instance.getOsId())) {
                       osDBLayer.deleteOSItem(osDBLayer.getInventoryOperatingSystem(instance.getOsId()));
                   }
                   //TODO some other tables should maybe deleted !!!
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
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            String jobschedulerId = "";
            if (jobSchedulerBody.getJobschedulerId() != null) {
                jobschedulerId = jobSchedulerBody.getJobschedulerId();
            }
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_TEST, jobSchedulerBody, accessToken, "", getPermissonsJocCockpit(
                    jobschedulerId, accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("url", jobSchedulerBody.getUrl());
            
            Master jobScheduler = testConnection(jobSchedulerBody.getUrl());
            
            JobScheduler200 entity = new JobScheduler200();
            entity.setJobscheduler(jobScheduler);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private Master testConnection(URI jobschedulerURI) throws JobSchedulerInvalidResponseDataException {
        Master jobScheduler = new Master();
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
            jobScheduler.setJobschedulerId(answer.getId());
            jobScheduler.setConnectionState(States.getConnectionState(ConnectionStateText.established));
        } else {
            jobScheduler.setConnectionState(States.getConnectionState(ConnectionStateText.unreachable));
        }
        return jobScheduler;
    }
    
    private void storeNewInventoryInstance(InventoryInstancesDBLayer instanceDBLayer, InventoryOperatingSystemsDBLayer osDBLayer,
            RegisterParameter master, String jobschedulerId) throws DBInvalidDataException, DBConnectionRefusedException,
            JocObjectAlreadyExistException, JobSchedulerInvalidResponseDataException {
        DBItemInventoryInstance instance = setInventoryInstance(null, master, jobschedulerId);
        Long newId = instanceDBLayer.saveInstance(instance);
        instance.setId(newId);

        MasterAnswer jobschedulerAnswer = new MasterCallable(instance, null, getAccessToken()).call();

        Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
        jobschedulerAnswer.setOsId(osId);

        if (jobschedulerAnswer.dbInstanceIsChanged()) {
            instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
        }
    }
    
    private DBItemInventoryInstance setInventoryInstance(DBItemInventoryInstance instance, RegisterParameter master, String jobschedulerId) {
        if (instance == null) {
            instance = new DBItemInventoryInstance();
            instance.setId(null);
            instance.setOsId(0L);
            instance.setStartedAt(null);
            instance.setTimezone(null);
            instance.setVersion(null);
        }
        Role role = master.getRole();
        instance.setSchedulerId(jobschedulerId);
        instance.setUri(master.getUrl().toString());
        if (master.getTitle() == null || master.getTitle().isEmpty()) {
            instance.setTitle(role.value());
        } else {
            instance.setTitle(master.getTitle());
        }
        instance.setIsPrimaryMaster(role != Role.BACKUP);
        instance.setIsCluster(role != Role.STANDALONE);
        if (instance.getIsCluster()) {
            if (master.getClusterUrl() != null) {
                instance.setClusterUri(master.getClusterUrl().toString());
            } else {
                instance.setClusterUri(master.getUrl().toString());
            }
        } else {
            instance.setClusterUri(null);
        }
        return instance;
    }
    
    private String getUnknownJobSchedulerMasterMessage(Long id) {
        return String.format("JobScheduler instance (id:%1$d) couldn't be found in table %2$s", id,
                DBLayer.TABLE_INVENTORY_INSTANCES);
    }

}
