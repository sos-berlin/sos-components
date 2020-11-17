package com.sos.joc.yade.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.yade.FilesFilter;
import com.sos.joc.model.yade.TransferFiles;
import com.sos.joc.yade.resource.IYadeFilesResource;

@Path("yade")
public class YadeFilesResourceImpl extends JOCResourceImpl implements IYadeFilesResource {

	private static final String API_CALL = "./yade/files";

	@Override
	public JOCDefaultResponse postYadeFiles(String accessToken, FilesFilter filterBody) throws Exception {
		try {
			if (filterBody.getControllerId() == null) {
				filterBody.setControllerId("");
			}
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, filterBody, accessToken,
					filterBody.getControllerId(),
					getPermissonsJocCockpit(filterBody.getControllerId(), accessToken).getYADE().getView().isFiles());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			
			TransferFiles entity = new TransferFiles();
			entity.setFiles(null);
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
