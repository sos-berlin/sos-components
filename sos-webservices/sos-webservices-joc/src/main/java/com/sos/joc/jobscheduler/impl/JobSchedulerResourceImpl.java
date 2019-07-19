package com.sos.joc.jobscheduler.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.jobscheduler.JobSchedulerVCallable;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResource;
import com.sos.joc.model.jobscheduler.HostPortParameter;
import com.sos.joc.model.jobscheduler.JobSchedulerV200;
import com.sos.joc.model.jobscheduler.UriParameter;

@Path("jobscheduler")
public class JobSchedulerResourceImpl extends JOCResourceImpl implements IJobSchedulerResource {

	private static final String API_CALL = "./jobscheduler";

	@Deprecated
	@Override
	public JOCDefaultResponse oldPostJobscheduler(String accessToken, HostPortParameter jobSchedulerBody) {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerBody, accessToken,
					jobSchedulerBody.getJobschedulerId(),
					getPermissonsJocCockpit(jobSchedulerBody.getJobschedulerId(), accessToken).getJobschedulerMaster()
							.getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			setJobSchedulerInstanceByURI(jobSchedulerBody.getJobschedulerId(),
					toURI(jobSchedulerBody.getHost(), jobSchedulerBody.getPort()));
			JobSchedulerV200 entity = new JobSchedulerV200();
			entity.setJobscheduler(new JobSchedulerVCallable(dbItemInventoryInstance, accessToken).call());
			entity.setDeliveryDate(new Date());
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}
	
	@Override
	public JOCDefaultResponse postJobscheduler(String accessToken, UriParameter jobSchedulerBody) {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerBody, accessToken,
					jobSchedulerBody.getJobschedulerId(),
					getPermissonsJocCockpit(jobSchedulerBody.getJobschedulerId(), accessToken).getJobschedulerMaster()
							.getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			setJobSchedulerInstanceByURI(jobSchedulerBody.getJobschedulerId(), jobSchedulerBody.getUri().toString());
			JobSchedulerV200 entity = new JobSchedulerV200();
			entity.setJobscheduler(new JobSchedulerVCallable(dbItemInventoryInstance, accessToken).call());
			entity.setDeliveryDate(new Date());
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}
