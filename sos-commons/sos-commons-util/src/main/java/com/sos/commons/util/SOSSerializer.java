package com.sos.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Note - serialized string has not the same value as a only Base64 encoded string (because writeObject is used)<br />
 * e.g.:<br />
 * - new SOSSerializer<String>().serialize("Hello World") - Output: rO0ABXQAC0hlbGxvIFdvcmxk<br />
 * - SOSBase64.encode("Hello World")- Output: SGVsbG8gV29ybGQ= */
public class SOSSerializer<T extends Serializable> {

    /** Serialize non compressed - Base64 encode */
    public String serialize(final T t) throws Exception {
        if (t == null) {
            return null;
        }
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(t);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /** Deserialize non compressed - Base64 decode */
    @SuppressWarnings("unchecked")
    public T deserialize(final String t) throws Exception {
        if (t == null) {
            return null;
        }
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(t)))) {
            return (T) ois.readObject();
        }
    }

    /** Serialize compressed - Base64 encode of GZIP compressing */
    public String serializeCompressed(final T t) throws Exception {
        return Base64.getEncoder().encodeToString(serializeCompressed2bytes(t));
    }

    /** Serialize compressed - GZIP compressing */
    public byte[] serializeCompressed2bytes(final T t) throws Exception {
        if (t == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPOutputStream gos = new GZIPOutputStream(baos); ObjectOutputStream oos =
                new ObjectOutputStream(gos)) {
            oos.writeObject(t);
            oos.flush();
            gos.finish();
            return baos.toByteArray();
        }
    }

    /** Deserialize compressed object - Base64 decode of GZIP compressing */
    public T deserializeCompressed(String t) throws Exception {
        return deserializeCompressed(Base64.getDecoder().decode(t));
    }

    /** Deserialize compressed object - GZIP decompressing */
    @SuppressWarnings("unchecked")
    public T deserializeCompressed(byte[] t) throws Exception {
        if (t == null) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(t)))) {
            return (T) ois.readObject();
        }
    }

}
