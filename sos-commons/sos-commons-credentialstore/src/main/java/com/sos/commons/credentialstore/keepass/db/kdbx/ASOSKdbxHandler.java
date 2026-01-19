package com.sos.commons.credentialstore.keepass.db.kdbx;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.credentialstore.keepass.db.ASOSKeePassHandler;
import com.sos.commons.credentialstore.keepass.db.kdbx.credentials.SOSKdbxCreds;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassAttachmentException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;

public abstract class ASOSKdbxHandler extends ASOSKeePassHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASOSKdbxHandler.class);

    protected abstract Database<?, ?, ?, ?> load() throws SOSKeePassDatabaseException;

    protected abstract Entry<?, ?, ?, ?> createEntry(String[] entryPath) throws SOSKeePassDatabaseException;

    @Override
    public boolean isKdbx() {
        return true;
    }

    @Override
    public void load(String pass, Path keyFile) throws SOSKeePassDatabaseException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[load]%s", SOSKeePassDatabase.getFilePath(getKeePassFile())));
        }
        setCredentials(pass, keyFile);
        setDatabase(load());
    }

    @Override
    public List<? extends Entry<?, ?, ?, ?>> getEntries() {
        if (getDatabase() == null) {
            return null;
        }
        return getDatabase().findEntries("");
    }

    @Override
    public Entry<?, ?, ?, ?> getEntryByPath(String path) {
        if (getDatabase() == null || path == null) {
            return null;
        }
        List<? extends Entry<?, ?, ?, ?>> l = getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                String p = path.startsWith("/") ? path : "/" + path;
                return entry.getPath().equals(p);
            }
        });
        return l == null || l.size() == 0 ? null : l.get(0);
    }

    @Override
    public byte[] getBinaryProperty(Entry<?, ?, ?, ?> entry, String propertyName) throws SOSKeePassAttachmentException {
        try {
            if (propertyName == null) {
                List<String> l = entry.getBinaryPropertyNames();
                if (l != null && l.size() > 0) {
                    String fa = l.get(0);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[getBinaryProperty][%s][first BinaryProperty]%s", entry.getPath(), fa));
                    }
                    return entry.getBinaryProperty(fa);
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[getBinaryProperty][%s][BinaryProperty]%s", entry.getPath(), propertyName));
                }
                return entry.getBinaryProperty(propertyName);
            }
        } catch (Exception e) {
            throw new SOSKeePassAttachmentException(e);
        }
        return null;
    }

    @Override
    public Entry<?, ?, ?, ?> setBinaryProperty(SOSKeePassPath path, Entry<?, ?, ?, ?> entry, Path attachment) throws SOSKeePassAttachmentException {
        String method = "setBinaryProperty";
        if (Files.notExists(attachment)) {
            throw new SOSKeePassAttachmentException(String.format("[%s]attachment file not founded", SOSKeePassDatabase.getFilePath(attachment)));
        }
        String propertyName = SOSKeePassDatabase.getBinaryPropertyName(path, attachment);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s][%s]%s", method, path.getEntry(), propertyName, attachment));
        }
        try {
            entry.setBinaryProperty(propertyName, Files.readAllBytes(attachment));
        } catch (Exception t) {
            throw new SOSKeePassAttachmentException(t);
        }
        return entry;
    }

    @Override
    public Entry<?, ?, ?, ?> setProperty(Entry<?, ?, ?, ?> entry, String name, String value) {
        entry.setProperty(name, value);
        return entry;
    }

    @Override
    public Entry<?, ?, ?, ?> createEntry(final String entryPath) throws SOSKeePassDatabaseException {
        String method = "createEntry";
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][entryPath]%s", method, entryPath));
        }
        String[] arr = null;
        if (entryPath.startsWith("/")) {
            arr = entryPath.split("/");
            if (!getDatabase().getRootGroup().getName().equals(arr[1])) {
                throw new SOSKeePassDatabaseException(String.format("[%s]could't create entry. Root node not matching: %s != %s", entryPath, arr[1],
                        getDatabase().getRootGroup().getName()));
            }
        } else {
            arr = new StringBuilder().append("/").append(getDatabase().getRootGroup().getName()).append("/").append(entryPath).toString().split("/");
        }
        return createEntry(arr);
    }

    @Override
    public void saveAs(Path file) throws SOSKeePassDatabaseException {
        if (getDatabase() == null) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            getDatabase().save(getCredentials(), fos);
        } catch (Exception e) {
            throw new SOSKeePassDatabaseException(e);
        }
    }

    protected Date getNowAsUTC() {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    protected void setCredentials(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        SOSKdbxCreds cred = new SOSKdbxCreds();
        cred.load(pass, keyFile);
        setCredentials(cred);
    }

}
