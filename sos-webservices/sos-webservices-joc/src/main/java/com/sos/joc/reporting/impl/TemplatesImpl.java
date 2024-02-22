package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.reporting.Templates;
import com.sos.joc.reporting.resource.ITemplatesResource;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class TemplatesImpl extends JOCResourceImpl implements ITemplatesResource {
    
    @Override
    public JOCDefaultResponse show(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getReports().getView());
            if (response != null) {
                return response;
            }
            
            Templates entity = new Templates();
            entity.setTemplates(com.sos.joc.classes.reporting.Templates.getTemplates());
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
