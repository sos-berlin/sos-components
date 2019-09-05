package com.sos.commons.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.util.ByteArrayBuffer;


public class SOSStreamUnzip {

    private static final int BUFFER = 4096;
    
    public static byte[] unzip(byte[] source) throws IOException {
        return unzip(source, BUFFER);
    }

    public static byte[] unzip(byte[] source, int bufferSize) throws IOException {
        if (source == null) {
           return null; 
        }
        InputStream is = null;
        try {
            if( source.length >= 4 && source[0] == (byte) 0x1f && source[1] == (byte) 0x8b ) {
                is = new GZIPInputStream(new ByteArrayInputStream(source));
            } else { 
                return source;
            }
            final ByteArrayBuffer byteBuffer = new ByteArrayBuffer(bufferSize);
            byte[] buffer = new byte[bufferSize];
            int l;
            while ((l = is.read(buffer)) != -1) {
                byteBuffer.append(buffer, 0, l);
            }
            return byteBuffer.toByteArray();
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
            }
        }
    }
    
    public static Path unzipToFile(byte[] source, String prefix) throws IOException {
        if (source == null) {
            return null;
        }
        InputStream is = null;
        Path path = null;
        try {
            //check if matches standard gzip magic number
            if( source.length >= 4 && source[0] == (byte) 0x1f && source[1] == (byte) 0x8b ) {
                is = new GZIPInputStream(new ByteArrayInputStream(source));
            } else { 
                is = new ByteArrayInputStream(source);
            }
            path = Files.createTempFile(prefix, null);
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            try {
                if (path != null) {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e1) {
            }
            throw e;
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
            }
        }
    }
    
    public static Path toGzipFile(byte[] source, String prefix) throws IOException {
        Path path = null;
        try {
            path = Files.createTempFile(prefix, null);
            if (toGzipFile(source, path, false)) {
                return path;
            } else {
                return null;
            }
        } catch (Exception e) {
            try {
                if (path != null) {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e1) {
            }
            throw e;
        }
    }
        
    public static boolean toGzipFile(byte[] source, Path target, boolean append) throws IOException {
        if (source == null) {
            return false;
        }
        InputStream is = null;
        OutputStream out = null;
        try {
            is = new ByteArrayInputStream(source);
            //check if matches standard gzip magic number
            if (source.length >= 4 && source[0] == (byte) 0x1f && source[1] == (byte) 0x8b) {
                if (append) {
                    out = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                } else {
                    out = Files.newOutputStream(target);
                }
            } else {
                if (append) {
                    out = new GZIPOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                            StandardOpenOption.APPEND));
                } else {
                    out = new GZIPOutputStream(Files.newOutputStream(target));
                }
            }
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return true;
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
            }
        }
    }
    
    public static String unzip2String(byte[] source) throws IOException {
        return unzip2String(source, BUFFER);
    }

    public static String unzip2String(byte[] source, int bufferSize) throws IOException {
        return new String(unzip(source, bufferSize), "UTF-8");
    }
    
    public static long getSize(byte[] source) throws IOException {
        if (source == null) {
            return 0L;
        }
        int logLength = source.length;
        if( source.length >= 4 && source[0] == 31 && source[1] == 139 ) {
            int b4 = source[logLength-4];
            int b3 = source[logLength-3];
            int b2 = source[logLength-2];
            int b1 = source[logLength-1];
            return ((long)b1 << 24) | ((long)b2 << 16) | ((long)b3 << 8) | (long)b4;
        } else { 
            return logLength;
        }
    }

}
