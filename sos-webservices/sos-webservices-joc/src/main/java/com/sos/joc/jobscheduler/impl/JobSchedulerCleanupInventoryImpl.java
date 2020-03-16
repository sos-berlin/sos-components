package com.sos.joc.jobscheduler.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyJobSchedulerAudit;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerCleanUpInventoryResource;
import com.sos.joc.model.jobscheduler.UrlParameter;

@Path("jobscheduler")
public class JobSchedulerCleanupInventoryImpl extends JOCResourceImpl implements IJobSchedulerCleanUpInventoryResource {

    private static String API_CALL = "./jobscheduler/cleanup";

    @Override
    public JOCDefaultResponse postJobschedulerCleanupInventory(String accessToken, UrlParameter urlParameter) {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, urlParameter, accessToken, urlParameter.getJobschedulerId(),
                    getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJobschedulerMaster().getAdministration()
                            .isRemoveOldInstances());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("url", urlParameter.getUrl());
            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerAudit jobschedulerAudit = new ModifyJobSchedulerAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);

            InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
            DBItemInventoryInstance schedulerInstanceFromDb = instanceLayer.getInventoryInstanceByURI(urlParameter.getUrl());
            boolean jobSchedulerIsRunning = true;
            try {
                JOCJsonCommand jocJsonCommand = new JOCJsonCommand(schedulerInstanceFromDb, accessToken);
                jocJsonCommand.setUriBuilderForOverview();
                jocJsonCommand.getJsonObjectFromGet();
            } catch (JobSchedulerConnectionRefusedException e) {
                jobSchedulerIsRunning = false;
            } catch (JocException e) {
                //
            }
            if (jobSchedulerIsRunning) {
                throw new JobSchedulerBadRequestException("Cleanup function is not available when JobScheduler is still running.");
            }

            // TODO
            return JOCDefaultResponse.responseNotYetImplemented();
            // TODO instanceLayer.cleanUp(schedulerInstanceFromDb);
            // storeAuditLogEntry(jobschedulerAudit);

            // if (uriParameter.getJobschedulerId().equals(dbItemInventoryInstance.getSchedulerId())
            // && uriParameter.getUri().equals(dbItemInventoryInstance.getUri())) {
            // try {
            // SOSShiroCurrentUser shiroUser = getJobschedulerUser().getSosShiroCurrentUser();
            // shiroUser.removeSchedulerInstanceDBItem(dbItemInventoryInstance.getSchedulerId());
            // } catch (InvalidSessionException e1) {
            // throw new SessionNotExistException(e1);
            // }
            // }
            // return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
