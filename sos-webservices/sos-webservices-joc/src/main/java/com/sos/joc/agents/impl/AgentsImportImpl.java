package com.sos.joc.agents.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsImport;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.agent.transfer.AgentImportFilter;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsImportImpl extends JOCResourceImpl implements IAgentsImport {

    private static final String API_CALL = "./agents/export";

    @Override
    public JOCDefaultResponse postImportConfiguration(String xAccessToken, FormDataBodyPart body, String format, String timeSpent, String ticketLink,
            String comment) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
        AgentImportFilter filter = new AgentImportFilter();
        filter.setAuditLog(auditLog);
        filter.setFormat(ArchiveFormat.fromValue(format));
        return postImport(xAccessToken, body, filter, auditLog);
    }

    
    public JOCDefaultResponse postImport(String xAccessToken, FormDataBodyPart body, AgentImportFilter filter, AuditParams auditLog) {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken); 
            JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(filter), AgentImportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            DBItemJocAuditLog dbAuditItem = storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            Long auditLogId = dbAuditItem.getId();
            
        // TODO Auto-generated method stub
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
    }

}
