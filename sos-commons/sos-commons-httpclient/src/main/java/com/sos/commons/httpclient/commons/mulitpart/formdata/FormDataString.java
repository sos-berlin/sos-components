package com.sos.commons.httpclient.commons.mulitpart.formdata;

import java.nio.charset.StandardCharsets;

public class FormDataString extends FormData {

    private final String content;
    private final String contentType;
    
    public FormDataString(String fieldName, String content) {
        this(fieldName, content, PARAM_TYPE_TEXT_PLAIN_UTF8);
    }

    public FormDataString(String fieldName, String content, String contentType) {
        super(fieldName);
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public String getParamType() {
        return contentType;
    }

    @Override
    public byte[] getContent() {
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
