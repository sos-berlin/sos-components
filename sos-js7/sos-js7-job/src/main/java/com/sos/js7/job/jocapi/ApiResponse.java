package com.sos.js7.job.jocapi;

public class ApiResponse {

    private Integer statusCode;
    private String responseBody;
    private Exception exception;
    private String accessToken;
    private String reasonPhrase;

    public ApiResponse(Integer statusCode, String reasonPhrase, String responseBody, String accessToken, Exception exception) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.exception = exception;
        this.accessToken = accessToken;
        this.reasonPhrase = reasonPhrase;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Exception getException() {
        return exception;
    }

    public String getAccessToken() {
        return accessToken;
    }
    
}
