package com.sos.jitl.jobs.jocapi;


public class ApiResponse {


    private Integer statusCode;
    private String responseBody;
    private Exception exception;
    private String accessToken;
    
    public ApiResponse(Integer statusCode, String responseBody, String accessToken, Exception exception) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.exception = exception;
        this.accessToken = accessToken;
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
