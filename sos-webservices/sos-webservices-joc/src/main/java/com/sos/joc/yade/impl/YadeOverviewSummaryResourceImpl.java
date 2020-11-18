package com.sos.joc.yade.impl;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.yade.TransferFilesSummary;
import com.sos.joc.model.yade.TransferFilter;
import com.sos.joc.yade.resource.IYadeOverviewSummaryResource;

@Path("yade")
public class YadeOverviewSummaryResourceImpl extends JOCResourceImpl implements IYadeOverviewSummaryResource {

	private static final String API_CALL = "./yade/overview/summary";

	@Override
	public JOCDefaultResponse postYadeOverviewSummary(String accessToken, TransferFilter filterBody) throws Exception {
		try {
			SOSPermissionJocCockpit sosPermission = getPermissonsJocCockpit(filterBody.getControllerId(),
					accessToken);
			// JobSchedulerId has to be "" to prevent exception to be thrown
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, filterBody, accessToken,
					filterBody.getControllerId(), sosPermission.getYADE().getView().isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			TransferFilesSummary entity = new TransferFilesSummary();
			entity.setSuccessful(0);
			entity.setFailed(0);
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}
