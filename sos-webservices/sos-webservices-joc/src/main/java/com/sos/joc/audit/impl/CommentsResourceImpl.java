package com.sos.joc.audit.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.audit.resource.ICommentsResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.audit.Comments;

import jakarta.ws.rs.Path;

@Path("audit_log")
public class CommentsResourceImpl extends JOCResourceImpl implements ICommentsResource {

    private static final String API_CALL = "./audit_log/comments";
    
    @Override
    public JOCDefaultResponse postComments(String xAccessToken, String accessToken) throws Exception {
        return postComments(getAccessToken(xAccessToken, accessToken));
    }

    public JOCDefaultResponse postComments(String accessToken) throws Exception {
        
        try {
            initLogging(API_CALL, "{}".getBytes(), accessToken, CategoryType.OTHERS);
            
            ConfigurationGlobalsJoc clusterSettings = Globals.getConfigurationGlobalsJoc();
            Comments entity = new Comments();
            entity.setForceCommentsForAuditLog(ClusterSettings.getForceCommentsForAuditLog(clusterSettings));
            entity.setComments(ClusterSettings.getCommentsForAuditLog(clusterSettings));
            entity.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } 
    }
}
