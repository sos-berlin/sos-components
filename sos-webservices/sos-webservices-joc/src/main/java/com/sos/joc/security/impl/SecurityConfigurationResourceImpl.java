package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.security.resource.ISecurityConfigurationResource;

@Path("authentication")
public class SecurityConfigurationResourceImpl extends JOCResourceImpl implements ISecurityConfigurationResource {

	private static final String API_CALL_READ = "./authentication/shiro";
	private static final String API_CALL_WRITE = "./authentication/store";

	
	@Override
	public JOCDefaultResponse postShiroRead(String accessToken) {
	    SOSHibernateSession connection = null;
	    try {
	        initLogging(API_CALL_READ, null, accessToken);
			JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
			SecurityConfiguration entity = sosSecurityConfiguration.readConfiguration();
			
			connection = Globals.createSosHibernateStatelessConnection(API_CALL_READ);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(connection);
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType("PROFILE");
     
            entity.setProfiles(jocConfigurationDBLayer.getJocConfigurationProfiles(filter));
			
            entity.setDeliveryDate(Date.from(Instant.now()));

			return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		} finally {
		    Globals.disconnect(connection);
		}

	}

	@Override
	public JOCDefaultResponse postShiroStore(String accessToken, byte[] body)  {
		try {
		    initLogging(API_CALL_WRITE, body, accessToken);
		    // TODO JsonValidator
		    SecurityConfiguration securityConfiguration = Globals.objectMapper.readValue(body, SecurityConfiguration.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
            SecurityConfiguration s = sosSecurityConfiguration.writeConfiguration(securityConfiguration);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(s));
        } catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}

	}

}