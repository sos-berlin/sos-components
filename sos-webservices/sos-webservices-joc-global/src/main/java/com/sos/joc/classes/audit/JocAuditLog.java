package com.sos.joc.classes.audit;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;

public class JocAuditLog {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger(WebserviceConstants.AUDIT_LOGGER);
    private static final Logger LOGGER = LoggerFactory.getLogger(JocAuditLog.class);
    private String user;
    private String request;
    private String params;
    private boolean isLogged = false;
    
    public JocAuditLog(String user, String request) {
        this.user = setProperty(user);
        this.request = setProperty(request);
        this.params = "-";
    }
    
    public JocAuditLog(String user, String request, String params) {
        this.user = setProperty(user);
        this.request = setProperty(request);
        this.params = setProperty(params);
    }

    private String setProperty(String prop) {
        if (prop == null || prop.isEmpty()) {
            prop = "-";
        }
        return prop;
    }

    public void logAuditMessage(AuditParams audit) {
        if (!isLogged) {
            isLogged = true;
            try {
                String comment = "-";
                String timeSpent = "-";
                String ticketLink = "-";
                if (audit != null) {
                    if (audit.getComment() != null) {
                        comment = audit.getComment();
                    }
                    if (audit.getTimeSpent() != null) {
                        timeSpent = audit.getTimeSpent().toString() + "m";
                    }
                    if (audit.getTicketLink() != null) {
                        ticketLink = audit.getTicketLink();
                    }
                }
                AUDIT_LOGGER.info(String.format("REQUEST: %1$s - USER: %2$s - PARAMS: %3$s - COMMENT: %4$s - TIMESPENT: %5$s - TICKET: %6$s", request,
                        user, params, comment, timeSpent, ticketLink));
            } catch (Exception e) {
                LOGGER.error("Cannot write to audit log file", e);
            }
        }
    }
    
    public synchronized DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, String controllerId, CategoryType type,
            SOSHibernateSession connection) {
        if (audit != null) {
            controllerId = setProperty(controllerId);
            DBItemJocAuditLog auditLogToDb = new DBItemJocAuditLog();
            auditLogToDb.setControllerId(controllerId);
            auditLogToDb.setAccount(user);
            auditLogToDb.setRequest(request);
            auditLogToDb.setParameters(params);
            auditLogToDb.setCategory(type != null ? type.intValue() : 0);
            auditLogToDb.setComment(audit.getComment());
            auditLogToDb.setTicketLink(audit.getTicketLink());
            auditLogToDb.setTimeSpent(audit.getTimeSpent());
            auditLogToDb.setCreated(Date.from(Instant.now()));
            if (connection == null) {
                SOSHibernateSession connection2 = null;
                try {
                    connection2 = Globals.createSosHibernateStatelessConnection("storeAuditLogEntry");
                    connection2.save(auditLogToDb);
                    return auditLogToDb;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    Globals.disconnect(connection2);
                }
            } else {
                try {
                    connection.save(auditLogToDb);
                    return auditLogToDb;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

//    public synchronized DBItemJocAuditLog storeAuditLogEntry(IAuditLog body, SOSHibernateSession connection) {
//        if (body != null) {
//            String controllerId = body.getControllerId();
//            if (controllerId == null || controllerId.isEmpty()) {
//                controllerId = "-";
//            }
//            DBItemJocAuditLog auditLogToDb = new DBItemJocAuditLog();
//            auditLogToDb.setControllerId(controllerId);
//            auditLogToDb.setAccount(user);
//            auditLogToDb.setRequest(request);
//            //auditLogToDb.setParameters(getJsonString(body));
//            auditLogToDb.setParameters(params);
//            auditLogToDb.setCategory(0); // TODO category
//            auditLogToDb.setComment(body.getComment());
//            auditLogToDb.setTicketLink(body.getTicketLink());
//            auditLogToDb.setTimeSpent(body.getTimeSpent());
//            auditLogToDb.setCreated(Date.from(Instant.now()));
//            if (connection == null) {
//                SOSHibernateSession connection2 = null;
//                try {
//                    connection2 = Globals.createSosHibernateStatelessConnection("storeAuditLogEntry");
//                    connection2.save(auditLogToDb);
//                    return auditLogToDb;
//                } catch (Exception e) {
//                    LOGGER.error(e.getMessage(), e);
//                } finally {
//                    Globals.disconnect(connection2);
//                }
//            } else {
//                try {
//                    connection.save(auditLogToDb);
//                    return auditLogToDb;
//                } catch (Exception e) {
//                    LOGGER.error(e.getMessage(), e);
//                }
//            }
//        }
//        return null;
//    }

//    public synchronized DBItemJocAuditLog storeAuditLogEntry(IAuditLog body) {
//        return storeAuditLogEntry(body, null);
//    }
    
    public DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, String controllerId, CategoryType type) {
        return storeAuditLogEntry(audit, controllerId, type, null);
    }
    
    public DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, CategoryType type) {
        return storeAuditLogEntry(audit, null, type, null);
    }

//    private String getJsonString(IAuditLog body) {
//        if (body == null) {
//            return "-";
//        }
//        try {
//            return new Globals.objectMapper.writeValueAsString(body);
//        } catch (Exception e) {
//            return body.toString();
//        }
//    }
}
