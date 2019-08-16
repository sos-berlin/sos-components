package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCPreferences;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.jobscheduler.JobSchedulerAnswer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceIds;
import com.sos.joc.model.jobscheduler.JobScheduler;
import com.sos.joc.model.jobscheduler.JobSchedulerIds;
import com.sos.joc.model.jobscheduler.Masters;

@Path("jobscheduler")
public class JobSchedulerResourceIdsImpl extends JOCResourceImpl implements IJobSchedulerResourceIds {

    private static final String API_CALL_IDS = "./jobscheduler/ids";
    private static final String API_CALL_MASTERS = "./jobscheduler/masters";

    @Override
    public JOCDefaultResponse postJobschedulerIds(String accessToken) {
        SOSHibernateSession connection = null;

        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_IDS, null, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            SOSShiroCurrentUser shiroUser = jobschedulerUser.getSosShiroCurrentUser();
            JOCPreferences jocPreferences = new JOCPreferences(shiroUser.getUsername());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_IDS);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
            List<String> schedulerIds = dbLayer.getJobSchedulerIds();
            Set<String> jobSchedulerIds = new HashSet<>();
            String first = null;
            if (schedulerIds != null && !schedulerIds.isEmpty()) {
                for (String schedulerId : schedulerIds) {
                    if (schedulerId == null || schedulerId.isEmpty()) {
                        continue;
                    }
                    if (!getPermissonsJocCockpit(schedulerId, accessToken).getJobschedulerMasterCluster().getView().isStatus()
                            && !getPermissonsJocCockpit(schedulerId, accessToken).getJobschedulerMaster().getView().isStatus()) {
                        continue;
                    }
                    jobSchedulerIds.add(schedulerId);
                    if (first == null) {
                        first = schedulerId;
                    }
                }
                if (jobSchedulerIds.isEmpty()) {
                    return accessDeniedResponse();
                }
            } else {
                // throw new DBMissingDataException("No JobSchedulers found in DB!");
            }
            String selectedInstanceSchedulerId = jocPreferences.get(WebserviceConstants.SELECTED_INSTANCE, first);

            if (!jobSchedulerIds.contains(selectedInstanceSchedulerId)) {
                if (first != null) {
                    selectedInstanceSchedulerId = first;
                    jocPreferences.put(WebserviceConstants.SELECTED_INSTANCE, first);
                }
            }

            JobSchedulerIds entity = new JobSchedulerIds();
            entity.getJobschedulerIds().addAll(jobSchedulerIds);
            entity.setSelected(selectedInstanceSchedulerId);
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

    @Override
    public JOCDefaultResponse postJobschedulerInstancess(String accessToken) throws Exception {
        SOSHibernateSession connection = null;

        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_MASTERS, null, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_MASTERS);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
            List<DBItemInventoryInstance> schedulerInstances = dbLayer.getInventoryInstancesBySchedulerId(null);
            List<JobScheduler> masters = new ArrayList<>();
            if (schedulerInstances != null) {
                for (DBItemInventoryInstance schedulerInstance : schedulerInstances) {
                    JobScheduler master = new JobScheduler();
                    master.setId(schedulerInstance.getId());
                    master.setClusterType(JobSchedulerAnswer.getClusterMemberType(schedulerInstance));
                    master.setJobschedulerId(schedulerInstance.getSchedulerId());
                    master.setSurveyDate(schedulerInstance.getModified());
                    master.setUrl(schedulerInstance.getUri());
                    master.setVersion(schedulerInstance.getVersion());
                    masters.add(master);
                }
            }
            
            Masters entity = new Masters();
            entity.setMasters(masters);
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

}
