package com.sos.joc.classes.audit;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocAuditLogDetails;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;

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
        }
        return null;
    }
    
    public DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, String controllerId, CategoryType type) {
        return storeAuditLogEntry(audit, controllerId, type, null);
    }
    
    public DBItemJocAuditLog storeAuditLogEntry(AuditParams audit, CategoryType type) {
        return storeAuditLogEntry(audit, null, type, null);
    }
    
    public void storeAuditLogDetails(Collection<DBItemJocAuditLogDetails> dbItems) {
        storeAuditLogDetails(dbItems, null);
    }
    
    public void storeAuditLogDetails(Collection<DBItemJocAuditLogDetails> dbItems, SOSHibernateSession connection) {
        if (dbItems != null && !dbItems.isEmpty()) {
            if (connection == null) {
                try {
                    connection = Globals.createSosHibernateStatelessConnection("storeAuditLogDetail");
                    for (DBItemJocAuditLogDetails dbItem : dbItems) {
                        storeAuditLogDetail(dbItem, connection);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    Globals.disconnect(connection);
                }
            } else {
                for (DBItemJocAuditLogDetails dbItem : dbItems) {
                    storeAuditLogDetail(dbItem, connection);
                }
            }
        }
    }
    
    public void storeAuditLogDetail(DBItemJocAuditLogDetails dbItem, SOSHibernateSession connection) {
        try {
            connection.save(dbItem);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public void storeAuditLogDetail(SOSHibernateSession connection, DBItemJocAuditLog auditlogItem, Path path, ConfigurationType type) {
        try {
            connection.save(createAuditLogDetail(auditlogItem, path, type));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public void storeAuditLogDetail(SOSHibernateSession connection, Long auditlogId, Path path, ConfigurationType type, Date now) {
        try {
            connection.save(createAuditLogDetail(auditlogId, path, type, now));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public void storeAuditLogDetail(SOSHibernateSession connection, Long auditlogId, Path path, ConfigurationType type) {
        try {
            connection.save(createAuditLogDetail(auditlogId, path, type));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
    
    public DBItemJocAuditLogDetails createAuditLogDetail(DBItemJocAuditLog auditlogItem, Path path, ConfigurationType type) {
        return createAuditLogDetail(auditlogItem.getId(), path, type, auditlogItem.getCreated());
    }

    public DBItemJocAuditLogDetails createAuditLogDetail(Long auditlogId, Path path, ConfigurationType type) {
        return createAuditLogDetail(auditlogId, path, type, Date.from(Instant.now()));
    }
    
    public DBItemJocAuditLogDetails createAuditLogDetail(Long auditlogId, Path path, ConfigurationType type, Date now) {
        DBItemJocAuditLogDetails dbItem = new DBItemJocAuditLogDetails();
        dbItem.setId(null);
        dbItem.setPath(path.toString().replace('\\', '/'));
        dbItem.setName(path.getFileName().toString());
        dbItem.setFolder(path.getParent().toString().replace('\\', '/'));
        dbItem.setCreated(Date.from(Instant.now()));
        dbItem.setType(type);
        dbItem.setAuditLogId(auditlogId);
        return dbItem;
    }
}
