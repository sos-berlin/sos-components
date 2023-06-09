package com.sos.commons.credentialstore.keepass.db.kdb;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.kdb.KdbEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.credentialstore.keepass.db.ASOSKeePassHandler;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassAttachmentException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassCredentialException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.util.SOSString;

public class SOSKdbHandler extends ASOSKeePassHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKdbHandler.class);

    public static final String KDB_GROUP_TITLE = "Meta-Info";
    /* see KdbDatabase constructor: KDB files don't have a single root group, this is a synthetic surrogate */
    public static final String KDB_ROOT_GROUP_NAME = "Root";

    @Override
    public boolean isKdbx() {
        return false;
    }

    @Override
    public void load(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        setCredentials(getCredentials(pass, keyFile));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[load]%s", SOSKeePassDatabase.getFilePath(getKeePassFile())));
        }

        KdbDatabase database = null;
        InputStream is = null;
        try {
            is = Files.newInputStream(getKeePassFile());
            database = KdbDatabase.load(getCredentials(), is);
        } catch (Throwable e) {
            throw new SOSKeePassDatabaseException(String.format("[%s]%s", SOSKeePassDatabase.getFilePath(getKeePassFile()), e.toString()), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable te) {
                }
            }
        }
        setDatabase(database);
    }

    private Credentials getCredentials(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        String method = "getCredentials";
        Credentials cred = null;

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (keyFile == null) {
            if (SOSString.isEmpty(pass)) {
                throw new SOSKeePassCredentialException("The password for the database must not be null. Please provide a valid password.");
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]pass=?", method));
            }
            try {
                cred = new KdbCredentials.Password(pass.getBytes());
            } catch (Throwable e) {
                throw new SOSKeePassCredentialException(e);
            }
        } else {
            if (SOSString.isEmpty(pass)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s]keyFile=%s", method, SOSKeePassDatabase.getFilePath(keyFile)));
                }
                throw new SOSKeePassCredentialException(
                        "Composite key with key file without master password is not supported for KeePass 1.x format. Please provide a valid password.");
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]pass=?, keyFile=%s", method, SOSKeePassDatabase.getFilePath(keyFile)));
            }
            InputStream is = null;
            try {
                is = Files.newInputStream(keyFile);
                cred = new KdbCredentials.KeyFile(pass.getBytes(), is);
            } catch (Throwable e) {
                throw new SOSKeePassCredentialException(String.format("[%s]%s", SOSKeePassDatabase.getFilePath(keyFile), e.toString()), e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable te) {
                    }
                }
            }
        }
        return cred;
    }

    @Override
    public List<? extends Entry<?, ?, ?, ?>> getEntries() {
        if (getDatabase() == null) {
            return null;
        }
        return getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return !entry.getTitle().equals(KDB_GROUP_TITLE);
            }
        });
    }

    @Override
    public Entry<?, ?, ?, ?> getEntryByPath(String path) {
        if (getDatabase() == null || path == null) {
            return null;
        }
        List<? extends Entry<?, ?, ?, ?>> l = getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                String p = path.startsWith("/") ? path : "/" + path;
                return entry.getPath().equals("/" + KDB_ROOT_GROUP_NAME + p);
            }
        });
        return l == null || l.size() == 0 ? null : l.get(0);
    }

    @Override
    public byte[] getBinaryProperty(Entry<?, ?, ?, ?> entry, String propertyName) throws SOSKeePassAttachmentException {
        try {
            return ((KdbEntry) entry).getBinaryData();
        } catch (Throwable e) {
            throw new SOSKeePassAttachmentException(e);
        }
    }

    @Override
    public Entry<?, ?, ?, ?> createEntry(String entryPath) throws SOSKeePassDatabaseException {
        throw new SOSKeePassDatabaseException(".kdx format is not yet supported");
    }

    @Override
    public void saveAs(Path file) throws SOSKeePassDatabaseException {
        throw new SOSKeePassDatabaseException(".kdx format is not yet supported");
    }

    @Override
    public Entry<?, ?, ?, ?> setBinaryProperty(SOSKeePassPath path, Entry<?, ?, ?, ?> entry, Path attachment) throws SOSKeePassDatabaseException {
        throw new SOSKeePassDatabaseException(".kdx format is not yet supported");
    }

    @Override
    public Entry<?, ?, ?, ?> setProperty(Entry<?, ?, ?, ?> entry, String name, String value) throws SOSKeePassDatabaseException {
        throw new SOSKeePassDatabaseException(".kdx format is not yet supported");
    }

}
