package com.sos.joc.cluster.api.bean.answer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JocClusterAnswer {

    public enum JocClusterAnswerType {
        SUCCESS, ERROR;
    }

    public enum JocClusterAnswerState {
        STARTED, STOPPED, RESTARTED, ALREADY_STARTED, ALREADY_STOPPED, WAITING_FOR_RESOURCES;
    }

    @JsonProperty("type")
    private JocClusterAnswerType type;
    @JsonProperty("state")
    private JocClusterAnswerState state;
    @JsonProperty("error")
    private JocClusterAnswerError error;

    @JsonProperty("type")
    public JocClusterAnswerType getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(JocClusterAnswerType val) {
        type = val;
    }

    @JsonProperty("state")
    public JocClusterAnswerState getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(JocClusterAnswerState val) {
        state = val;
    }

    @JsonProperty("error")
    public JocClusterAnswerError getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(JocClusterAnswerError val) {
        error = val;
    }

    public void createError(Exception e) {
        type = JocClusterAnswerType.ERROR;
        error = new JocClusterAnswerError();
        error.setType(e.getClass().getSimpleName());
        error.setMessage(e.toString());
        error.setException(e);
    }
}
