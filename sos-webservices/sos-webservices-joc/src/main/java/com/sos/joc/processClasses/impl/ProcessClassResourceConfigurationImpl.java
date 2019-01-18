package com.sos.joc.processClasses.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Configuration;
import com.sos.joc.model.common.Configuration200;
import com.sos.joc.model.processClass.ProcessClassConfigurationFilter;
import com.sos.joc.processClasses.resource.IProcessClassResourceConfiguration;

@Path("process_class")
public class ProcessClassResourceConfigurationImpl extends JOCResourceImpl
		implements IProcessClassResourceConfiguration {

	private static final String API_CALL = "./process_class/configuration";

	@Override
	public JOCDefaultResponse postProcessClassConfiguration(String xAccessToken, String accessToken,
			ProcessClassConfigurationFilter processClassConfigurationFilter) throws Exception {
		return postProcessClassConfiguration(getAccessToken(xAccessToken, accessToken),
				processClassConfigurationFilter);
	}

	public JOCDefaultResponse postProcessClassConfiguration(String accessToken,
			ProcessClassConfigurationFilter processClassConfigurationFilter) throws Exception {

		try {
			JOCDefaultResponse jocDefaultResponse = init(API_CALL, processClassConfigurationFilter, accessToken,
					processClassConfigurationFilter.getJobschedulerId(),
					getPermissonsJocCockpit(processClassConfigurationFilter.getJobschedulerId(), accessToken)
							.getProcessClass().getView().isConfiguration());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			Configuration200 entity = new Configuration200();
			entity.setDeliveryDate(Date.from(Instant.now()));
			Configuration conf = new Configuration();
			entity.setConfiguration(conf);
			return JOCDefaultResponse.responseStatus200(entity);
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}
}