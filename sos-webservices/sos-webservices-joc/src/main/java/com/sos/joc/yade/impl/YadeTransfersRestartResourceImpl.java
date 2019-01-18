package com.sos.joc.yade.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.yade.ModifyTransfers;
import com.sos.joc.yade.resource.IYadeTransfersRestartResource;

@Path("yade")
public class YadeTransfersRestartResourceImpl extends JOCResourceImpl implements IYadeTransfersRestartResource {

	private static final String API_CALL = "./yade/transfers/restart";

	@Override
	public JOCDefaultResponse postYadeTransfersRestart(String accessToken, ModifyTransfers filterBody)
			throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, filterBody, accessToken,
					filterBody.getJobschedulerId(), getPermissonsJocCockpit(filterBody.getJobschedulerId(), accessToken)
							.getYADE().getExecute().isTransferStart());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			checkRequiredComment(filterBody.getAuditLog());
			if (filterBody.getTransfers() == null || filterBody.getTransfers().size() == 0) {
				throw new JocMissingRequiredParameterException("undefined 'transferIds'");
			}
			
			return JOCDefaultResponse.responseStatusJSOk(new Date());
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}
