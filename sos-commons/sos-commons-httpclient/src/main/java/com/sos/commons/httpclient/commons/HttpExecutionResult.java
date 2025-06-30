package com.sos.commons.httpclient.commons;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Holds result of an executed request and its response */
public class HttpExecutionResult<T> {

    private final HttpRequest request;
    private final HttpResponse<T> response;

    private boolean formatWithResponseBody = false;
    // https://myaccount.blob.core.windows.net/?comp=list&sv=2020-10-02&ss=b&srt=co&sp=rwdlacx&se=2025-06-27T12:43:02Z&st=2025-06-27T11:38:02Z&spr=https&sig=XXXXX
    // as : https://myaccount.blob.core.windows.net/***
    private boolean formatWithMaskRequestURIQueryParams = false;

    protected HttpExecutionResult(HttpRequest request, HttpResponse<T> response) {
        this.request = request;
        this.response = response;
    }

    public HttpRequest request() {
        return request;
    }

    public HttpResponse<T> response() {
        return response;
    }

    public boolean formatWithResponseBody() {
        return formatWithResponseBody;
    }

    public void formatWithResponseBody(boolean val) {
        formatWithResponseBody = val;
    }

    public boolean formatWithMaskRequestURIQueryParams() {
        return formatWithMaskRequestURIQueryParams;
    }

    public void formatWithMaskRequestURIQueryParams(boolean val) {
        formatWithMaskRequestURIQueryParams = val;
    }

}
