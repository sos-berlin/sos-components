package com.sos.joc.classes;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.SOSPermissionsCreator;
import com.sos.auth.rest.SOSShiroCurrentUserAnswer;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateInvalidSessionException;
import com.sos.joc.Globals;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingCommentException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.security.permissions.JocPermissions;

public class JOCResourceImpl {

    protected JobSchedulerUser jobschedulerUser;
    protected SOSShiroFolderPermissions folderPermissions;
    private static final String SHIRO_SESSION = "SHIRO_SESSION";
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCResourceImpl.class);
    private String accessToken;
    private JocAuditLog jocAuditLog;

    private JocError jocError = new JocError();

    protected void initGetPermissions(String accessToken) throws JocException, InvalidSessionException {
        if (jobschedulerUser == null) {
            this.accessToken = accessToken;
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
        try {
            sosPermissionsCreator.loginFromAccessToken(accessToken);
        } catch (IOException e) {
            throw new JocException(e);
        }

        updateUserInMetaInfo();
    }

    private String getMasterId(String masterId) throws SessionNotExistException {
        // TODO why we need this?
        // it should be part of the webservice if the controllerId is required or not
        // Here, the last selected controllId is returnd in the case where not no controllerId is given.
        // I think that's wrong
        // if (masterId == null || masterId.isEmpty()) {
        // SOSShiroSession sosShiroSession = new SOSShiroSession(jobschedulerUser.getSosShiroCurrentUser());
        // masterId = sosShiroSession.getStringAttribute(SESSION_KEY);
        // }
        if (masterId == null) {
            masterId = "";
        }
        return masterId;
    }

    protected ControllerPermissions getControllerPermissions(String masterId, String accessToken) throws JocException, InvalidSessionException {
        initGetPermissions(accessToken);
        masterId = getMasterId(masterId);
        return jobschedulerUser.getSosShiroCurrentUser().getControllerPermissions(masterId);
    }
    
    protected JocPermissions getJocPermissions(String accessToken) throws JocException, InvalidSessionException {
        initGetPermissions(accessToken);
        return jobschedulerUser.getSosShiroCurrentUser().getJocPermissions();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessToken(String xAccessToken, String oldAccessToken) {
        if (xAccessToken != null && !xAccessToken.isEmpty()) {
            return xAccessToken;
        }
        return oldAccessToken;
    }

    public JobSchedulerUser getJobschedulerUser() {
        return jobschedulerUser;
    }

    public JobSchedulerUser getJobschedulerUser(String accessToken) {
        if (jobschedulerUser == null) {
            this.accessToken = accessToken;
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        return jobschedulerUser;
    }

    public JocError getJocError() {
        return jocError;
    }

    public Date getDateFromString(String dateString) {
        Date date = null;
        try {
            dateString = dateString.trim().replaceFirst("^(\\d{4}-\\d{2}-\\d{2}) ", "$1T");
            date = Date.from(Instant.parse(dateString));
        } catch (Exception e) {
            // TODO what should we do with this exception?
            // jocError = new JocError("JOC-420","Could not parse date: " +
            // dateString);
            LOGGER.warn("Could not parse date: " + dateString, e);
        }
        return date;
    }

    public Date getDateFromTimestamp(Long timeStamp) {
        Instant fromEpochMilli = Instant.ofEpochMilli(timeStamp / 1000);
        return Date.from(fromEpochMilli);
    }

    public JOCDefaultResponse init(String request, Object body, String accessToken, String controllerId, boolean permission) throws JocException,
            InvalidSessionException, JsonParseException, JsonMappingException, IOException {
        this.accessToken = accessToken;
        if (jobschedulerUser == null) {
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        initLogging(request, body);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
        sosPermissionsCreator.loginFromAccessToken(accessToken);

        return initPermissions(controllerId, permission);
    }

    public String normalizePath(String path) {
        return Globals.normalizePath(path);
    }

    public String normalizeFolder(String path) {
        if (path == null) {
            return null;
        }
        return ("/" + path.trim()).replaceAll("//+", "/");
    }

    public boolean checkRequiredComment(AuditParams auditParams) throws JocMissingCommentException {
        if (ClusterSettings.getForceCommentsForAuditLog(Globals.getConfigurationGlobalsJoc())) {
            String comment = null;
            if (auditParams != null) {
                comment = auditParams.getComment();
            }
            if (comment == null || comment.isEmpty()) {
                throw new JocMissingCommentException();
            }
        }
        return true;
    }

    public boolean checkRequiredParameter(String paramKey, String paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.isEmpty()) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public boolean checkRequiredParameter(String paramKey, Long paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public boolean checkRequiredParameter(String paramKey, Object paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public boolean checkRequiredParameter(String paramKey, URI paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.toString().isEmpty()) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public boolean checkRequiredParameter(String paramKey, Collection<?> paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.isEmpty()) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public boolean checkRequiredParameter(String paramKey, Integer paramVal) throws JocMissingRequiredParameterException {
        return checkRequiredParameter(paramKey, String.valueOf(paramVal));
    }

    protected static String getParent(String path) {
        Path p = Paths.get(path).getParent();
        if (p == null) {
            return "/";
        } else {
            return p.toString().replace('\\', '/');
        }
    }

    protected boolean matchesRegex(Pattern p, String path) {
        if (p != null) {
            return p.matcher(path).find();
        } else {
            return true;
        }
    }

    public JocAuditLog getJocAuditLog() {
        return jocAuditLog;
    }
    
    public void logAuditMessage(AuditParams audit) {
        jocAuditLog.logAuditMessage(audit);
    }
    
    public DBItemJocAuditLog storeAuditLog(AuditParams audit, CategoryType category) {
        checkRequiredComment(audit);
        jocAuditLog.logAuditMessage(audit);
        return jocAuditLog.storeAuditLogEntry(audit, category.intValue());
    }
    
    public DBItemJocAuditLog storeAuditLog(AuditParams audit, String controllerId, CategoryType category) {
        checkRequiredComment(audit);
        jocAuditLog.logAuditMessage(audit);
        return jocAuditLog.storeAuditLogEntry(audit, controllerId, category.intValue());
    }
    
    public DBItemJocAuditLog storeAuditLog(AuditParams audit, String controllerId, CategoryType category, SOSHibernateSession connection) {
        checkRequiredComment(audit);
        jocAuditLog.logAuditMessage(audit);
        return jocAuditLog.storeAuditLogEntry(audit, controllerId, category.intValue(), connection);
    }
    
    public void storeAuditLogDetails(Collection<AuditLogDetail> details, DBItemJocAuditLog dbAuditLog) {
        storeAuditLogDetails(details, null, dbAuditLog);
    }
    
    public void storeAuditLogDetails(Collection<AuditLogDetail> details, SOSHibernateSession connection, DBItemJocAuditLog dbAuditLog) {
        if (dbAuditLog != null) {
            JocAuditLog.storeAuditLogDetails(details, connection, dbAuditLog.getId(), dbAuditLog.getCreated());
        }
    }

    public String getJsonString(Object body) {
        if (body != null) {
            try {
                return Globals.objectMapper.writeValueAsString(body);
            } catch (Exception e) {
                return body.toString();
            }
        }
        return "-";
    }

    public JOCDefaultResponse accessDeniedResponse() {
        return accessDeniedResponse("Access denied");
    }

    public JOCDefaultResponse accessDeniedResponse(String message) {
        jocError.setMessage(message);
        return JOCDefaultResponse.responseStatus403(JOCDefaultResponse.getError401Schema(jobschedulerUser, jocError));
    }

    private boolean sessionExistInDb(String sessionIdString) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("JOCResourceImpl");

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();

            filter.setAccount(".");
            filter.setName(sessionIdString);
            filter.setConfigurationType(SHIRO_SESSION);

            return jocConfigurationDBLayer.jocConfigurationExists(filter);
        } catch (SOSHibernateInvalidSessionException e) {
            throw new DBConnectionRefusedException(e);
        } catch (SOSHibernateException e) {
            throw new DBInvalidDataException(e);
        } catch (JocException e) {
            throw e;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void initLogging(String request, byte[] body, String accessToken) throws JocException, InvalidSessionException, JsonParseException,
            JsonMappingException, IOException {
        this.accessToken = accessToken;
        if (jobschedulerUser == null) {
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        initLogging(request, body);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
        sosPermissionsCreator.loginFromAccessToken(accessToken);

        if (Globals.jocWebserviceDataContainer != null && Globals.jocWebserviceDataContainer.getCurrentUsersList() != null) {
            SessionKey s = new DefaultSessionKey(accessToken);
            Session session = null;
            session = SecurityUtils.getSecurityManager().getSession(s);

            if (session != null && "true".equals(session.getAttribute("dao"))) {
                if (!sessionExistInDb(accessToken)) {
                    Globals.jocWebserviceDataContainer.getCurrentUsersList().removeUser(accessToken);
                }
            }
        }

        if (Globals.jocWebserviceDataContainer == null || Globals.jocWebserviceDataContainer.getCurrentUsersList() == null) {
            throw new SessionNotExistException("Session is broken and no longer valid. New login is neccessary");
        }

        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = Globals.jocWebserviceDataContainer.getCurrentUsersList().getUserByToken(accessToken);
        if (sosShiroCurrentUserAnswer.getSessionTimeout() == 0L) {
            throw new SessionNotExistException("Session has expired. New login is neccessary");
        }

        if (jobschedulerUser == null) {
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }

    }

    private void initLogging(String request, Object body) {
        String user;
        try {
            user = jobschedulerUser.getSosShiroCurrentUser().getUsername().trim();
        } catch (Exception e) {
            user = "-";
        }
        if (request == null || request.isEmpty()) {
            request = "-";
        }
        String bodyStr = getJsonString(body);
        if (bodyStr != null) {
            if (bodyStr.length() > 4096) {
                bodyStr = bodyStr.substring(0, 4093) + "...";
            }
        }
        jocAuditLog = new JocAuditLog(user, request, bodyStr);
        LOGGER.debug("REQUEST: " + request + ", PARAMS: " + bodyStr);
        jocError.addMetaInfoOnTop("\nREQUEST: " + request, "PARAMS: " + bodyStr, "USER: " + user);
    }

    public void initLogging(String request, byte[] body) {
        String user;
        try {
            user = jobschedulerUser.getSosShiroCurrentUser().getUsername().trim();
        } catch (Exception e) {
            user = "-";
        }
        if (request == null || request.isEmpty()) {
            request = "-";
        }
        String bodyStr = "-";
        if (body != null) {
            try {
                // eliminate possibly pretty print from origin request
                bodyStr = Globals.objectMapper.writeValueAsString(Globals.objectMapper.readValue(body, Object.class));
            } catch (Exception e) {
                bodyStr = new String(body, StandardCharsets.UTF_8);
            }
            if (bodyStr.length() > 4096) {
                bodyStr = bodyStr.substring(0, 4093) + "...";
            }
        }
        
        jocAuditLog = new JocAuditLog(user, request, bodyStr);
        LOGGER.debug("REQUEST: " + request + ", PARAMS: " + bodyStr);
        jocError.addMetaInfoOnTop("\nREQUEST: " + request, "PARAMS: " + bodyStr, "USER: " + user);
    }

    public JOCDefaultResponse initPermissions(String controllerId, boolean permission) throws JocException {
        JOCDefaultResponse jocDefaultResponse = init401And440();

        if (!permission) {
            return accessDeniedResponse();
        }
        folderPermissions = jobschedulerUser.getSosShiroCurrentUser().getSosShiroFolderPermissions();
        folderPermissions.setSchedulerId(controllerId);
        return jocDefaultResponse;
    }

    private JOCDefaultResponse init401And440() {
        if (!jobschedulerUser.isAuthenticated()) {
            return JOCDefaultResponse.responseStatus401(JOCDefaultResponse.getError401Schema(jobschedulerUser));
        }
        return null;
    }

    private void updateUserInMetaInfo() {
        try {
            if (jocError != null) {
                String userMetaInfo = "USER: " + jobschedulerUser.getSosShiroCurrentUser().getUsername();
                List<String> metaInfo = jocError.getMetaInfo();
                if (metaInfo.size() > 2) {
                    metaInfo.remove(2);
                    metaInfo.add(2, userMetaInfo);
                }
            }
        } catch (Exception e) {
        }
    }

    protected void checkFolderPermissions(String path) throws JocFolderPermissionsException {
        String folder = getParent(path);
        if (!folderPermissions.isPermittedForFolder(folder)) {
            throw new JocFolderPermissionsException(folder);
        }
    }

    protected static void checkFolderPermissions(String path, Collection<Folder> listOfFolders) throws JocFolderPermissionsException {
        String folder = getParent(path);
        if (!SOSShiroFolderPermissions.isPermittedForFolder(folder, listOfFolders)) {
            throw new JocFolderPermissionsException(folder);
        }
    }

    public static boolean canAdd(String path, Set<Folder> listOfFolders) {
        if (path == null || !path.startsWith("/")) {
            return false;
        }
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            return true;
        }
        return SOSShiroFolderPermissions.isPermittedForFolder(getParent(path), listOfFolders);
    }

    protected Set<Folder> addPermittedFolder(Collection<Folder> folders) {
        return folderPermissions.getPermittedFolders(folders);
    }
    
    protected static Set<Folder> addPermittedFolder(Collection<Folder> folders, SOSShiroFolderPermissions folderPermissions) {
        return folderPermissions.getPermittedFolders(folders);
    }
    
    protected static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        return SOSShiroFolderPermissions.isPermittedForFolder(folder, listOfFolders);
    }

    public String getAccount() {
        try {
            return jobschedulerUser.getSosShiroCurrentUser().getUsername().trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    

  
}
