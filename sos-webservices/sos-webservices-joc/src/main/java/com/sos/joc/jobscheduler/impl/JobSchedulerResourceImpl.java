package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.ControllerAnswer;
import com.sos.joc.classes.jobscheduler.ControllerCallable;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResource;
import com.sos.joc.model.jobscheduler.JobScheduler200;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;

@Path("jobscheduler")
public class JobSchedulerResourceImpl extends JOCResourceImpl implements IJobSchedulerResource {

    private static final String API_CALL = "./jobscheduler";

    @Override
    public JOCDefaultResponse postJobschedulerP(String accessToken, byte[] filterBytes) {
        return postJobscheduler(accessToken, filterBytes, true);
    }

    @Override
    public JOCDefaultResponse postJobscheduler(String accessToken, byte[] filterBytes) {
        return postJobscheduler(accessToken, filterBytes, false);
    }

    public JOCDefaultResponse postJobscheduler(String accessToken, byte[] filterBytes, boolean onlyDb) {
        SOSHibernateSession connection = null;
        try {
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter jobSchedulerBody = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            String apiCall = API_CALL;
            if (onlyDb) {
                apiCall += "/p";
            }
            JOCDefaultResponse jocDefaultResponse = init(apiCall, jobSchedulerBody, accessToken, jobSchedulerBody.getJobschedulerId(),
                    getPermissonsJocCockpit(jobSchedulerBody.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            
            InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
            DBItemInventoryInstance schedulerInstance = null;
            
            if (jobSchedulerBody.getUrl() == null) {
                schedulerInstance = dbItemInventoryInstance;
            } else {
                schedulerInstance = instanceLayer.getInventoryInstanceByURI(jobSchedulerBody.getUrl());
                if (schedulerInstance == null) {
                    throw new DBMissingDataException("No JobScheduler found with url: " + jobSchedulerBody.getUrl());
                }
            }

            ControllerAnswer master = new ControllerCallable(schedulerInstance, osDBLayer.getInventoryOperatingSystem(schedulerInstance.getOsId()),
                    accessToken, onlyDb).call();

            if (!onlyDb && master != null) {
                Long osId = osDBLayer.saveOrUpdateOSItem(master.getDbOs());
                master.setOsId(osId);

                if (master.dbInstanceIsChanged()) {
                    instanceLayer.updateInstance(master.getDbInstance());
                }
            }

            JobScheduler200 entity = new JobScheduler200();
            entity.setJobscheduler(master);
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
