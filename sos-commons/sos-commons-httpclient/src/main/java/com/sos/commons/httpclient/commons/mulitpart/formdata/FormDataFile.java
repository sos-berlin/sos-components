package com.sos.commons.httpclient.commons.mulitpart.formdata;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FormDataFile extends FormData implements Closeable {

    public static final int BUFFER_SIZE_DEFAULT = 8192;

    private final String fileName;
    private final Path file;
    private final String contenType;
    private final int bufferSize;

    private InputStream in;
    
    public FormDataFile(String fieldName, String fileName, Path file) {
        this(fieldName, fileName, file, PARAM_TYPE_BYTES);
    }

    public FormDataFile(String fieldName, String fileName, Path file, String contentType) {
        this(fieldName, fileName, file, contentType, BUFFER_SIZE_DEFAULT);
    }

    public FormDataFile(String fieldName, String fileName, Path file, int bufferSize) {
        this(fieldName, fileName, file, PARAM_TYPE_BYTES, bufferSize);
    }

    public FormDataFile(String fieldName, String fileName, Path file, String contentType, int bufferSize) {
        super(fieldName);
        this.fileName = fileName;
        this.file = file;
        this.bufferSize = bufferSize;
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
        
        if (in == null) {
            try {
                in = Files.newInputStream(file);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to open file " + file, e);
            }
        }
        var buf = new byte[bufferSize];
        int l = 0;
        try {
            l = in.read(buf);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read contents from file " + file, e);
        }
        if (l == bufferSize) {
            return buf;
        }
        if (l < 0) {
            try {
                in.close();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to close file " + file, e);
            }
            in = null;
            return null; //NOSONAR
        }
        var content = new byte[l];
        System.arraycopy(buf, 0, content, 0, l);
        return content;
    }

    @Override
    public boolean moreContent() {
        return in != null;
    }

    @Override
    public void reset() {
        try {
            close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Follows Closeable contract, only try closing once.
     */
    @Override
    public void close() throws IOException {
        if (in == null) {
            return;
        }
        try {
            in.close();
            in = null;
        } catch (IOException e) {
            in = null;
            throw e;
        }
    }
}
