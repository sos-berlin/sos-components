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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingCommentException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.configuration.permissions.ControllerPermissions;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

public class JOCResourceImpl {

    protected JobSchedulerUser jobschedulerUser;
    protected SOSAuthFolderPermissions folderPermissions;
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCResourceImpl.class);
//    private String accessToken;
    private String headerAccessToken;
    private JocAuditLog jocAuditLog;

    private JocError jocError = new JocError();

    protected void initGetPermissions(String accessToken) throws JocException {
        headerAccessToken = accessToken;
        if (jobschedulerUser == null) {
//            this.accessToken = SOSAuthHelper.getIdentityServiceAccessToken(accessToken);
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }

        updateUserInMetaInfo();
    }

    private String getMasterId(String masterId) throws SessionNotExistException {
        if (masterId == null) {
            masterId = "";
        }
        return masterId;
    }

    protected ControllerPermissions getControllerPermissions(String masterId, String accessToken) throws JocException {
        initGetPermissions(accessToken);
        masterId = getMasterId(masterId);
        return jobschedulerUser.getSOSAuthCurrentAccount().getControllerPermissions(masterId);
    }
    
    protected ControllerPermissions getControllerDefaultPermissions(String accessToken) throws JocException {
        initGetPermissions(accessToken);
        return jobschedulerUser.getSOSAuthCurrentAccount().getControllerDefaultPermissions();
    }

    protected JocPermissions getJocPermissions(String accessToken) throws JocException {
        initGetPermissions(accessToken);
        return jobschedulerUser.getSOSAuthCurrentAccount().getJocPermissions();
    }

    public String getAccessToken() {
        return headerAccessToken;
    }

    public String getAccessToken(String accessToken, String oldAccessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }
        return oldAccessToken;
    }

    public JobSchedulerUser getJobschedulerUser() {
        return jobschedulerUser;
    }

    public JocError getJocError() {
        return jocError;
    }

    public JocError getJocErrorWithPrintMetaInfoAndClear(Logger logger) {
        if(jocError != null && jocError.getMetaInfo() != null && !jocError.getMetaInfo().isEmpty()) {
            logger.info(jocError.printMetaInfo());
            jocError.getMetaInfo().clear();
        }
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

    public String normalizePath(String path) {
        return Globals.normalizePath(path);
    }

    public static String normalizeFolder(String path) {
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

    public static String getParent(String path) {
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

    public DBItemJocAuditLog storeAuditLog(AuditParams audit, CategoryType category) {
        checkRequiredComment(audit);
        DBItemJocAuditLog logDbItem = jocAuditLog.storeAuditLogEntry(audit, category.intValue());
        jocAuditLog.logAuditMessage(audit, logDbItem.getId());
        return logDbItem;
    }

    public DBItemJocAuditLog storeAuditLog(AuditParams audit, String controllerId, CategoryType category) {
        checkRequiredComment(audit);
        DBItemJocAuditLog logDbItem = jocAuditLog.storeAuditLogEntry(audit, controllerId, category.intValue());
        jocAuditLog.logAuditMessage(audit, logDbItem.getId());
        return logDbItem;
    }

    public DBItemJocAuditLog storeAuditLog(AuditParams audit, String controllerId, CategoryType category, SOSHibernateSession connection) {
        checkRequiredComment(audit);
        DBItemJocAuditLog logDbItem = jocAuditLog.storeAuditLogEntry(audit, controllerId, category.intValue(), connection);
        jocAuditLog.logAuditMessage(audit, logDbItem.getId());
        return logDbItem;
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

    public void initLogging(String request, byte[] body, String accessToken) throws JocException, JsonParseException, JsonMappingException,
            IOException {
        headerAccessToken = accessToken;
//        this.accessToken = SOSAuthHelper.getIdentityServiceAccessToken(accessToken);
        if (jobschedulerUser == null) {
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        initLogging(request, body);

        if (Globals.jocWebserviceDataContainer == null || Globals.jocWebserviceDataContainer.getCurrentAccountsList() == null) {
            throw new SessionNotExistException("Session is broken and no longer valid. New login is neccessary");
        }

        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccountByToken(
                accessToken);
        if (sosAuthCurrentAccountAnswer.getSessionTimeout() == 0L) {
            throw new SessionNotExistException("Session has expired. New login is neccessary");
        }
    }

    public void initLogging(String request, byte[] body) {
        String user;
        try {
            user = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
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
        }
        jocAuditLog = new JocAuditLog(user, request, bodyStr);
        if (bodyStr.length() > 4096) {
            bodyStr = bodyStr.substring(0, 4093) + "...";
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REQUEST: " + request + ", PARAMS: " + bodyStr);
        }
        jocError.addMetaInfoOnTop("\nREQUEST: " + request, "PARAMS: " + bodyStr, "USER: " + user);
        jocError.setApiCall(request);
    }

    public JOCDefaultResponse initPermissions(String controllerId, boolean permission) throws JocException {
        JOCDefaultResponse jocDefaultResponse = init401And440();

        if (!permission) {
            return accessDeniedResponse();
        }
        folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        folderPermissions.setSchedulerId(controllerId);
        return jocDefaultResponse;
    }

    private JOCDefaultResponse init401And440() {
        if (!jobschedulerUser.isAuthenticated()) {
            String apiCall = jocError == null ? null : jocError.getApiCall();
            return JOCDefaultResponse.responseStatus401(JOCDefaultResponse.getError401Schema(jobschedulerUser, apiCall));
        }
        return null;
    }

    private void updateUserInMetaInfo() {
        try {
            if (jocError != null) {
                String userMetaInfo = "USER: " + jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
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
        if (!SOSAuthFolderPermissions.isPermittedForFolder(folder, listOfFolders)) {
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
        return SOSAuthFolderPermissions.isPermittedForFolder(getParent(path), listOfFolders);
    }

    protected Set<Folder> addPermittedFolder(Collection<Folder> folders) {
        return folderPermissions.getPermittedFolders(folders);
    }

    protected static Set<Folder> addPermittedFolder(Collection<Folder> folders, SOSAuthFolderPermissions folderPermissions) {
        return folderPermissions.getPermittedFolders(folders);
    }

    protected static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        return SOSAuthFolderPermissions.isPermittedForFolder(folder, listOfFolders);
    }

    public String getAccount() {
        try {
            return jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
        } catch (Exception e) {
            return null;
        }
    }

}
