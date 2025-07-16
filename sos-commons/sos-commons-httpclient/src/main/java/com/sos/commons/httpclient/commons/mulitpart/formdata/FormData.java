package com.sos.commons.httpclient.commons.mulitpart.formdata;


public abstract class FormData {

    public static final String PARAM_TYPE_TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";
    public static final String PARAM_TYPE_BYTES = "application/octet-stream";

    protected final String fieldName;

    protected FormData(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Parameters for the "Content-Disposition" header of a part.<p>
     * Default <code>form-data; name="fieldName"</code>
     */
    public String getParamDisposition() {
        return "form-data; name=\"" + fieldName + "\"";
    }

    /**
     * Parameters for the "Content-Type" header of a part.<p>
     * See {@link #PARAM_TYPE_TEXT_PLAIN_UTF8} and {@link #PARAM_TYPE_BYTES}
     */
    public abstract String getParamType();

    /**
     * If null or an empty array is returned, no more content is assumed.
     */
    public abstract byte[] getContent();

    /**
     * Return true when more content is available / can be expected.<p>
     * Default false.
     */
    public boolean moreContent() {
        return false;
    }

    /**
     * Parts can be re-used for retries. Reset inner state to start anew.<p>
     * Default no-op.
     */
    public void reset() {
        // NO-OP
    }

}
