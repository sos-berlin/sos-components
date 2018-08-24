package com.sos.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class SOSUnicodeInputStream extends InputStream {

    private static final int BOM_SIZE = 4;
    private PushbackInputStream inStr;
    private boolean initialized = false;
    private String defaultEncoding;
    private String encoding;

    SOSUnicodeInputStream(InputStream in, String defaultEnc) {
        inStr = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEncoding = defaultEnc;
    }

    protected void init() throws IOException {
        if (initialized) {
            return;
        }
        byte bom[] = new byte[BOM_SIZE];
        int n, unread;
        n = inStr.read(bom, 0, bom.length);
        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else {
            encoding = defaultEncoding;
            unread = n;
        }
        if (unread > 0) {
            inStr.unread(bom, n - unread, unread);
        }
        initialized = true;
    }

    public String getEncoding() {
        if (!initialized) {
            try {
                init();
            } catch (IOException ex) {
                IllegalStateException ise = new IllegalStateException("Init method failed.");
                ise.initCause(ise);
                throw ise;
            }
        }
        return encoding;
    }

    public void close() throws IOException {
        initialized = true;
        inStr.close();
    }

    public int read() throws IOException {
        initialized = true;
        return inStr.read();
    }

}
