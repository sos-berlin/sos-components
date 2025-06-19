package com.sos.joc.classes;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.JocAuditTrail;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingCommentException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.configuration.permissions.ControllerPermissions;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.model.security.foureyes.FourEyesResponse;
import com.sos.joc.model.security.foureyes.RequestBody;
import com.sos.joc.model.security.foureyes.RequestorState;

import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

public class JOCResourceImpl {

    protected JobSchedulerUser jobschedulerUser;
    protected SOSAuthFolderPermissions folderPermissions;
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCResourceImpl.class);
//    private String accessToken;
    private String headerAccessToken;
    private JocAuditTrail jocAuditLog = new JocAuditTrail();
    
    @HeaderParam("X-Approval-Request-Id")
    private String approvalRequestId;
    
    @Context 
    private HttpServletRequest httpRequest;
    
    private byte[] origBody;

    private JocError jocError = new JocError();

    protected void initGetPermissions(String accessToken) throws JocException {
        headerAccessToken = accessToken;
        if (jobschedulerUser == null) {
//            this.accessToken = SOSAuthHelper.getIdentityServiceAccessToken(accessToken);
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }

        updateUserInMetaInfo();
    }

    private String getControllerId(String controllerId) throws SessionNotExistException {
        if (controllerId == null) {
            controllerId = "";
        }
        return controllerId;
    }
    
    protected ControllerPermissions getBasicControllerDefaultPermissions(String accessToken) throws JocException {
        initGetPermissions(accessToken);
        return jobschedulerUser.getSOSAuthCurrentAccount().getControllerDefaultPermissions();
    }

    protected ControllerPermissions getBasicControllerPermissions(String controllerId, String accessToken) throws JocException {
        initGetPermissions(accessToken);
        controllerId = getControllerId(controllerId);
        return jobschedulerUser.getSOSAuthCurrentAccount().getControllerPermissions(controllerId);
    }

    protected JocPermissions getBasicJocPermissions(String accessToken) throws JocException {
        initGetPermissions(accessToken);
        return jobschedulerUser.getSOSAuthCurrentAccount().getJocPermissions();
    }
    
    protected Stream<ControllerPermissions> getControllerPermissions(String controllerId, String accessToken) throws JocException {
        initGetPermissions(accessToken);
        controllerId = getControllerId(controllerId);
        return Stream.of(jobschedulerUser.getSOSAuthCurrentAccount().getControllerPermissions(controllerId), jobschedulerUser
                .getSOSAuthCurrentAccount().get4EyesControllerPermissions(controllerId));
    }
    
    protected ControllerPermissions get4EyesControllerPermissions(String controllerId) throws JocException {
        controllerId = getControllerId(controllerId);
        return jobschedulerUser.getSOSAuthCurrentAccount().get4EyesControllerPermissions(controllerId);
    }
    
    protected Stream<ControllerPermissions> getControllerDefaultPermissions(String accessToken) throws JocException {
        initGetPermissions(accessToken);
        return Stream.of(jobschedulerUser.getSOSAuthCurrentAccount().getControllerDefaultPermissions(), jobschedulerUser.getSOSAuthCurrentAccount()
                .get4EyesControllerDefaultPermissions());
    }
    
    protected Stream<JocPermissions> getJocPermissions(String accessToken) throws JocException {
        initGetPermissions(accessToken);
        return Stream.of(jobschedulerUser.getSOSAuthCurrentAccount().getJocPermissions(), jobschedulerUser.getSOSAuthCurrentAccount()
                .get4EyesJocPermissions());
    }
    
    protected JocPermissions get4EyesJocPermissions() throws JocException {
        return jobschedulerUser.getSOSAuthCurrentAccount().get4EyesJocPermissions();
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
    
    public JocAuditTrail getJocAuditTrail() {
        return jocAuditLog;
    }
    
//    public void logAuditTrail() {
//        jocAuditLog.log();
//    }

    public DBItemJocAuditLog storeAuditLog(AuditParams audit) {
        checkRequiredComment(audit);
        DBItemJocAuditLog logDbItem = jocAuditLog.storeAuditLogEntry(audit);
        jocAuditLog.logAuditMessage(audit, logDbItem.getId());
        return logDbItem;
    }

    public DBItemJocAuditLog storeAuditLog(AuditParams audit, String controllerId) {
        checkRequiredComment(audit);
        DBItemJocAuditLog logDbItem = jocAuditLog.storeAuditLogEntry(audit, controllerId);
        jocAuditLog.logAuditMessage(audit, logDbItem.getId());
        return logDbItem;
    }

    public DBItemJocAuditLog storeAuditLog(AuditParams audit, String controllerId, SOSHibernateSession connection) {
        checkRequiredComment(audit);
        DBItemJocAuditLog logDbItem = jocAuditLog.storeAuditLogEntry(audit, controllerId, connection);
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
    
    public JOCDefaultResponse approvalRequestResponse() {
        return approvalRequestResponse("4-eyes principle: Operation needs approval process");
    }
    
    public JOCDefaultResponse approvalRequestResponse(String message) {
        if (approvalRequestId == null || !approvalRequestId.trim().matches("\\d+") || approvalRequestId.trim().equals("0")) {
            return JOCDefaultResponse.responseStatus433(getError433Schema(message), jocAuditLog);
        } else {
            // already checked in initLooging method
            // Long fEyesId = Long.valueOf(fourEyesId.trim());
            // check dataset of fEyesId: 
            // 1. db.accountName == current accountName
            // 2. db.url == current url
            // 3. db.approved == true
            // 4. db.request == current request ??? or use db.request instead current request ???
            return null;
        }
    }
    
    private byte[] getError433Schema(String message) {
        SOSHibernateSession session = null;
        try {
            FourEyesResponse entity = new FourEyesResponse();
            entity.setRequestor(jocAuditLog.getUser());
            entity.setRequestUrl(jocAuditLog.getRequest());
            if (origBody == null) {
                // it could be that jocAuditLog.getParams() is a masked body (without password etc.)
                entity.setRequestBody(Globals.objectMapper.readValue(jocAuditLog.getParams(), RequestBody.class));
            } else {
                entity.setRequestBody(Globals.objectMapper.readValue(origBody, RequestBody.class));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setMessage(message);
            entity.setCategory(jocAuditLog.getCategory());
            session = Globals.createSosHibernateStatelessConnection("getApprovers");
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            entity.setApprovers(dbLayer.getApprovers().stream().map(DBItemJocApprover::mapToApproverWithoutEmail).toList());
            if (entity.getApprovers().isEmpty()) {
                throw new JocBadRequestException(message + " but no approvers are defined");
            }
            return Globals.objectMapper.writeValueAsBytes(entity);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocException(e);
        } finally {
            Globals.disconnect(session);
        }
    }

    public JOCDefaultResponse accessDeniedResponse(String message) {
        jocError.setMessage(message);
        return responseStatus403(JOCDefaultResponse.getError401Schema(jobschedulerUser, jocError));
    }
    
    public JOCDefaultResponse accessDeniedResponseByUnsupported4EyesPrinciple() {
        String approvalRequestorRole = Globals.getConfigurationGlobalsJoc().getApprovalRequestorRole().getValue();
        String message = String.format(
                "An approval should be requested according to the permissions configuration '%s' (Role: %s) but this is unsupported. Access denied.",
                "joc:adminstration:accounts:manage", approvalRequestorRole);
        jocError.setMessage(message);
        return responseStatus403(JOCDefaultResponse.getError401Schema(jobschedulerUser, jocError));
    }
    
    public byte[] initLogging(String request, byte[] maskedBody, byte[] originBody, String accessToken, CategoryType category) throws Exception {
        origBody = originBody;
        return initLogging(request, maskedBody, accessToken, category);
    }
    
    public byte[] initLogging(String request, byte[] body, String accessToken, CategoryType category) throws Exception {
        headerAccessToken = accessToken;
//        this.accessToken = SOSAuthHelper.getIdentityServiceAccessToken(accessToken);
        if (jobschedulerUser == null) {
            jobschedulerUser = new JobSchedulerUser(accessToken);
        }
        body = initLogging2(request, body, accessToken, category);

        if (Globals.jocWebserviceDataContainer == null || Globals.jocWebserviceDataContainer.getCurrentAccountsList() == null) {
            throw new SessionNotExistException("Session is broken and no longer valid. New login is neccessary");
        }

        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccountByToken(
                accessToken);
        if (sosAuthCurrentAccountAnswer.getSessionTimeout() == 0L) {
            throw new SessionNotExistException("Session has expired. New login is neccessary");
        }
        
        return body;
    }
    
    public byte[] initLogging(String request, byte[] body, CategoryType category) throws Exception {
        return initLogging2(request, body, null, category);
    }

    private byte[] initLogging2(String request, byte[] body, String accessToken, CategoryType category) throws Exception {
        String user;
        try {
            user = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
        } catch (Exception e) {
            user = "-";
        }
        if (request == null || request.isEmpty()) {
            request = "-";
        }
        
        Either<Exception, byte[]> fourEyesBody = getApprovalRequestBody(request, user, body);
        if (fourEyesBody.isRight()) {
            body = fourEyesBody.get();
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
//        Optional<String> ipAddress = Optional.ofNullable(jobschedulerUser).map(u -> {
//            try {
//                return u.getSOSAuthCurrentAccount();
//            } catch (Exception e) {
//                return null;
//            }
//        }).map(SOSAuthCurrentAccount::getCallerIpAddress);
        Optional<String> ipAddress = Optional.ofNullable(httpRequest).map(HttpServletRequest::getRemoteAddr);
        jocAuditLog = new JocAuditTrail(user, request, bodyStr, Optional.ofNullable(accessToken), ipAddress, category);

        if (bodyStr.length() > 4096) {
            bodyStr = bodyStr.substring(0, 4093) + "...";
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("REQUEST: " + request + ", PARAMS: " + bodyStr);
        }
        jocError.addMetaInfoOnTop("\nREQUEST: " + request, "PARAMS: " + bodyStr, "USER: " + user);
        jocError.setApiCall(request);
        
        if (fourEyesBody.isLeft()) {
            throw fourEyesBody.getLeft();
        }
        
        return body;
    }
    
    private Either<Exception, byte[]> getApprovalRequestBody(String request, String user, byte[] body) {
        Either<Exception, byte[]> either = Either.right(body);
        if (approvalRequestId != null && approvalRequestId.trim().matches("\\d+") && !approvalRequestId.trim().equals("0")) {
            SOSHibernateSession hibernateSession = null;
            try {
                hibernateSession = Globals.createSosHibernateStatelessConnection(request);
                hibernateSession.setAutoCommit(false);
                Long aRId = Long.valueOf(approvalRequestId.trim());
                DBItemJocApprovalRequest item = hibernateSession.get(DBItemJocApprovalRequest.class, aRId);
                if (item == null) {
                    throw new JocAccessDeniedException("Approval request: Couldn't find request");
                }
                if (!item.getRequestor().equals(user)) {
                    throw new JocAccessDeniedException("Approval request: wrong requestor");
                }
                if (!item.getRequest().equals(request)) {
                    throw new JocAccessDeniedException("Approval request: wrong requested URL");
                }
                switch (item.getApproverStateAsEnum()) {
                case PENDING:
                    throw new JocAccessDeniedException("Approval request: request is not approved");
                case APPROVED: // expected state
                    break;
                case REJECTED:
                    throw new JocAccessDeniedException("Approval request: request is rejected");
                }
                switch (item.getRequestorStateAsEnum()) {
                case REQUESTED: // expected state
                    break;
                case WITHDRAWN:
                    throw new JocAccessDeniedException("Approval request: request has already been withdrawn");
                case EXECUTED:
                    throw new JocAccessDeniedException("Approval request: approved request is already in progress");
                }
                
                new ApprovalDBLayer(hibernateSession).updateRequestorStatusInclusiveTransaction(aRId, RequestorState.EXECUTED);
                EventBus.getInstance().post(new ApprovalUpdatedEvent());

                either = Either.right(item.getParameters() == null ? null : item.getParameters().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                either = Either.left(e);
            } finally {
                Globals.disconnect(hibernateSession);
            }
        }
        return either;
    }

    public JOCDefaultResponse initPermissions(String controllerId, boolean permission) throws JocException {
        return initPermissions(controllerId, permission, false);
    }
    
    public JOCDefaultResponse initPermissions(String controllerId, Stream<Boolean> permissions) throws JocException {
        return initPermissions(controllerId, permissions.toList());
    }
    
    public JOCDefaultResponse initManageAccountPermissions(String accessToken) throws JocException {
        List<Boolean> perms = getJocPermissions(accessToken).map(p -> p.getAdministration().getAccounts().getManage()).toList();
        return initPermissions(null, perms.get(0), perms.get(1), true);
    }
    
    public JOCDefaultResponse initPermissions(String controllerId, List<Boolean> permissions) throws JocException {
        return initPermissions(controllerId, permissions.get(0), permissions.get(1));
    }
    
    @SafeVarargs
    public final JOCDefaultResponse initOrPermissions(String controllerId, Stream<Boolean>... permissions) throws JocException {
        return initPermissions(controllerId, orPermissions(permissions));
    }
    
    @SafeVarargs
    public final List<Boolean> orPermissions(Stream<Boolean>... permissions) throws JocException {
        // p wird verordert; p4eyes wird verundet
        boolean p = false;
        boolean p4eyes = true;
        for (Stream<Boolean> perms : permissions) {
            List<Boolean> pList = perms.toList();
            // pList.get(0): normal permissions; pList.get(1): 4-eyes permissions 
            if (pList.isEmpty()) {
                continue;
            }
            if (!p) {
                p = pList.get(0);
            }
            if (!pList.get(1)) {
                p4eyes = false; 
            }
        }
        return Arrays.asList(p, p4eyes);
    }
    
    @SafeVarargs
    public final JOCDefaultResponse initAndPermissions(String controllerId, Stream<Boolean>... permissions) throws JocException {
        return initPermissions(controllerId, andPermissions(permissions));
    }
    
    @SafeVarargs
    public final List<Boolean> andPermissions(Stream<Boolean>... permissions) throws JocException {
        if (permissions.length == 0) {
            return Arrays.asList(false, true);
        }
        // p wird verundet; p4eyes wird verordert
        boolean p = true;
        boolean p4eyes = false;
        for (Stream<Boolean> perms : permissions) {
            List<Boolean> pList = perms.toList();
            // pList.get(0): normal permissions; pList.get(1): 4-eyes permissions 
            if (pList.isEmpty()) {
                continue;
            }
            if (!pList.get(0)) {
                p = false; 
            }
            if (!p4eyes) {
                p4eyes = pList.get(1);
            }
        }
        return Arrays.asList(p, p4eyes);
    }
    
    public JOCDefaultResponse initPermissions(String controllerId, boolean permission, boolean fourEyesPermission) throws JocException {
        return initPermissions(controllerId, permission, fourEyesPermission, false);
    }
    
    private JOCDefaultResponse initPermissions(String controllerId, boolean permission, boolean fourEyesPermission, boolean unsupported4eyes)
            throws JocException {
        JOCDefaultResponse jocDefaultResponse = null;
        
        if (!jobschedulerUser.isAuthenticated()) {
            return responseStatus401(JOCDefaultResponse.getError401Schema(jobschedulerUser, jocError));
        }

        if (!permission) {
            return accessDeniedResponse();
        }
        if (fourEyesPermission) {
            // check if approver are configured -> otherwise ignore fourEyesPermission?
            if (unsupported4eyes) {
                return accessDeniedResponseByUnsupported4EyesPrinciple();
            }
            jocDefaultResponse = approvalRequestResponse();
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
        }
        folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        folderPermissions.setSchedulerId(controllerId);
        return jocDefaultResponse;
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
    
    
//    public JOCDefaultResponse responseStatus200(byte[] entity, Map<String, Object> headers) {
//        jocAuditLog.setResponse(entity);
//        return JOCDefaultResponse.responseStatus200(entity, MediaType.APPLICATION_JSON, headers, jocAuditLog);
//    }
    
    public JOCDefaultResponse responseStatus200(byte[] entity, String mediaType) {
        /** no response iff
         * application/pdf
         * application/octet-stream
         * image/*
         */
        jocAuditLog.setResponse(entity);
        return JOCDefaultResponse.responseStatus200(entity, mediaType, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatus200(byte[] entity) {
        return responseStatus200(entity, MediaType.APPLICATION_JSON);
    }
    
    public JOCDefaultResponse responseOctetStreamDownloadStatus200(StreamingOutput entity, String downloadFileName) {
        return responseOctetStreamDownloadStatus200(entity, downloadFileName, null);
    }
    
    public JOCDefaultResponse responseOctetStreamDownloadStatus200(StreamingOutput entity, String downloadFileName, Long uncompressedLength) {
        return JOCDefaultResponse.responseOctetStreamDownloadStatus200(entity, downloadFileName, uncompressedLength, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatusJSOk(Date surveyDate) {
        return JOCDefaultResponse.responseStatusJSOk(surveyDate, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatusJSError(Throwable e) {
        return responseStatusJSError(e, MediaType.APPLICATION_JSON);
    }
    
    public JOCDefaultResponse responseStatusJSError(Throwable e, String mediaType) {
        return JOCDefaultResponse.responseStatusJSError(e, jocError, mediaType, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatus434JSError(JocException e) {
        return responseStatus434JSError(e, false);
    }
    
    public JOCDefaultResponse responseStatus434JSError(JocException e, boolean withoutLogging) {
        e.addErrorMetaInfo(jocError);
        return JOCDefaultResponse.responseStatus434JSError(e, withoutLogging, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatus419(List<Err419> listOfErrors) {
        return JOCDefaultResponse.responseStatus419(listOfErrors, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatus200(SOSAuthCurrentAccountAnswer entity) {
        return JOCDefaultResponse.responseStatus200(entity, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatus401(SOSAuthCurrentAccountAnswer entity) {
        return JOCDefaultResponse.responseStatus401(entity, jocAuditLog);
    }
    
    public JOCDefaultResponse responseStatus403(SOSAuthCurrentAccountAnswer entity) {
        return JOCDefaultResponse.responseStatus403(entity, jocAuditLog);
    }

}
