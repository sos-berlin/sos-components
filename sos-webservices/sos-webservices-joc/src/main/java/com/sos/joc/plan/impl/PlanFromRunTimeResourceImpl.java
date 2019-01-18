package com.sos.joc.plan.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.RunTime;
import com.sos.joc.model.plan.RunTimePlanFilter;
import com.sos.joc.plan.resource.IPlanFromRunTimeResource;


@Path("plan")
public class PlanFromRunTimeResourceImpl extends JOCResourceImpl implements IPlanFromRunTimeResource {

	private static final String API_CALL = "./plan/from_run_time";

	@Override
	public JOCDefaultResponse postPlan(String accessToken, RunTimePlanFilter planFilter) throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, planFilter, accessToken,
					planFilter.getJobschedulerId(), getPermissonsJocCockpit(planFilter.getJobschedulerId(), accessToken)
							.getOrder().getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			RunTime entity = new RunTime();
			entity.setDeliveryDate(Date.from(Instant.now()));
			//entity.setPeriods(null);
			entity.setTimeZone(null);
			
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

}