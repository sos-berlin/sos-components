package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.JobSchedulerAnswer;
import com.sos.joc.classes.jobscheduler.JobSchedulerCallable;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.inventory.os.InventoryOperatingSystemsDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResource;
import com.sos.joc.model.jobscheduler.JobScheduler200;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerResourceImpl extends JOCResourceImpl implements IJobSchedulerResource {

    private static final String API_CALL = "./jobscheduler";

    @Override
    public JOCDefaultResponse postJobschedulerP(String accessToken, UrlParameter jobSchedulerBody) {
        return postJobscheduler(accessToken, jobSchedulerBody);
    }

    @Override
    public JOCDefaultResponse postJobscheduler(String accessToken, UrlParameter jobSchedulerBody) {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerBody, accessToken, jobSchedulerBody.getJobschedulerId(),
                    getPermissonsJocCockpit(jobSchedulerBody.getJobschedulerId(), accessToken).getJobschedulerMaster().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            setJobSchedulerInstanceByURI(jobSchedulerBody.getJobschedulerId(), jobSchedulerBody.getUrl());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryOperatingSystemsDBLayer osDBLayer = new InventoryOperatingSystemsDBLayer(connection);
            DBItemOperatingSystem osSystem = osDBLayer.getInventoryOperatingSystem(dbItemInventoryInstance.getOsId());

            JobSchedulerAnswer jobschedulerAnswer = new JobSchedulerCallable(dbItemInventoryInstance, osSystem, accessToken).call();

            Long osId = osDBLayer.saveOrUpdateOSItem(jobschedulerAnswer.getDbOs());
            jobschedulerAnswer.setOsId(osId);

            if (jobschedulerAnswer.dbInstanceIsChanged()) {
                InventoryInstancesDBLayer instanceDBLayer = new InventoryInstancesDBLayer(connection);
                instanceDBLayer.updateInstance(jobschedulerAnswer.getDbInstance());
            }

            JobScheduler200 entity = new JobScheduler200();
            entity.setJobscheduler(jobschedulerAnswer);
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
    
    @Deprecated
    @Override
    public JOCDefaultResponse oldPostJobschedulerDb(String accessToken) {
        return new JobSchedulerResourceDbImpl().postJobschedulerDb(accessToken);
    }

}
