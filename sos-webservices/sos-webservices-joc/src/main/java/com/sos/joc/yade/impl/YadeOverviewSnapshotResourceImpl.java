package com.sos.joc.yade.impl;

import java.sql.Date;
import java.time.Instant;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.ControllerId;
import com.sos.joc.model.yade.TransfersSummary;
import com.sos.joc.model.yade.YadeSnapshot;
import com.sos.joc.yade.resource.IYadeOverviewSnapshotResource;

@Path("yade")
public class YadeOverviewSnapshotResourceImpl extends JOCResourceImpl implements IYadeOverviewSnapshotResource {

	private static final String API_CALL = "./yade/overview/snapshot";

	@Override
	public JOCDefaultResponse postYadeOverviewSnapshot(String accessToken, ControllerId jobschedulerId)
			throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobschedulerId, accessToken,
					jobschedulerId.getControllerId(),
					getPermissonsJocCockpit(jobschedulerId.getControllerId(), accessToken).getYADE().getView()
							.isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			TransfersSummary summary = new TransfersSummary();
			summary.setRunning(0);
			summary.setSetback(0);
			summary.setSuspended(0);
			summary.setWaitingForResource(0);

			YadeSnapshot snapshot = new YadeSnapshot();
			snapshot.setTransfers(summary);
			snapshot.setSurveyDate(Date.from(Instant.now()));
			snapshot.setDeliveryDate(snapshot.getSurveyDate());

			return JOCDefaultResponse.responseStatus200(snapshot);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}
