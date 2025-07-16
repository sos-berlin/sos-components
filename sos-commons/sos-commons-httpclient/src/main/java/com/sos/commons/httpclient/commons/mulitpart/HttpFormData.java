package com.sos.commons.httpclient.commons.mulitpart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.sos.commons.httpclient.commons.mulitpart.formdata.FormData;
import com.sos.commons.httpclient.commons.mulitpart.formdata.FormDataIterator;

public class HttpFormData implements Iterable<byte[]> {

    private static final String boundary = "-----SOSFormBoundary" + UUID.randomUUID().toString();
    private final List<FormData> parts = new ArrayList<>();
    public static final String contentType = "multipart/form-data; boundary=" + boundary;
    public static final String CONTENT_TYPE_ZIP = "application/x-zip-compressed";
    public static final String CONTENT_TYPE_GZIP = "application/x-gzip";

    /**
     * Add parts only BEFORE {@link #iterator()} is called.<p>
     * For {@link AutoCloseable} parts use {@link HttpFormDataCloseable}
     * which has proper support for closeable parts.
     */
    public void addPart(FormData formData) {
        parts.add(formData);
    }

    /**
     * Current list of parts (the actual list, not a copy, so be careful).
     */
    public List<FormData> getParts() {
        return parts;
    }

    /**
     * Boundary used to separate form-data parts.
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Use this method for the value of the header "Content-Type"
     */
    public String getContentType() {
        return "multipart/form-data; boundary=" + getBoundary();
    }

    /**
     * This method allows for the usage of this class as parameter for {@link java.net.http.HttpRequest.BodyPublisher}.ofByteArrays
     * <p>
     * Parts are reset before creating a new {@link FormDataPartsIterator}.
     */
    @Override
    public Iterator<byte[]> iterator() {
        parts.forEach(FormData::reset);
        return new FormDataIterator(parts, boundary);
    }
}
