package com.sos.schema.exception;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.networknt.schema.ValidationMessage;
import com.sos.commons.exception.SOSInvalidDataException;

public class SOSJsonSchemaException extends SOSInvalidDataException {

    private static final long serialVersionUID = 1L;
    private Set<ValidationMessage> errors = Collections.emptySet();
    
    public SOSJsonSchemaException() {
        super();
    }

    public SOSJsonSchemaException(String message) {
        super(message);
    }
    
    public SOSJsonSchemaException(Set<ValidationMessage> errors) {
        super(errors.stream().map(ValidationMessage::toString).collect(Collectors.joining(" or ")));
        this.errors = errors;
    }
    
    public SOSJsonSchemaException(Throwable cause) {
        super(cause);
    }
    
    public SOSJsonSchemaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSJsonSchemaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public Set<ValidationMessage> getErrors() {
        return this.errors;
    }
    
    public String getMessageFromErrors() {
        return errors.stream().map(ValidationMessage::toString).collect(Collectors.joining(" or "));
    }
}
