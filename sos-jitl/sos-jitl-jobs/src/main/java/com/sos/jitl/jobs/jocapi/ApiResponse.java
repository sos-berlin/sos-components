package com.sos.jitl.jobs.jocapi;


public class ApiResponse {


    private Integer statusCode;
    private String responseBody;
    private Exception exception;
    
    public ApiResponse(Integer statusCode, String responseBody, Exception exception) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.exception = exception;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
//    public void setResponseBody(String responseBody) {
//        this.responseBody = responseBody;
//    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
//    public void setStatusCode(Integer statusCode) {
//        this.statusCode = statusCode;
//    }
    
    public Exception getException() {
        return exception;
    }
//    public void setException(Exception exception) {
//        this.exception = exception;
//    }
    
}
