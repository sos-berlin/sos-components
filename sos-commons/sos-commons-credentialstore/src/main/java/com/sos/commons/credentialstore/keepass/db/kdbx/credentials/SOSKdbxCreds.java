package com.sos.commons.credentialstore.keepass.db.kdbx.credentials;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.MessageDigest;

import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.security.Encryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassCredentialException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassKeyFileException;
import com.sos.commons.util.SOSString;

public class SOSKdbxCreds implements Credentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKdbxCreds.class);

    private byte[] key;

    public SOSKdbxCreds() {
    }

    public void load(String password, Path keyFile) throws SOSKeePassDatabaseException {

        FileInputStream is = null;
        try {
            if (keyFile == null) {
                handlePassword(password);
            } else {
                is = new FileInputStream(SOSKeePassDatabase.getFilePath(keyFile));
                if (isXmlKey(is)) {
                    handleXmlKey(is, password);
                } else {
                    handleBinaryKey(is, password);
                }
            }
        } catch (Throwable e) {
            if (keyFile == null) {
                throw new SOSKeePassCredentialException(e);
            } else {
                throw new SOSKeePassCredentialException(String.format("[%s]%s", SOSKeePassDatabase.getFilePath(keyFile), e.toString()), e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable te) {
                }
            }
        }
    }

    private boolean isXmlKey(@NotNull FileInputStream is) throws Exception {

        FileChannel fc = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(50);
            fc = is.getChannel();
            fc.read(buffer, 0);
            String preview = new String(buffer.array()).trim().replaceAll(">\\s+<", "><").toLowerCase();
            return preview.startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?><keyfile>");
        } catch (Exception e) {
            throw e;
        } finally {
            // do not close FileChannel as this will close the entire InputStream
            if (fc != null) {
                // fc.position(0);
            }
        }
    }

    private void handlePassword(String password) throws Exception {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[handlePassword]");
        }
        if (SOSString.isEmpty(password)) {
            throw new SOSKeePassCredentialException("The password for the database must not be null. Please provide a valid password.");
        }
        key = new KdbxCreds(password.getBytes()).getKey();
    }

    private void handleXmlKey(@NotNull InputStream keyFile, String password) throws SOSKeePassKeyFileException {
        if (SOSString.isEmpty(password)) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[handleXmlKey]keyFile");
            }
            this.key = new KdbxCreds(keyFile).getKey();
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[handleXmlKey]pass,keyFile");
            }
            this.key = new KdbxCreds(password.getBytes(), keyFile).getKey();
        }
    }

    private void handleBinaryKey(@NotNull InputStream keyFile, String password) {
        MessageDigest md = Encryption.getSha256MessageDigestInstance();
        if (SOSString.isEmpty(password)) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[handleBinaryKey]keyFile");
            }
            byte[] hash = md.digest(toByteArray(keyFile));
            key = md.digest(hash);
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[handleBinaryKey]pass,keyFile");
            }
            byte[] passHash = md.digest(password.getBytes());
            byte[] keyHash = md.digest(toByteArray(keyFile));
            key = md.digest(Bytes.concat(passHash, keyHash));
        }
    }

    private byte[] toByteArray(@NotNull InputStream keyFile) {
        byte[] ba = null;
        try {
            ba = ByteStreams.toByteArray(keyFile);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        return ba;
    }

    @Override
    public byte[] getKey() {
        return key;
    }

}
