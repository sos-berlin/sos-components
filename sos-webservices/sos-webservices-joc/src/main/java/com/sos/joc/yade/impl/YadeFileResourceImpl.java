package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.yade.FileFilter;
import com.sos.joc.model.yade.TransferFile200;
import com.sos.joc.yade.resource.IYadeFileResource;

@Path("yade")
public class YadeFileResourceImpl extends JOCResourceImpl implements IYadeFileResource {

	private static final String API_CALL = "./yade/file";

	@Override
	public JOCDefaultResponse postYadeFile(String accessToken, FileFilter filterBody) throws Exception {
		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, filterBody, accessToken,
					filterBody.getControllerId(),
					getPermissonsJocCockpit(filterBody.getControllerId(), accessToken).getYADE().getView().isFiles());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			TransferFile200 entity = new TransferFile200();
			entity.setFile(null);
			entity.setDeliveryDate(Date.from(Instant.now()));
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}
