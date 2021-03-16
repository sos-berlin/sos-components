package com.sos.joc.audit.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.audit.resource.ICommentsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.Comments;

@Path("audit_log")
public class CommentsResourceImpl extends JOCResourceImpl implements ICommentsResource {

    private static final String API_CALL = "./audit_log/comments";
    
    @Override
    public JOCDefaultResponse postComments(String xAccessToken, String accessToken) throws Exception {
        return postComments(getAccessToken(xAccessToken, accessToken));
    }

    public JOCDefaultResponse postComments(String accessToken) throws Exception {
        
        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
            Comments entity = new Comments();
            entity.setForceCommentsForAuditLog(ClusterSettings.getForceCommentsForAuditLog(clusterSettings));
            entity.setComments(ClusterSettings.getCommentsForAuditLog(clusterSettings));
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
