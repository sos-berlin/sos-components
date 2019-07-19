package com.sos.joc.jobscheduler.impl;

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

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
import com.sos.joc.model.jobscheduler.HostPortParameter;
import com.sos.joc.model.jobscheduler.UriParameter;

@Path("jobscheduler")
public class JobSchedulerCleanupInventoryImpl extends JOCResourceImpl implements IJobSchedulerCleanUpInventoryResource {

	private static String API_CALL = "./jobscheduler/cleanup";

	@Deprecated
	@Override
	public JOCDefaultResponse oldPostJobschedulerCleanupInventory(String accessToken, HostPortParameter hostPortParameter)
			throws Exception {
		checkRequiredParameter("host", hostPortParameter.getHost());
		checkRequiredParameter("port", hostPortParameter.getPort());
		URI uri = UriBuilder.fromPath(toURI(hostPortParameter.getHost(), hostPortParameter.getPort())).build();
		UriParameter uriParameter = new UriParameter();
		uriParameter.setJobschedulerId(hostPortParameter.getJobschedulerId());
		uriParameter.setUri(uri);
		uriParameter.setAuditLog(hostPortParameter.getAuditLog());
		return postJobschedulerCleanupInventory(accessToken, uriParameter);
	}

	@Override
	public JOCDefaultResponse postJobschedulerCleanupInventory(String accessToken, UriParameter uriParameter)
			throws Exception {
		SOSHibernateSession connection = null;
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, uriParameter, accessToken,
					uriParameter.getJobschedulerId(),
					getPermissonsJocCockpit(uriParameter.getJobschedulerId(), accessToken).getJobschedulerMaster()
							.getAdministration().isRemoveOldInstances());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			checkRequiredParameter("uri", uriParameter.getUri());
			checkRequiredComment(uriParameter.getAuditLog());
			ModifyJobSchedulerAudit jobschedulerAudit = new ModifyJobSchedulerAudit(uriParameter);
			logAuditMessage(jobschedulerAudit);
			connection = Globals.createSosHibernateStatelessConnection(API_CALL);

			InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
			DBItemInventoryInstance schedulerInstanceFromDb = instanceLayer
					.getInventoryInstanceByURI(uriParameter.getJobschedulerId(), uriParameter.getUri().toString());
			boolean jobSchedulerIsRunning = true;
			try {
				JOCJsonCommand jocJsonCommand = new JOCJsonCommand(schedulerInstanceFromDb);
				jocJsonCommand.setUriBuilderForOverview();
				jocJsonCommand.getJsonObjectFromGet(accessToken);
			} catch (JobSchedulerConnectionRefusedException e) {
				jobSchedulerIsRunning = false;
			} catch (JocException e) {
				//
			 }
			if (jobSchedulerIsRunning) {
				throw new JobSchedulerBadRequestException(
						"Cleanup function is not available when JobScheduler is still running.");
			}
			
			// TODO
			return JOCDefaultResponse.responseNotYetImplemented();
			//TODO instanceLayer.cleanUp(schedulerInstanceFromDb);
//			storeAuditLogEntry(jobschedulerAudit);

//			if (uriParameter.getJobschedulerId().equals(dbItemInventoryInstance.getSchedulerId())
//					&& uriParameter.getUri().equals(dbItemInventoryInstance.getUri())) {
//				try {
//					SOSShiroCurrentUser shiroUser = getJobschedulerUser().getSosShiroCurrentUser();
//					shiroUser.removeSchedulerInstanceDBItem(dbItemInventoryInstance.getSchedulerId());
//				} catch (InvalidSessionException e1) {
//					throw new SessionNotExistException(e1);
//				}
//			}
//			return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
