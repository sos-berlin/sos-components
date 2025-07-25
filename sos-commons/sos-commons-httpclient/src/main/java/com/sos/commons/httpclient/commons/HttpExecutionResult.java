package com.sos.commons.httpclient.commons;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Holds the result of an executed HTTP request and its response.
 *
 * <p>
 * This class represents the outcome of a synchronous HTTP call, including the original request data, the received response,<br/>
 * and any additional metadata (e.g., status, duration, errors).
 *
 * @param <T> the type of the response body */
public class HttpExecutionResult<T> {

    private final HttpResponse<T> response;

    private boolean formatWithResponseBody = false;
    // https://myaccount.blob.core.windows.net/?comp=list&sv=2020-10-02&ss=b&srt=co&sp=rwdlacx&se=2025-06-27T12:43:02Z&st=2025-06-27T11:38:02Z&spr=https&sig=XXXXX
    // as : https://myaccount.blob.core.windows.net/***
    private boolean formatWithMaskRequestURIQueryParams = false;

    /** Synchronous constructor used to capture the request and its response.
     *
     * <p>
     * Since the call is synchronous:<br/>
     * - The request object is already fully built and available before sending.<br/>
     * -- The client logs the request headers before sending the request.<br/>
     * - The response (and its headers) is available immediately after the request is sent and completed.
     * 
     * The response headers are logged via {@code debugHeaders()} after receiving the response.
     *
     * @param client the HTTP client executing the request
     * @param request the HTTP request sent
     * @param response the HTTP response received */
    protected HttpExecutionResult(ABaseHttpClient client, HttpResponse<T> response) {
        this.response = response;
        if (this.response != null) {
            client.debugHeaders("HttpResponse headers", this.response.headers());
        }
    }

    public HttpRequest request() {
        return response == null ? null : response.request();
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
