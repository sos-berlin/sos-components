package com.sos.joc.classes.audit;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.model.audit.AuditParams;

public class JocAuditLog {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger(WebserviceConstants.AUDIT_LOGGER);
    private static final Logger LOGGER = LoggerFactory.getLogger(JocAuditLog.class);
    private String user;
    private String request;
    private String params;
    private boolean isLogged = false;
    public static final String EMPTY_STRING = "-";
    
    public JocAuditLog(String user, String request) {
        this.user = setProperty(user);
        this.request = setProperty(request);
        this.params = EMPTY_STRING;
    }
    
    public JocAuditLog(String user, String request, String params) {
        this.user = setProperty(user);
        this.request = setProperty(request);
        this.params = setProperty(params);
    }

    private String setProperty(String prop) {
        if (prop == null || prop.isEmpty()) {
            prop = EMPTY_STRING;
        }
        return prop;
    }

    public void logAuditMessage(AuditParams audit) {
        if (!isLogged) {
            isLogged = true;
            try {
                String comment = EMPTY_STRING;
                String timeSpent = EMPTY_STRING;
                String ticketLink = EMPTY_STRING;
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
    
    public synchronized DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, String controllerId, Integer type,
            SOSHibernateSession connection) {
        controllerId = setProperty(controllerId);
        DBItemJocAuditLog auditLogToDb = new DBItemJocAuditLog();
        auditLogToDb.setControllerId(controllerId);
        auditLogToDb.setAccount(user);
        auditLogToDb.setRequest(request);
        auditLogToDb.setParameters(params);
        auditLogToDb.setCategory(type != null ? type : 0);
        if (audit != null) {
            auditLogToDb.setComment(audit.getComment());
            auditLogToDb.setTicketLink(audit.getTicketLink());
            auditLogToDb.setTimeSpent(audit.getTimeSpent());
        }
        auditLogToDb.setCreated(Date.from(Instant.now()));
        if (connection == null) {
            try {
                connection = Globals.createSosHibernateStatelessConnection("storeAuditLogEntry");
                connection.save(auditLogToDb);
                return auditLogToDb;
            } catch (Exception e) {
                LOGGER.error("", e);
            } finally {
                Globals.disconnect(connection);
            }
        } else {
            try {
                connection.save(auditLogToDb);
                return auditLogToDb;
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        auditLogToDb.setId(0L);
        return auditLogToDb;
    }
    
    public DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, String controllerId, Integer type) {
        return storeAuditLogEntry(audit, controllerId, type, null);
    }
    
    public DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, Integer type) {
        return storeAuditLogEntry(audit, null, type, null);
    }
    
    public static void updateAuditLogEntry(DBItemJocAuditLog dbItem) {
        updateAuditLogEntry(dbItem, null);
    }
    
    public static void updateAuditLogEntry(DBItemJocAuditLog dbItem, SOSHibernateSession connection) {
        if (dbItem != null) {
            if (connection == null) {
                try {
                    connection = Globals.createSosHibernateStatelessConnection("updateAuditLog");
                    connection.update(dbItem);
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    Globals.disconnect(connection);
                }
            } else {
                try {
                    connection.update(dbItem);
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
    }
    
//    public static void storeAuditLogDetails(Collection<DBItemJocAuditLogDetails> dbItems) {
//        storeAuditLogDetails(dbItems, null);
//    }
//    
//    public static void storeAuditLogDetails(Collection<DBItemJocAuditLogDetails> dbItems, SOSHibernateSession connection) {
//        if (dbItems != null && !dbItems.isEmpty()) {
//            if (connection == null) {
//                try {
//                    connection = Globals.createSosHibernateStatelessConnection("storeAuditLogDetail");
//                    for (DBItemJocAuditLogDetails dbItem : dbItems) {
//                        storeAuditLogDetail(dbItem, connection);
//                    }
//                } catch (Exception e) {
//                    LOGGER.error(e.getMessage(), e);
//                } finally {
//                    Globals.disconnect(connection);
//                }
//            } else {
//                for (DBItemJocAuditLogDetails dbItem : dbItems) {
//                    storeAuditLogDetail(dbItem, connection);
//                }
//            }
//        }
//    }
//    
    public static void storeAuditLogDetails(Collection<AuditLogDetail> details, Long auditlogId) {
        storeAuditLogDetails(details, auditlogId, Date.from(Instant.now()));
    }
    
    public static void storeAuditLogDetails(Collection<AuditLogDetail> details, SOSHibernateSession connection, Long auditlogId) {
        storeAuditLogDetails(details, connection, auditlogId, Date.from(Instant.now()));
    }
    
    public static void storeAuditLogDetails(Collection<AuditLogDetail> details, Long auditlogId, Date now) {
        storeAuditLogDetails(details, null, auditlogId, now);
    }
    
    public static void storeAuditLogDetails(Collection<AuditLogDetail> details, SOSHibernateSession connection, DBItemJocAuditLog dbAuditItem) {
        storeAuditLogDetails(details, connection, dbAuditItem.getId(), dbAuditItem.getCreated());
    }
    
    public static synchronized void storeAuditLogDetails(Collection<AuditLogDetail> details, SOSHibernateSession connection, Long auditlogId, Date now) {
        if (details != null && !details.isEmpty() && auditlogId != null) {
            if (connection == null) {
                try {
                    connection = Globals.createSosHibernateStatelessConnection("storeAuditLogDetail");
                    for (AuditLogDetail detail : details) {
                        storeAuditLogDetail(detail.getAuditLogDetail(auditlogId, now), connection);
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    Globals.disconnect(connection);
                }
            } else {
                for (AuditLogDetail detail : details) {
                    storeAuditLogDetail(detail.getAuditLogDetail(auditlogId, now), connection);
                }
            }
        }
    }
    
    public static void storeAuditLogDetails(Stream<AuditLogDetail> details, Long auditlogId) {
        storeAuditLogDetails(details, auditlogId, Date.from(Instant.now()));
    }
    
    public static void storeAuditLogDetails(Stream<AuditLogDetail> details, SOSHibernateSession connection, Long auditlogId) {
        storeAuditLogDetails(details, connection, auditlogId, Date.from(Instant.now()));
    }
    
    public static void storeAuditLogDetails(Stream<AuditLogDetail> details, Long auditlogId, Date now) {
        storeAuditLogDetails(details, null, auditlogId, now);
    }
    
    public static void storeAuditLogDetails(Stream<AuditLogDetail> details, SOSHibernateSession connection, Long auditlogId, Date now) {
        if (details != null && auditlogId != null) {
            if (connection == null) {
                SOSHibernateSession connection2 = null;
                try {
                    connection2 = Globals.createSosHibernateStatelessConnection("storeAuditLogDetail");
                    storeAuditLogDetails(details, connection2, auditlogId, now);
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    Globals.disconnect(connection2);
                }
            } else {
                details.forEach(detail -> storeAuditLogDetail(detail.getAuditLogDetail(auditlogId, now), connection));
            }
        }
    }
    
    public static void storeAuditLogDetail(AuditLogDetail detail, SOSHibernateSession connection, DBItemJocAuditLog dbAudit) {
        storeAuditLogDetail(detail, connection, dbAudit.getId(), dbAudit.getCreated());
    }
    
    private static void storeAuditLogDetail(AuditLogDetail detail, SOSHibernateSession connection, Long auditlogId, Date now) {
        if (detail != null && auditlogId != null) {
            if (now == null) {
                now = Date.from(Instant.now());
            }
            if (connection == null) {
                try {
                    connection = Globals.createSosHibernateStatelessConnection("storeAuditLogDetail");
                    storeAuditLogDetail(detail.getAuditLogDetail(auditlogId, now), connection);
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    Globals.disconnect(connection);
                }
            } else {
                storeAuditLogDetail(detail.getAuditLogDetail(auditlogId, now), connection);
            }
        }
    }
    
    private static void storeAuditLogDetail(DBItemJocAuditLogDetails dbItem, SOSHibernateSession connection) {
        try {
            if (dbItem != null && dbItem.getAuditLogId() != 0L) {
                connection.save(dbItem);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
}
