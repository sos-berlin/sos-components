package com.sos.jitl.jobs.rest;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;

public class Request {
    private String endpoint;
    @JsonSetter(
            nulls = Nulls.AS_EMPTY
    )
    private String body;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<KeyAndValue> headers;

    public String getEndpoint() {
        return this.endpoint;
    }


    public String getBody() {
        return this.body;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHeaders(List<KeyAndValue> headers) {
        this.headers = headers;
    }
}
