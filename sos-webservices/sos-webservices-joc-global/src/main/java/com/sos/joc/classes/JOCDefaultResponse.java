package com.sos.joc.classes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;
import com.sos.joc.classes.audit.JocAuditTrail;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Err420;
import com.sos.joc.model.common.Errs;
import com.sos.joc.model.common.Ok;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

public class JOCDefaultResponse extends com.sos.joc.classes.ResponseWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOCDefaultResponse.class);
    private static final String TIMEOUT = "X-JOC-Timeout";
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
    private static final String ERROR_HTML = "<!DOCTYPE html>%n" + "<html>%n" + "<head>%n" + "  <title>%1$s</title>%n"
            + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>%n"
            + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>%n" + "  <style type=\"text/css\">%n"
            + "      div.frame{margin:16px auto 0;max-width:900px;min-height:100%%;height:auto !important;background-color:#eee;padding:2px 0;border-radius:16px;border:solid 2px #007da6;}%n"
            + "      div.innerframe{margin:0 auto;max-width:748px;text-align:center;font-family:\"Open Sans\",\"lucida grande\",\"Segoe UI\",arial,verdana,\"lucida sans unicode\",tahoma,serif;text-rendering:optimizeLegibility;}%n"
            + "      div.code {color:#eb8814;font-size:18pt;font-weight:bold;text-align:center;}%n"
            + "      div.message {color:#666;font-size:12pt;font-weight:normal;text-align:left;margin:4px 10px;}%n" + "  </style>%n" + "</head>%n"
            + "<body>%n" + "  <div class=\"frame\">%n" + "    <div class=\"innerframe\">%n" + "      <div class=\"code\">&#x26a0; %1$s<div>%n"
            + "      <div class=\"message\">%2$s</div>%n" + "    </div>%n" + "  </div>%n" + "</body>%n" + "</html>%n";
    private static final String REASON_PHRASE_420 = "Invalid Request";
    private static final String REASON_PHRASE_440 = "Login Timeout";
    
    private JOCDefaultResponse(Response delegate) {
        super(delegate);
    }

    public static JOCDefaultResponse responseStatus200(Object entity, String mediaType, Map<String, Object> headers, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", mediaType).cacheControl(setNoCaching());

        jocAuditTrail.addContentTypeHeader(mediaType);
        if (headers != null) {
            headers.keySet().stream().filter(s -> headers.get(s) != null).forEach(s -> {
                responseBuilder.header(s, headers.get(s));
                if (!s.equals(X_ACCESS_TOKEN)) {
                    jocAuditTrail.addResponseHeader(s, headers.get(s));
                }
            });
        } 
        // jocAuditTrail.setResponse(null); has to set earlier at the caller
        jocAuditTrail.log();
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }
    
    public static JOCDefaultResponse responseStatus200(byte[] entity, String mediaType, JocAuditTrail jocAuditTrail) {
        return responseStatus200(entity, mediaType, null, jocAuditTrail);
    }
    
    public static JOCDefaultResponse responseStatus200(byte[] entity, JocAuditTrail jocAuditTrail) { // called by ./logout
        jocAuditTrail.setResponse(entity);
        return responseStatus200(entity, MediaType.APPLICATION_JSON, jocAuditTrail);
    }
    
    public static JOCDefaultResponse responsePlainStatus200(byte[] entity, String mediaType, JocAuditTrail jocAuditTrail) {
        jocAuditTrail.setResponse(entity);
        return responseStatus200(entity, mediaType, jocAuditTrail);
    }

    public static JOCDefaultResponse responsePlainStatus200(StreamingOutput entity, Map<String, Object> headers, JocAuditTrail jocAuditTrail) {
        return responseStatus200(entity, MediaType.TEXT_PLAIN + "; charset=UTF-8", headers, jocAuditTrail);
    }

    protected static JOCDefaultResponse responseOctetStreamDownloadStatus200(StreamingOutput entity, String filename, Long uncompressedLength,
            JocAuditTrail jocAuditTrail) {
        try {
            filename = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        Response.ResponseBuilder responseBuilder = Response.ok(entity, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                "attachment; filename*=UTF-8''" + filename).header("Access-Control-Expose-Headers", "Content-Dispositon").cacheControl(
                        setNoCaching());

        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_OCTET_STREAM);
        jocAuditTrail.addResponseHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        jocAuditTrail.addResponseHeader("Access-Control-Expose-Headers", "Content-Dispositon");

        if (uncompressedLength != null) {
            responseBuilder.header("X-Uncompressed-Length", uncompressedLength);
            jocAuditTrail.addResponseHeader("X-Uncompressed-Length", uncompressedLength);
        }
        jocAuditTrail.log();
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus200(SOSAuthCurrentAccountAnswer entity, JocAuditTrail jocAuditTrail) {
        // called by SOSServicePermissionIam ("Sonderlocke" to hide accessToken)
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);

        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();

        return new JOCDefaultResponse(responseBuilder.build());
    }
    
    public static JOCDefaultResponse responseStatus200WithHeaders(SOSAuthCurrentAccountAnswer entity, long timeout,
            JocAuditTrail jocAuditTrail) {
        // called by SOSServicePermissionIam (./login)
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);
        responseBuilder.header(X_ACCESS_TOKEN, entity.getAccessToken());
        responseBuilder.header(TIMEOUT, timeout);

        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.addResponseHeader(TIMEOUT, timeout);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();

        return new JOCDefaultResponse(responseBuilder.build());
    }
    
    protected static JOCDefaultResponse responseStatusJSOk(Date surveyDate, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        Ok entity = new Ok();
        if (surveyDate != null) {
            entity.setSurveyDate(surveyDate);
        }
        entity.setDeliveryDate(new Date());
        entity.setOk(true);
        responseBuilder.entity(entity);

        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();

        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseNotYetImplemented() {
        return responseStatus420(getErr420(new JocError("444", "Not yet implemented")), new JocAuditTrail());
    }

    public static JOCDefaultResponse responseStatusJSError(String message) {
        return responseStatus420(getErr420(new JocError(message)), new JocAuditTrail()); // called by ./login
    }

    public static JOCDefaultResponse responseStatusJSError(SessionNotExistException e, String mediaType, JocAuditTrail jocAuditTrail) {
        String errorOutput = "";
        if (e.getCause() != null) {
            errorOutput = e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
        } else {
            errorOutput = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        Response.ResponseBuilder responseBuilder = Response.status(440, REASON_PHRASE_440).header("Content-Type", mediaType).cacheControl(
                setNoCaching());
        
        jocAuditTrail.addContentTypeHeader(mediaType);
        
        LOGGER.info(errorOutput);
        if (mediaType.contains(MediaType.TEXT_HTML)) {
            String entityStr = String.format(ERROR_HTML, "JOC-440", StringEscapeUtils.escapeHtml4(errorOutput));
            responseBuilder.entity(entityStr);
            
            jocAuditTrail.setResponse(entityStr);
        } else {
            SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer();
            sosAuthCurrentAccountAnswer.setHasRole(false);
            sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
            sosAuthCurrentAccountAnswer.setIsPermitted(false);
            sosAuthCurrentAccountAnswer.setMessage(errorOutput);
            responseBuilder.entity(sosAuthCurrentAccountAnswer);
            
            jocAuditTrail.setResponse(sosAuthCurrentAccountAnswer);
        }
        jocAuditTrail.log();
        
        return new JOCDefaultResponse(responseBuilder.build());
    }

//    public static JOCDefaultResponse responseStatusJSError(SessionNotExistException e) {
//        return responseStatusJSError(e, MediaType.APPLICATION_JSON);
//    }

    private static JOCDefaultResponse responseStatusJSError(JocException e, String mediaType, JocAuditTrail jocAuditTrail) {
        if (e instanceof SessionNotExistException) {
            return responseStatusJSError((SessionNotExistException) e, mediaType, jocAuditTrail);
        }
        if (!e.getError().printMetaInfo().isEmpty()) {
            LOGGER.info(e.getError().printMetaInfo());
        }
        String errorMsg = getErrorMessage(e);
        e.getError().setMessage(errorMsg);
        return responseStatus420(getErr420(e.getError()), mediaType, jocAuditTrail);

    }

    private static JOCDefaultResponse responseStatusJSError(Throwable e, String mediaType, JocAuditTrail jocAuditTrail) { // SOSServicePermissionIam
        if (e instanceof JocException) {
            return responseStatusJSError((JocException) e, mediaType, jocAuditTrail);
        }
        if (e.getCause() != null && e.getCause() instanceof JocException) {
            return responseStatusJSError((JocException) e.getCause(), mediaType, jocAuditTrail);
        }
        return responseStatus420(getErr420(new JocError(getErrorMessage(e))), mediaType, jocAuditTrail);
    }

    public static JOCDefaultResponse responseStatusJSError(Throwable e) { // SOSServicePermissionIam
        return responseStatusJSError(e, MediaType.APPLICATION_JSON, new JocAuditTrail());
    }

    public static JOCDefaultResponse responseStatusJSError(Throwable e, JocError err, String mediaType, JocAuditTrail jocAuditTrail) {
        if (e instanceof JocException) {
            JocException ee = (JocException) e;
            ee.addErrorMetaInfo(err);
            return responseStatusJSError(ee, mediaType, jocAuditTrail);
        }
        if (e.getCause() != null && e.getCause() instanceof JocException) {
            JocException ee = (JocException) e.getCause();
            ee.addErrorMetaInfo(err);
            return responseStatusJSError(ee, mediaType, jocAuditTrail);
        }
        if (!err.printMetaInfo().isEmpty()) {
            LOGGER.info(err.printMetaInfo());
        }
        return responseStatus420(getErr420(new JocError(getErrorMessage(e))), mediaType, jocAuditTrail);
    }

    private static JOCDefaultResponse responseStatus420(Err420 entity, String mediaType, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(420, REASON_PHRASE_420).header("Content-Type", mediaType).cacheControl(
                setNoCaching());
        jocAuditTrail.addContentTypeHeader(mediaType);
        if (mediaType.contains(MediaType.TEXT_HTML)) {
            String entityStr = String.format(ERROR_HTML, entity.getError().getCode(), StringEscapeUtils.escapeHtml4(entity.getError().getMessage()));
            responseBuilder.entity(entityStr);
            jocAuditTrail.setResponse(entityStr);
        } else {
            responseBuilder.entity(entity);
            jocAuditTrail.setResponse(entity);
        }
        jocAuditTrail.log();
        return new JOCDefaultResponse(responseBuilder.build());
    }

    private static JOCDefaultResponse responseStatus420(Err420 entity, JocAuditTrail jocAuditTrail) {
        return responseStatus420(entity, MediaType.APPLICATION_JSON, jocAuditTrail);
    }

//    public static JOCDefaultResponse responseHTMLStatus420(Err420 entity) {
//        return responseStatus420(entity, MediaType.TEXT_HTML + "; charset=UTF-8");
//    }

//    public static JOCDefaultResponse responseHTMLStatus420(String entity) {
//        entity = String.format(ERROR_HTML, "JOC-420", StringEscapeUtils.escapeHtml4(entity));
//        Response.ResponseBuilder responseBuilder = Response.status(420, REASON_PHRASE_420).header("Content-Type", MediaType.TEXT_HTML
//                + "; charset=UTF-8").cacheControl(setNoCaching());
//        responseBuilder.entity(entity);
//        return new JOCDefaultResponse(responseBuilder.build());
//    }

//    public static JOCDefaultResponse responsePlainStatus420(String entity) {
//        Response.ResponseBuilder responseBuilder = Response.status(420, REASON_PHRASE_420).header("Content-Type", MediaType.TEXT_PLAIN
//                + "; charset=UTF-8").cacheControl(setNoCaching());
//        responseBuilder.entity(entity);
//        return new JOCDefaultResponse(responseBuilder.build());
//    }

    public static JOCDefaultResponse responseStatus434JSError(JocException e, boolean withoutLogging, JocAuditTrail jocAuditTrail) {
        String errorMsg = e.toString();
        if (!withoutLogging) {
            if (!"".equals(e.getError().printMetaInfo())) {
                LOGGER.info(e.getError().printMetaInfo());
            }
            errorMsg = getErrorMessage(e);
        }
        e.getError().setMessage(errorMsg);
        return responseStatus434(getErr420(e.getError()), jocAuditTrail);
    }

    private static JOCDefaultResponse responseStatus434(Err420 entity, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(434, REASON_PHRASE_420).header("Content-Type", MediaType.APPLICATION_JSON)
                .cacheControl(setNoCaching());
        responseBuilder.entity(entity);
        
        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();
        
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus419(List<Err419> listOfErrors, JocAuditTrail jocAuditTrail) {
        Errs errors = new Errs();
        errors.setErrors(listOfErrors);

        Response.ResponseBuilder responseBuilder = Response.status(419, REASON_PHRASE_420).header("Content-Type", MediaType.APPLICATION_JSON)
                .cacheControl(setNoCaching());
        responseBuilder.entity(errors);
        
        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(errors);
        jocAuditTrail.log();
        
        return new JOCDefaultResponse(responseBuilder.build());
    }

//    public static JOCDefaultResponse responseStatus419(Object entity) {
//        Response.ResponseBuilder responseBuilder = Response.status(419, REASON_PHRASE_420).header("Content-Type", MediaType.APPLICATION_JSON)
//                .cacheControl(setNoCaching());
//        responseBuilder.entity(entity);
//        return new JOCDefaultResponse(responseBuilder.build());
//    }

//    public static JOCDefaultResponse responseStatus419(List<Err419> listOfErrors, JocError err) {
//        if (!err.printMetaInfo().isEmpty()) {
//            LOGGER.info(err.printMetaInfo());
//        }
//        return responseStatus419(listOfErrors);
//    }

//    public static JOCDefaultResponse responseStatus401(SOSAuthCurrentAccountAnswer entity) { // SOSServicePermissionIam ./login
//        return responseStatus401(entity, new JocAuditTrail());
//    }
    
    public static JOCDefaultResponse responseStatus401(SOSAuthCurrentAccountAnswer entity, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        LOGGER.info(entity.getMessage());
        responseBuilder.entity(entity);
        
        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();
        
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus403(SOSAuthCurrentAccountAnswer entity, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(403).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(setNoCaching());
        if (entity.getApiCall() == null) {
            LOGGER.error(entity.getMessage());
        } else {
            LOGGER.error(MarkerFactory.getMarker(entity.getApiCall()), entity.getMessage());
        }
        responseBuilder.entity(entity);
        
        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();
        
        return new JOCDefaultResponse(responseBuilder.build());
    }
    
    public static JOCDefaultResponse responseStatus433(byte[] entity, JocAuditTrail jocAuditTrail) {
        Response.ResponseBuilder responseBuilder = Response.status(433).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(setNoCaching());
        responseBuilder.entity(entity);
        
        jocAuditTrail.addContentTypeHeader(MediaType.APPLICATION_JSON);
        jocAuditTrail.setResponse(entity);
        jocAuditTrail.log();
        
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static SOSAuthCurrentAccountAnswer getError401Schema(JobSchedulerUser sosJobschedulerUser, JocError err) {
        String apiCall = Optional.ofNullable(err).map(JocError::getApiCall).orElse(null);
        return getError401Schema(sosJobschedulerUser, err, apiCall);
    }

    private static SOSAuthCurrentAccountAnswer getError401Schema(JobSchedulerUser sosJobschedulerUser, JocError err, String apiCall) {
        SOSAuthCurrentAccountAnswer entity = new SOSAuthCurrentAccountAnswer();
        SOSAuthCurrentAccount sosAuthCurrentAccountAnswer = null;
        String message = "Authentication failure";
        if (err != null) {
            if (err.getMessage() != null) {
                message = err.getMessage();
            }
            if (!"".equals(err.printMetaInfo())) {
                LOGGER.info(err.printMetaInfo());
            }
        }
        try {
            sosAuthCurrentAccountAnswer = sosJobschedulerUser.getSOSAuthCurrentAccount();
        } catch (SessionNotExistException e) {
            message += ": " + e.getMessage();
        }
        if (sosAuthCurrentAccountAnswer != null) {
            entity.setAccessToken(sosAuthCurrentAccountAnswer.getAccessToken());
            entity.setAccount(sosAuthCurrentAccountAnswer.getAccountname());
            entity.setRole(String.join(", ", sosAuthCurrentAccountAnswer.getRoles()));
            entity.setHasRole(!sosAuthCurrentAccountAnswer.getRoles().isEmpty());
        } else {
            entity.setAccessToken("");
            entity.setAccount("");
            entity.setHasRole(false);
        }
        entity.setIsPermitted(false);
        entity.setIsAuthenticated(sosJobschedulerUser.isAuthenticated());
        entity.setMessage(message);
        entity.setApiCall(apiCall);
        return entity;
    }

    public static String getErrorMessage(JocException e) {
        String errorOutput = e.getClass().getSimpleName() + ": ";
        String logOutput = e.getClass().getSimpleName() + ": ";
        if (!e.getMessage().isEmpty() || e.getCause() == null) {
            errorOutput += e.getMessage();
            logOutput += e.toString();
        } else {
            errorOutput += e.getCause().getMessage();
            logOutput += e.getCause().toString();
        }
        boolean logAsInfo = false;
        if (e.getError() != null) {
            logAsInfo = e.getError().isLogAsInfo();
        }
        if (e.getError() == null || e.getError().getApiCall() == null) {
            if (!logAsInfo) {
                LOGGER.error(logOutput, e);
            } else {
                LOGGER.info(logOutput);
            }
        } else {
            if (!logAsInfo) {
                LOGGER.error(MarkerFactory.getMarker(e.getError().getApiCall()), logOutput, e);
            } else {
                LOGGER.info(MarkerFactory.getMarker(e.getError().getApiCall()), logOutput);
            }
        }
        return errorOutput;
    }

    public static String getErrorMessage(Throwable e) {
        return getErrorMessage(e, null);
    }

    public static String getErrorMessage(Throwable e, String apiCall) {
        if (SessionNotExistException.class.isInstance(e)) {
            // LOGGER.warn(e.toString());
        } else if (JocAuthenticationException.class.isInstance(e)) {
            if (apiCall == null) {
                LOGGER.error(e.toString());
            } else {
                LOGGER.error(MarkerFactory.getMarker(apiCall), e.toString());
            }
        } else {
            if (apiCall == null) {
                LOGGER.error(e.toString(), e);
            } else {
                LOGGER.error(MarkerFactory.getMarker(apiCall), e.toString(), e);
            }
        }
        return e.toString();
    }

    private static Err420 getErr420(JocError e) {
        Err420 entity = new Err420();
        entity.setError(e);
        entity.setSurveyDate(new Date());
        entity.setDeliveryDate(new Date());
        return entity;
    }

    private static CacheControl setNoCaching() {
        CacheControl cache = new CacheControl();
        cache.setMustRevalidate(true);
        cache.setNoStore(true);
        cache.setNoCache(true);
        // cache.setNoTransform(true);
        return cache;
    }

}
