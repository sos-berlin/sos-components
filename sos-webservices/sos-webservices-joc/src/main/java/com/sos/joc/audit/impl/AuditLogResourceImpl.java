package com.sos.joc.audit.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SearchStringHelper;
import com.sos.joc.Globals;
import com.sos.joc.audit.resource.IAuditLogResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.audit.AuditLogDBFilter;
import com.sos.joc.db.audit.AuditLogDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditLog;
import com.sos.joc.model.audit.AuditLogFilter;
import com.sos.joc.model.audit.AuditLogItem;
import com.sos.schema.JsonValidator;

@Path("audit_log")
public class AuditLogResourceImpl extends JOCResourceImpl implements IAuditLogResource {

    private static final String API_CALL = "./audit_log";

    @Override
    public JOCDefaultResponse postAuditLog(String accessToken, byte[] bytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, bytes, accessToken);
            JsonValidator.validateFailFast(bytes, AuditLogFilter.class);
            AuditLogFilter auditLogFilter = Globals.objectMapper.readValue(bytes, AuditLogFilter.class);
            String controllerId = auditLogFilter.getControllerId() == null ? "" : auditLogFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getJocPermissions(accessToken).getAuditLog().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // controllerId == "-" for requests ./inventory, ./profile
            AuditLogDBFilter auditLogDBFilter = new AuditLogDBFilter(auditLogFilter);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            AuditLogDBLayer dbLayer = new AuditLogDBLayer(connection);
            String filterRegex = auditLogFilter.getRegex();
            if (SearchStringHelper.isDBWildcardSearch(filterRegex)) {
                auditLogDBFilter.setReason(filterRegex);
                filterRegex = "";
            }
            List<DBItemJocAuditLog> auditLogs = dbLayer.getAuditLogs(auditLogDBFilter, auditLogFilter.getLimit());

            if (filterRegex != null && !filterRegex.isEmpty()) {
                auditLogs = filterComment(auditLogs, filterRegex);
            }
            AuditLog entity = new AuditLog();
            entity.setAuditLog(fillAuditLogItems(auditLogs, controllerId));
            entity.setDeliveryDate(new Date());

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private List<DBItemJocAuditLog> filterComment(List<DBItemJocAuditLog> auditLogsUnfiltered, String regex) {
        List<DBItemJocAuditLog> filteredAuditLogs = new ArrayList<DBItemJocAuditLog>();
        for (DBItemJocAuditLog auditLogUnfiltered : auditLogsUnfiltered) {
            if (auditLogUnfiltered.getComment() != null && !auditLogUnfiltered.getComment().isEmpty()) {
                Matcher regExMatcher = Pattern.compile(regex).matcher(auditLogUnfiltered.getComment());
                if (regExMatcher.find()) {
                    filteredAuditLogs.add(auditLogUnfiltered);
                }
            }
        }
        return filteredAuditLogs;
    }

    private List<AuditLogItem> fillAuditLogItems(List<DBItemJocAuditLog> auditLogsFromDb, String controllerId) throws JocException,
            InvalidSessionException, JsonParseException, JsonMappingException, IOException {
        List<AuditLogItem> audits = new ArrayList<AuditLogItem>();
        if (auditLogsFromDb != null) {
            for (DBItemJocAuditLog auditLogFromDb : auditLogsFromDb) {
                AuditLogItem auditLogItem = new AuditLogItem();
                if (controllerId.isEmpty()) {
                    if (!getJocPermissions(getAccessToken()).getAuditLog().getView()) {
                        continue;
                    }
                    auditLogItem.setControllerId(auditLogFromDb.getControllerId());
                }
                auditLogItem.setAccount(auditLogFromDb.getAccount());
                auditLogItem.setRequest(auditLogFromDb.getRequest());
                auditLogItem.setParameters(auditLogFromDb.getParameters());
                auditLogItem.setComment(auditLogFromDb.getComment());
                auditLogItem.setCreated(auditLogFromDb.getCreated());
                auditLogItem.setTicketLink(auditLogFromDb.getTicketLink());
                auditLogItem.setTimeSpent(auditLogFromDb.getTimeSpent());
                audits.add(auditLogItem);
            }
        }
        return audits;
    }

}