package com.sos.joc.exceptions;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.order.AddOrder;


public class BulkError extends Err419 {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkError.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger(WebserviceConstants.AUDIT_LOGGER);
    private static final String ERROR_CODE = "JOC-419";

    public BulkError() {
        setSurveyDate(Date.from(Instant.now()));
    }
    
    public Err419 get(Throwable e, JocError jocError, String path) {
        setCodeAndMessage(e, jocError);
        setPath(path);
        return this;
    }
    
    public Err419 get(JocError jocError, String path) {
        setCodeAndMessage(jocError);
        setPath(path);
        return this;
    }
    
    public Err419 get(Throwable e, JocError jocError, AddOrder startOrder) {
        setCodeAndMessage(e, jocError);
        setPath(startOrder.getWorkflowPath() + "/" + startOrder.getOrderName());
        return this;
    }
    
    private void setCodeAndMessage(JocException e) {
        if (e instanceof JobSchedulerBadRequestException) {
            setSurveyDate(((JobSchedulerBadRequestException) e).getSurveyDate());
        }
        JocError err = e.getError();
        setCode(err.getCode());
        setMessage(err.getMessage());
        AUDIT_LOGGER.error(err.getMessage());
    }
    
    private void setCodeAndMessage(JocError jocError) {
        setCode(jocError.getCode());
        setMessage(jocError.getMessage());
        printMetaInfo(jocError);
        LOGGER.error(getMessage());
        AUDIT_LOGGER.error(jocError.getMessage());
    }
    
    private void setCodeAndMessage(Throwable e, JocError jocError) {
        if (JocException.class.isInstance(e)) {
            setCodeAndMessage((JocException) e);
        } else {
            setCode(ERROR_CODE);
            String errorMsg = ((e.getCause() != null) ? e.getCause().toString() : e.getClass().getSimpleName()) + ": " + e.getMessage();
            setMessage(errorMsg);
            AUDIT_LOGGER.error(errorMsg);
        }
        printMetaInfo(jocError);
        LOGGER.error(e.getMessage(),e);
    }
    
    private void printMetaInfo(JocError jocError) {
        String metaInfo = jocError.printMetaInfo();
        if (!metaInfo.isEmpty()) {
            LOGGER.info(metaInfo);
            jocError.getMetaInfo().clear();
        }
    }
}
