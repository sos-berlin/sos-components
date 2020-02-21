package com.sos.joc.jobscheduler.impl;

import java.net.URI;
import java.time.Instant;
import java.util.Date;

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
import com.sos.joc.classes.jobscheduler.JobSchedulerAnswer;
import com.sos.joc.classes.jobscheduler.JobSchedulerCallable;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.exceptions.UnknownJobSchedulerMasterException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerEditResource;
import com.sos.joc.model.jobscheduler.JobScheduler;
import com.sos.joc.model.jobscheduler.JobScheduler200;
import com.sos.joc.model.jobscheduler.JobSchedulerStateText;
import com.sos.joc.model.jobscheduler.RegisterParameter;
import com.sos.joc.model.jobscheduler.RegisterParameters;
import com.sos.joc.model.jobscheduler.Role;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerEditResourceImpl extends JOCResourceImpl implements IJobSchedulerEditResource {

    private static final String API_CALL_REGISTER = "./jobscheduler/register";
    private static final String API_CALL_TEST = "./jobscheduler/test";

    @Override
    public JOCDefaultResponse storeJobscheduler(String accessToken, RegisterParameters jobSchedulerBody) {
        SOSHibernateSession connection = null;
        try {
            //TODO permission for editing JobScheduler instance
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_REGISTER, jobSchedulerBody, accessToken, "", getPermissonsJocCockpit(jobSchedulerBody
                    .getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            //checkRequiredParameter("jobSchedulerId", jobSchedulerBody.getJobschedulerId());
            checkRequiredParameter("url", jobSchedulerBody.getMasters());
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REGISTER);
            InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            DBItemInventoryInstance instance = null;
            DBItemOperatingSystem osSystem = null;
            boolean updateInstanceRequired = true;
            boolean newMasterInstances = false;
            
            ModifyJobSchedulerAudit jobSchedulerAudit = new ModifyJobSchedulerAudit(jobSchedulerBody);
            logAuditMessage(jobSchedulerAudit);
            
            for (RegisterParameter master : jobSchedulerBody.getMasters()) {
                checkRequiredParameter("url", master.getUrl().toString());
                checkRequiredParameter("role", master.getRole());
                
                JobScheduler jobScheduler = testConnection(jobSchedulerBody.getJobschedulerId(), master.getUrl());
                if (jobScheduler.getState().get_text() == JobSchedulerStateText.UNREACHABLE) {
                    throw new JobSchedulerConnectionRefusedException(master.getUrl().toString());
                }
                
                DBItemInventoryInstance constraintInstance = instanceDBLayer.getInventoryInstanceByURI(jobScheduler.getJobschedulerId(),
                        master.getUrl().toString());
                String constraintErrMessage = String.format("JobScheduler instance (jobschedulerId:%1$s, url:%2$s) already exists in table %3$s",
                        jobScheduler.getJobschedulerId(), master.getUrl(), DBLayer.TABLE_INVENTORY_INSTANCES);
                
                Role role = master.getRole();
                boolean newMasterInstance = master.getId() == null || master.getId() == 0L;
                newMasterInstances = newMasterInstances || newMasterInstance;
                if (newMasterInstance) {
                    if (constraintInstance != null) {
                        throw new JocObjectAlreadyExistException(constraintErrMessage);
                    }
                    instance = new DBItemInventoryInstance();
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
                    instance.setId(null);
                    instance.setOsId(0L);
                    instance.setSchedulerId(jobScheduler.getJobschedulerId());
                    instance.setStartedAt(null);
                    instance.setTimezone(null);
                    instance.setUri(master.getUrl().toString());
                    instance.setVersion(null);
                    Long newId = instanceDBLayer.saveInstance(instance);
                    instance.setId(newId);
                    updateInstanceRequired = false;
                } else {
                    instance = instanceDBLayer.getInventoryInstance(master.getId());
                    if (instance == null) {
                        String errMessage = String.format("JobScheduler instance (id:%1$s) couldn't be found in table %2$s", master.getId(),
                                DBLayer.TABLE_INVENTORY_INSTANCES);
                        throw new UnknownJobSchedulerMasterException(errMessage);
                    } else {
//                        if (jobSchedulerBody.getJobschedulerId().equals(instance.getSchedulerId()) && jobSchedulerBody.getUrl().toString().equalsIgnoreCase(instance
//                                .getUri())) {
//                            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
//                        }
                        if (constraintInstance != null && constraintInstance.getId() != master.getId()) {
                            throw new JocObjectAlreadyExistException(constraintErrMessage);
                        }
                        instance.setSchedulerId(jobScheduler.getJobschedulerId());
                        instance.setUri(master.getUrl().toString());
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
                        osSystem = osDBLayer.getInventoryOperatingSystem(instance.getOsId());
                    }
                }
                
                JobSchedulerAnswer jobschedulerAnswer = new JobSchedulerCallable(instance, osSystem, accessToken).call();
                
                Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
                jobschedulerAnswer.setOsId(osId);
                
                if (jobschedulerAnswer.dbInstanceIsChanged() || updateInstanceRequired) {
                    instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
                }
            }

            storeAuditLogEntry(jobSchedulerAudit);
            
            if (newMasterInstances) {
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
    public JOCDefaultResponse testConnectionJobscheduler(String accessToken, UrlParameter jobSchedulerBody) {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_TEST, jobSchedulerBody, accessToken, "", getPermissonsJocCockpit(
                    jobSchedulerBody.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            //checkRequiredParameter("jobSchedulerId", jobSchedulerBody.getJobschedulerId());
            checkRequiredParameter("url", jobSchedulerBody.getUrl());
            checkRequiredParameter("url", jobSchedulerBody.getUrl().toString());
            
            JobScheduler jobScheduler = testConnection(jobSchedulerBody.getJobschedulerId(), jobSchedulerBody.getUrl());
            
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
    
    private JobScheduler testConnection(String jobschedulerId, URI jobschedulerURI) throws JobSchedulerInvalidResponseDataException {
        JobScheduler jobScheduler = new JobScheduler();
        jobScheduler.setJobschedulerId(jobschedulerId);
        jobScheduler.setUrl(jobschedulerURI.toString());
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
            if (jobschedulerId.isEmpty()) {
                jobScheduler.setJobschedulerId(answer.getId());
            }
            if (!jobScheduler.getJobschedulerId().equals(answer.getId())) {
                throw new JobSchedulerInvalidResponseDataException("unexpected JobSchedulerId " + answer.getId());
            }
            jobScheduler.setState(JobSchedulerAnswer.getJobSchedulerState("running"));
        } else {
            jobScheduler.setState(JobSchedulerAnswer.getJobSchedulerState("unreachable"));
        }
        return jobScheduler;
    }

}
