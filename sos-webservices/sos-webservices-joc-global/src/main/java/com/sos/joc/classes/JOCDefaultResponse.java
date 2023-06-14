package com.sos.joc.classes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Err420;
import com.sos.joc.model.common.Errs;
import com.sos.joc.model.common.Ok;

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

    private JOCDefaultResponse(Response delegate) {
        super(delegate);
    }

    public static JOCDefaultResponse responseStatus200(Object entity, String mediaType, Map<String, Object> headers) {
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", mediaType).cacheControl(setNoCaching());
        if (headers != null) {
            headers.keySet().stream().filter(s -> headers.get(s) != null).forEach(s -> responseBuilder.header(s, headers.get(s)));
        }

        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus200(Object entity, String mediaType) {
        return responseStatus200(entity, mediaType, null);
    }

    public static JOCDefaultResponse responseStatus200(Object entity) {
        return responseStatus200(entity, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseHtmlStatus200(Object entity) {
        return responseStatus200(entity, MediaType.TEXT_HTML + "; charset=UTF-8");
    }

    public static JOCDefaultResponse responsePlainStatus200(Object entity) {
        return responseStatus200(entity, MediaType.TEXT_PLAIN + "; charset=UTF-8");
    }

    public static JOCDefaultResponse responsePlainStatus200(Object entity, Map<String, Object> headers) {
        return responseStatus200(entity, MediaType.TEXT_PLAIN + "; charset=UTF-8", headers);
    }

    public static JOCDefaultResponse responseOctetStreamDownloadStatus200(Object entity, String filename) {
        return responseOctetStreamDownloadStatus200(entity, filename, null);
    }

    public static JOCDefaultResponse responseOctetStreamDownloadStatus200(Object entity, String filename, Long uncompressedLength) {
        try {
            filename = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        Response.ResponseBuilder responseBuilder = Response.ok(entity, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                "attachment; filename*=UTF-8''" + filename).header("Access-Control-Expose-Headers", "Content-Dispositon").cacheControl(
                        setNoCaching());

        if (uncompressedLength != null) {
            responseBuilder.header("X-Uncompressed-Length", uncompressedLength);
        }
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus200WithHeaders(Object entity, String accessToken, long timeout) {
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);
        responseBuilder.header(X_ACCESS_TOKEN, accessToken);
        responseBuilder.header(TIMEOUT, timeout);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatusJSOk(Date surveyDate) {
        Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        Ok entity = new Ok();
        if (surveyDate != null) {
            entity.setSurveyDate(surveyDate);
        }
        entity.setDeliveryDate(new Date());
        entity.setOk(true);
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseNotYetImplemented() {
        return responseStatus420(getErr420(new JocError("444", "Not yet implemented")));
    }

    public static JOCDefaultResponse responseStatusJSError(String message) {
        return responseStatus420(getErr420(new JocError(message)));
    }

    public static JOCDefaultResponse responseStatusJSError(SessionNotExistException e, String mediaType) {
        String errorOutput = "";
        if (e.getCause() != null) {
            errorOutput = e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
        } else {
            errorOutput = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        Response.ResponseBuilder responseBuilder = Response.status(440).header("Content-Type", mediaType).cacheControl(setNoCaching());
        LOGGER.info(errorOutput);
        if (mediaType.contains(MediaType.TEXT_HTML)) {
            String entityStr = String.format(ERROR_HTML, "JOC-440", StringEscapeUtils.escapeHtml4(errorOutput));
            responseBuilder.entity(entityStr);
        } else {
            SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer();
            sosAuthCurrentAccountAnswer.setHasRole(false);
            sosAuthCurrentAccountAnswer.setIsAuthenticated(false);
            sosAuthCurrentAccountAnswer.setIsPermitted(false);
            sosAuthCurrentAccountAnswer.setMessage(errorOutput);
            responseBuilder.entity(sosAuthCurrentAccountAnswer);
        }
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatusJSError(SessionNotExistException e) {
        return responseStatusJSError(e, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseStatusJSError(JocException e, String mediaType) {
        if (e instanceof SessionNotExistException) {
            return responseStatusJSError((SessionNotExistException) e, mediaType);
        }
        if (!"".equals(e.getError().printMetaInfo())) {
            LOGGER.info(e.getError().printMetaInfo());
        }
        String errorMsg = getErrorMessage(e);
        e.getError().setMessage(errorMsg);
        return responseStatus420(getErr420(e.getError()), mediaType);

    }

    public static JOCDefaultResponse responseStatusJSError(JocException e) {
        return responseStatusJSError(e, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseHTMLStatusJSError(JocException e) {
        return responseStatusJSError(e, MediaType.TEXT_HTML + "; charset=UTF-8");
    }

    public static JOCDefaultResponse responseStatusJSError(Throwable e, String mediaType) {
        if (e instanceof JocException) {
            return responseStatusJSError((JocException) e, mediaType);
        }
        if (e.getCause() != null && e.getCause() instanceof JocException) {
            return responseStatusJSError((JocException) e.getCause(), mediaType);
        }
        return responseStatus420(getErr420(new JocError(getErrorMessage(e))), mediaType);
    }

    public static JOCDefaultResponse responseStatusJSError(Throwable e) {
        return responseStatusJSError(e, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseHTMLStatusJSError(Throwable e) {
        return responseStatusJSError(e, MediaType.TEXT_HTML + "; charset=UTF-8");
    }

    public static JOCDefaultResponse responseStatusJSError(Throwable e, JocError err, String mediaType) {
        if (e instanceof JocException) {
            JocException ee = (JocException) e;
            ee.addErrorMetaInfo(err);
            return responseStatusJSError(ee, mediaType);
        }
        if (e.getCause() != null && e.getCause() instanceof JocException) {
            JocException ee = (JocException) e.getCause();
            ee.addErrorMetaInfo(err);
            return responseStatusJSError(ee, mediaType);
        }
        if (!"".equals(err.printMetaInfo())) {
            LOGGER.info(err.printMetaInfo());
        }
        return responseStatus420(getErr420(new JocError(getErrorMessage(e))), mediaType);
    }

    public static JOCDefaultResponse responseStatusJSError(Throwable e, JocError err) {
        return responseStatusJSError(e, err, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseHTMLStatusJSError(Throwable e, JocError err) {
        return responseStatusJSError(e, err, MediaType.TEXT_HTML + "; charset=UTF-8");
    }

    public static JOCDefaultResponse responseStatus420(Err420 entity, String mediaType) {
        Response.ResponseBuilder responseBuilder = Response.status(420).header("Content-Type", mediaType).cacheControl(setNoCaching());
        if (mediaType.contains(MediaType.TEXT_HTML)) {
            String entityStr = String.format(ERROR_HTML, entity.getError().getCode(), StringEscapeUtils.escapeHtml4(entity.getError().getMessage()));
            responseBuilder.entity(entityStr);
        } else {
            responseBuilder.entity(entity);
        }
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus420(Err420 entity) {
        return responseStatus420(entity, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseHTMLStatus420(Err420 entity) {
        return responseStatus420(entity, MediaType.TEXT_HTML + "; charset=UTF-8");
    }

    public static JOCDefaultResponse responseHTMLStatus420(String entity) {
        entity = String.format(ERROR_HTML, "JOC-420", StringEscapeUtils.escapeHtml4(entity));
        Response.ResponseBuilder responseBuilder = Response.status(420).header("Content-Type", MediaType.TEXT_HTML + "; charset=UTF-8").cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responsePlainStatus420(String entity) {
        Response.ResponseBuilder responseBuilder = Response.status(420).header("Content-Type", MediaType.TEXT_PLAIN + "; charset=UTF-8").cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus434JSError(JocException e) {
        return responseStatus434JSError(e, false);
    }

    public static JOCDefaultResponse responseStatus434JSError(JocException e, boolean withoutLogging) {
        String errorMsg = e.toString();
        if (!withoutLogging) {
            if (!"".equals(e.getError().printMetaInfo())) {
                LOGGER.info(e.getError().printMetaInfo());
            }
            errorMsg = getErrorMessage(e);
        }
        e.getError().setMessage(errorMsg);
        return responseStatus434(getErr420(e.getError()));
    }

    public static JOCDefaultResponse responseStatus434JSError(Exception e) {
        if (e instanceof JocException) {
            return responseStatus434JSError((JocException) e);
        }
        if (e.getCause() != null && e.getCause() instanceof JocException) {
            return responseStatus434JSError((JocException) e.getCause());
        }
        return responseStatus434(getErr420(new JocError(getErrorMessage(e))));
    }

    public static JOCDefaultResponse responseStatus434(Err420 entity) {
        Response.ResponseBuilder responseBuilder = Response.status(434).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus419(List<Err419> listOfErrors) {
        Errs errors = new Errs();
        errors.setErrors(listOfErrors);

        Response.ResponseBuilder responseBuilder = Response.status(419).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        responseBuilder.entity(errors);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus419(Object entity) {
        Response.ResponseBuilder responseBuilder = Response.status(419).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus419(List<Err419> listOfErrors, JocError err) {
        if (!"".equals(err.printMetaInfo())) {
            LOGGER.info(err.printMetaInfo());
        }
        return responseStatus419(listOfErrors);
    }

    public static JOCDefaultResponse responseStatus401(SOSAuthCurrentAccountAnswer entity) {
        Response.ResponseBuilder responseBuilder = Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON).cacheControl(
                setNoCaching());
        LOGGER.info(entity.getMessage());
        responseBuilder.entity(entity);
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus403(SOSAuthCurrentAccountAnswer entity, String mediaType) {
        Response.ResponseBuilder responseBuilder = Response.status(403).header("Content-Type", mediaType).cacheControl(setNoCaching());
        if (entity.getApiCall() == null) {
            LOGGER.error(entity.getMessage());
        } else {
            LOGGER.error(MarkerFactory.getMarker(entity.getApiCall()), entity.getMessage());
        }
        if (mediaType.contains(MediaType.TEXT_HTML)) {
            String entityStr = String.format(ERROR_HTML, "JOC-403", StringEscapeUtils.escapeHtml4(entity.getMessage()));
            responseBuilder.entity(entityStr);
        } else {
            responseBuilder.entity(entity);
        }
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus403(SOSAuthCurrentAccountAnswer entity) {
        return responseStatus403(entity, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseStatus440(SOSAuthCurrentAccountAnswer entity, String mediaType) {
        Response.ResponseBuilder responseBuilder = Response.status(440).header("Content-Type", mediaType).cacheControl(setNoCaching());
        if (entity.getApiCall() == null) {
            LOGGER.error(entity.getMessage());
        } else {
            LOGGER.error(MarkerFactory.getMarker(entity.getApiCall()), entity.getMessage());
        }
        if (mediaType.contains(MediaType.TEXT_HTML)) {
            String entityStr = String.format(ERROR_HTML, "JOC-440", StringEscapeUtils.escapeHtml4(entity.getMessage()));
            responseBuilder.entity(entityStr);
        } else {
            responseBuilder.entity(entity);
        }
        return new JOCDefaultResponse(responseBuilder.build());
    }

    public static JOCDefaultResponse responseStatus440(SOSAuthCurrentAccountAnswer entity) {
        return responseStatus440(entity, MediaType.APPLICATION_JSON);
    }

    public static JOCDefaultResponse responseHTMLStatus440(SOSAuthCurrentAccountAnswer entity) {
        return responseStatus440(entity, MediaType.TEXT_HTML + "; charset=UTF-8");
    }

    public static SOSAuthCurrentAccountAnswer getError401Schema(JobSchedulerUser sosJobschedulerUser, String apiCall) {
        return getError401Schema(sosJobschedulerUser, null, apiCall);
    }

    public static SOSAuthCurrentAccountAnswer getError401Schema(JobSchedulerUser sosJobschedulerUser, JocError err) {
        String apiCall = (err != null) ? err.getApiCall() : null;
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
        if (e.getError() == null || e.getError().getApiCall() == null) {
            if (!e.getError().isLogAsInfo()) {
                LOGGER.error(logOutput, e);
            }
        } else {
            LOGGER.error(MarkerFactory.getMarker(e.getError().getApiCall()), logOutput, e);
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
