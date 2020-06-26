package com.sos.joc.cluster.api.bean.answer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JocClusterAnswerError {

    @JsonProperty("type")
    private String type;
    @JsonProperty("message")
    private String message;
    @JsonIgnore
    private Exception exception;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String val) {
        type = val;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String val) {
        message = val;
    }
    
    @JsonIgnore
    public Exception getException() {
        return exception;
    }

    @JsonIgnore
    public void setException(Exception e) {
        exception = e;
    }

}
