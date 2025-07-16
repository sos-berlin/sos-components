package com.sos.commons.httpclient.commons.mulitpart.formdata;


public class FormDataBytes extends FormData {

    private final String fileName;
    private final byte[] content;
    private final String contenType;

    public FormDataBytes(String fieldName, String fileName, byte[] content) {
        this(fieldName, fileName, content, PARAM_TYPE_BYTES);
    }
    
    public FormDataBytes(String fieldName, String fileName, byte[] content, String contentType) {
        super(fieldName);
        this.fileName = fileName;
        this.content = content;
        this.contenType = contentType;
    }

    @Override
    public String getParamDisposition() {
        return super.getParamDisposition() + "; filename=\"" + fileName + "\"";
    }

    @Override
    public String getParamType() {
        return contenType;
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
