package com.sos.joc.classes;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.auth.rest.SOSPermissionsCreator;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.joc.Globals;
import com.sos.joc.classes.audit.IAuditLog;
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
import com.sos.joc.model.common.Folder;

public class JOCResourceImpl {

    protected JobSchedulerUser jobschedulerUser;
    protected SOSShiroFolderPermissions folderPermissions;
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCResourceImpl.class);
    private String accessToken;
    private JocAuditLog jocAuditLog;

    private JocError jocError = new JocError();

    protected void initGetPermissions(String accessToken) throws JocException {
        if (jobschedulerUser == null) {
            this.accessToken = accessToken;
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
        sosPermissionsCreator.loginFromAccessToken(accessToken);

        updateUserInMetaInfo();
    }

    private String getMasterId(String masterId) throws SessionNotExistException {
        // TODO why we need this?
        // it should be part of the webservice if the controllerId is required or not
        // Here, the last selected controllId is returnd in the case where not no controllerId is given.
        // I think that's wrong
//        if (masterId == null || masterId.isEmpty()) {
//            SOSShiroSession sosShiroSession = new SOSShiroSession(jobschedulerUser.getSosShiroCurrentUser());
//            masterId = sosShiroSession.getStringAttribute(SESSION_KEY);
//        }
        if (masterId == null) {
            masterId = "";
        }
        return masterId;
    }

    protected SOSPermissionJocCockpit getPermissonsJocCockpit(String masterId, String accessToken) throws JocException {
        initGetPermissions(accessToken);
        masterId = getMasterId(masterId);
        return jobschedulerUser.getSosShiroCurrentUser().getSosPermissionJocCockpit(masterId);
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

    public JOCDefaultResponse init(String request, Object body, String accessToken, String controllerId, boolean permission) throws JocException {
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

    public boolean checkRequiredComment(String comment) throws JocMissingCommentException {
        if (ClusterSettings.getForceCommentsForAuditLog(Globals.getConfigurationGlobalsJoc())) {
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

    public boolean checkRequiredParameter(String paramKey, List<?> paramVal) throws JocMissingRequiredParameterException {
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
            return null;
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

    public void logAuditMessage(IAuditLog body) {
        jocAuditLog.logAuditMessage(body);
    }

    public DBItemJocAuditLog storeAuditLogEntry(IAuditLog body) {
        return jocAuditLog.storeAuditLogEntry(body);
    }

    public String getJsonString(Object body) {
        if (body != null) {
            try {
                return new ObjectMapper().writeValueAsString(body);
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

    public void initLogging(String request, byte[] body, String accessToken) throws JocException {
        this.accessToken = accessToken;
        if (jobschedulerUser == null) {
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        initLogging(request, body);
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
        sosPermissionsCreator.loginFromAccessToken(accessToken);
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
        jocAuditLog = new JocAuditLog(user, request);
        LOGGER.debug("REQUEST: " + request + ", PARAMS: " + getJsonString(body));
        jocError.addMetaInfoOnTop("\nREQUEST: " + request, "PARAMS: " + getJsonString(body), "USER: " + user);
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
            bodyStr = new String(body, StandardCharsets.UTF_8);
        }
        jocAuditLog = new JocAuditLog(user, request);
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

    protected static boolean canAdd(String path, Set<Folder> listOfFolders) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return SOSShiroFolderPermissions.isPermittedForFolder(getParent(path), listOfFolders);
    }

    protected Set<Folder> addPermittedFolder(Collection<Folder> folders) {
        return folderPermissions.getPermittedFolders(folders);
    }

    public String getAccount() {
        try {
            return jobschedulerUser.getSosShiroCurrentUser().getUsername().trim();
        } catch (Exception e) {
            return null;
        }
    }

}
