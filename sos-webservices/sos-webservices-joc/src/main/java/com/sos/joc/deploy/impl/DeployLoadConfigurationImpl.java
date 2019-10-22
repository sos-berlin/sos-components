package com.sos.joc.deploy.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.deploy.resource.IDeployLoadConfigurationResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.deploy.DeployFilter;

@Path("deploy")
public class DeployLoadConfigurationImpl extends JOCResourceImpl implements IDeployLoadConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployLoadConfigurationImpl.class);
    private static final String API_CALL = "./deploy/load";

	@Override
	public JOCDefaultResponse postDeployLoadConfiguration(String xAccessToken, DeployFilter filter, String comment)
			throws Exception {
		// TODO Auto-generated method stub
		SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, filter.getJobschedulerId(),
                    getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getJobChain().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);

            Boolean compact = filter.getCompact();


            if (compact != null && !compact) {
            	// TODO: Not compact (if at all)
            } else {
                // TODO: compact
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
