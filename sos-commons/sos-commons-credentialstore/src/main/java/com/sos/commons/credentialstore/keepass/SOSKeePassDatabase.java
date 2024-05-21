package com.sos.commons.credentialstore.keepass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.linguafranca.pwdb.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.db.ASOSKeePassHandler;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassAttachmentException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassEntryExpiredException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassEntryNotFoundException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassPropertyNotFoundException;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;

public class SOSKeePassDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeePassDatabase.class);

    public enum Module {
        DOM, JAXB
    };

    public static final Module DEFAULT_MODULE = Module.JAXB;

    public static final String ENV_VAR_APPDATA_PATH = "APPDATA_PATH";
    public static final String STANDARD_PROPERTY_NAME_ATTACHMENT = "Attachment";

    private SOSKeePassPath _keepassPath;
    private Entry<?, ?, ?, ?> _entry;

    private ASOSKeePassHandler _handler;

    public SOSKeePassDatabase(final Path file, Module module) throws SOSKeePassDatabaseException {
        if (file == null) {
            throw new SOSKeePassDatabaseException("KeePass file is null");
        }
        _handler = ASOSKeePassHandler.newInstance(file, module);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s]isKdbx=%s", SOSKeePassDatabase.class.getSimpleName(), getFilePath(_handler.getKeePassFile()), _handler
                    .isKdbx()));
        }
    }

    public static boolean isKdbx(Path keePassFile) {
        return keePassFile != null && isKdbx(keePassFile.getFileName().toString());
    }

    public static boolean isKdbx(String keePassFile) {
        return keePassFile != null && !keePassFile.toLowerCase().endsWith(".kdb");
    }

    public void load(final String password) throws SOSKeePassDatabaseException {
        load(password, null);
    }

    public void load(final String password, final Path keyFile) throws SOSKeePassDatabaseException {
        if (LOGGER.isTraceEnabled()) {
            String pass = password == null ? "" : "pass=?, ";
            LOGGER.trace(String.format("%skeyFile=%s", pass, keyFile));
        }
        _handler.load(password, keyFile);
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntries() {
        LOGGER.debug("[getEntries]");
        return _handler.getEntries();
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntriesByTitle(final String match) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getEntriesByTitle]%s", match));
        }
        if (_handler.getDatabase() == null) {
            return null;
        }
        return _handler.getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return entry.getTitle().matches(match == null ? "" : match);
            }
        });
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntriesByUsername(final String match) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getEntriesByUsername]%s", match));
        }
        if (_handler.getDatabase() == null) {
            return null;
        }
        return _handler.getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return entry.getUsername().matches(match == null ? "" : match);
            }
        });
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntriesByUrl(final String match) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getEntriesByUrl]%s", match));
        }
        if (_handler.getDatabase() == null) {
            return null;
        }
        return _handler.getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return entry.getUrl().matches(match == null ? "" : match);
            }
        });
    }

    public Entry<?, ?, ?, ?> getEntryByPath(final String path) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getEntryByPath]%s", path));
        }
        return _handler.getEntryByPath(path);
    }

    /** V1 KDB - returns the attachment data, V2 KDBX - returns the first attachment data */
    public byte[] getAttachment(final String entryPath) throws SOSKeePassDatabaseException {
        return getAttachment(getEntryByPath(entryPath), null);
    }

    /** V1 KDB - returns the attachment data, V2 KDBX - returns the attachment data of the propertyName */
    public byte[] getAttachment(final String entryPath, final String propertyName) throws SOSKeePassDatabaseException {
        return getAttachment(getEntryByPath(entryPath), propertyName);
    }

    /** V1 KDB - returns the attachment data, V2 KDBX - returns the first attachment data */
    public byte[] getAttachment(final Entry<?, ?, ?, ?> entry) throws SOSKeePassDatabaseException {
        return getAttachment(entry, null);
    }

    /** V1 KDB - returns the attachment data, V2 KDBX - returns the attachment data of the propertyName */
    public byte[] getAttachment(final Entry<?, ?, ?, ?> entry, String propertyName) throws SOSKeePassDatabaseException {
        if (entry == null) {
            throw new SOSKeePassEntryNotFoundException("entry is null");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getAttachment][%s]%s", entry.getPath(), propertyName));
        }
        if (propertyName != null && propertyName.equalsIgnoreCase(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
            propertyName = null;
        }
        byte[] data = _handler.getBinaryProperty(entry, propertyName);
        if (data == null || data.length == 0) {
            if (propertyName == null) {
                throw new SOSKeePassAttachmentException(String.format("[%s]attachment not found or is 0 bytes", entry.getPath()));
            } else {
                throw new SOSKeePassAttachmentException(String.format("[%s][%s]attachment not found or is 0 bytes", entry.getPath(), propertyName));
            }
        }
        return data;
    }

    /** V1 KDB - exports the attachment, V2 KDBX - exports the first attachment */
    public void exportAttachment2File(final String entryPath, final Path targetFile) throws SOSKeePassDatabaseException {
        exportAttachment2File(entryPath, targetFile, null);
    }

    /** V1 KDB - exports the attachment, V2 KDBX - exports the attachment of the propertyName */
    public void exportAttachment2File(final String entryPath, final Path targetFile, final String propertyName) throws SOSKeePassDatabaseException {
        exportAttachment2File(getEntryByPath(entryPath), targetFile, propertyName);
    }

    /** V1 KDB - exports the attachment, V2 KDBX - exports the first attachment */
    public void exportAttachment2File(final Entry<?, ?, ?, ?> entry, final Path targetFile) throws SOSKeePassDatabaseException {
        exportAttachment2File(entry, targetFile, null);
    }

    /** V1 KDB - exports the attachment, V2 KDBX - exports the attachment of the propertyName */
    public String exportAttachment2File(final Entry<?, ?, ?, ?> entry, final Path targetFile, final String propertyName)
            throws SOSKeePassDatabaseException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[exportAttachment2File][%s][%s]%s", entry.getPath(), propertyName, targetFile));
        }

        byte[] data = getAttachment(entry, propertyName);

        try {
            Files.write(targetFile, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Throwable e) {
            throw new SOSKeePassAttachmentException(String.format("[%s][%s][%s]can't write attachment to file: %s", entry.getPath(), propertyName,
                    getFilePath(targetFile), e.toString()), e);
        }
        return getFilePath(targetFile);
    }

    public static String getPropertyName(String propertyName) {
        if (SOSString.isEmpty(propertyName)) {
            return propertyName;
        }
        switch (propertyName.toLowerCase()) {
        case "title":
            return Entry.STANDARD_PROPERTY_NAME_TITLE;
        case "user":
        case "username":
            return Entry.STANDARD_PROPERTY_NAME_USER_NAME;
        case "password":
            return Entry.STANDARD_PROPERTY_NAME_PASSWORD;
        case "url":
            return Entry.STANDARD_PROPERTY_NAME_URL;
        case "notes":
            return Entry.STANDARD_PROPERTY_NAME_NOTES;
        case "attach":
        case "attachment":
            return STANDARD_PROPERTY_NAME_ATTACHMENT;
        default:
            return propertyName;
        }
    }

    public ASOSKeePassHandler getHandler() {
        return _handler;
    }

    public SOSKeePassPath getKeePassPath() {
        return _keepassPath;
    }

    public void setKeePassPath(SOSKeePassPath val) {
        _keepassPath = val;
    }

    public Entry<?, ?, ?, ?> getEntry() {
        return _entry;
    }

    protected void setEntry(Entry<?, ?, ?, ?> val) {
        _entry = val;
    }

    private Entry<?, ?, ?, ?> createEntry(String entryPath) throws SOSKeePassDatabaseException {
        return _handler.createEntry(entryPath);
    }

    protected Entry<?, ?, ?, ?> getEntry(SOSKeePassPath path) throws SOSKeePassDatabaseException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getEntry]%s", path.getEntry()));
        }

        Entry<?, ?, ?, ?> entry = getEntryByPath(path.getEntry());
        if (entry == null) {
            if (path.isCreateEntry()) {
                entry = createEntry(path.getEntry());
            } else {
                throw new SOSKeePassEntryNotFoundException(String.format("[%s][%s]entry not found", _handler.getKeePassFile(), path.getEntry()));
            }
        }
        if (entry.getExpires() && !path.isIgnoreExpired()) {
            throw new SOSKeePassEntryExpiredException(String.format("[%s][%s]entry is expired (%s)", _handler.getKeePassFile(), path.getEntry(), entry
                    .getExpiryTime()));
        }
        return entry;
    }

    public static String getBinaryPropertyName(SOSKeePassPath path, Path attachment) {
        String propertyName = path.getPropertyName();
        if (path.getPropertyName().equals(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
            propertyName = attachment.getFileName().toString();
        }
        return propertyName;
    }

    public void save() throws SOSKeePassDatabaseException {
        saveAs(_handler.getKeePassFile());
    }

    public void saveAs(String file) throws SOSKeePassDatabaseException {
        saveAs(Paths.get(file));
    }

    public void saveAs(Path file) throws SOSKeePassDatabaseException {
        _handler.saveAs(file);
    }

    public static String getProperty(String uri) throws Exception {
        SOSKeePassDatabase kpd = loadFromUri(uri);
        SOSKeePassPath path = kpd.getKeePassPath();
        Entry<?, ?, ?, ?> entry = kpd.getEntry(path);

        String val = null;
        String queryParamSetProperty = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_SET_PROPERTY);
        if (SOSString.isEmpty(queryParamSetProperty)) {
            if (path.isAttachment() || path.getPropertyName().equals(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
                val = new String(kpd.getAttachment(entry, path.getPropertyName()));
            } else {
                val = entry.getProperty(path.getPropertyName());
                if (val == null) {
                    throw new SOSKeePassPropertyNotFoundException(String.format("[%s]property not found", path.toString()));
                }
            }
        } else {
            if (path.isAttachment() || path.getPropertyName().equals(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
                Path attachment = Paths.get(queryParamSetProperty);
                entry = kpd.getHandler().setBinaryProperty(path, entry, attachment);
                if (path.isStdoutOnSetBinaryProperty()) {
                    val = new String(entry.getBinaryProperty(getBinaryPropertyName(path, attachment)));
                } else {
                    val = "";
                }
            } else {
                kpd.getHandler().setProperty(entry, path.getPropertyName(), queryParamSetProperty);
                val = queryParamSetProperty;
            }
        }
        if (kpd.getHandler().isDirty()) {
            kpd.save();
        }

        return val;
    }

    public static byte[] getBinaryProperty(String uri) throws Exception {
        SOSKeePassDatabase kpd = loadFromUri(uri);
        SOSKeePassPath path = kpd.getKeePassPath();
        Entry<?, ?, ?, ?> entry = kpd.getEntry(path);

        byte[] val = null;
        String queryParamSetProperty = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_SET_PROPERTY);
        if (SOSString.isEmpty(queryParamSetProperty)) {
            val = kpd.getAttachment(entry, path.getPropertyName());
        } else {
            Path attachment = Paths.get(queryParamSetProperty);
            entry = kpd.getHandler().setBinaryProperty(path, entry, attachment);
            val = entry.getBinaryProperty(getBinaryPropertyName(path, attachment));
        }

        if (kpd.getHandler().isDirty()) {
            kpd.save();
        }
        return val;
    }

    public static SOSKeePassDatabase loadFromUri(String uri) throws Exception {
        SOSKeePassPath path = new SOSKeePassPath(uri);
        if (!path.isValid()) {
            throw new SOSKeePassDatabaseException(String.format("[%s][not valid uri]%s", uri, path.getError()));
        }

        String queryFile = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_FILE);
        String queryKeyFile = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_KEY_FILE);
        String queryPassword = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_PASSWORD);
        String queryModule = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_MODULE);

        Path file = Paths.get(queryFile);
        Path keyFile = null;
        if (SOSString.isEmpty(queryKeyFile)) {
            keyFile = getDefaultKeyFile(file);
            if (keyFile == null) {
                if (SOSString.isEmpty(queryPassword)) {
                    throw new SOSKeePassDatabaseException(String.format("[%s]default key file not found. password is empty", uri));
                }
            }
        } else {
            keyFile = Paths.get(queryKeyFile);
            if (Files.notExists(keyFile)) {
                throw new SOSKeePassDatabaseException(String.format("[%s][%s]key file not found", uri, getFilePath(keyFile)));
            }
        }
        SOSKeePassDatabase kpd = new SOSKeePassDatabase(file, getModule(queryModule));
        kpd.setKeePassPath(path);
        if (keyFile == null) {
            kpd.load(queryPassword);
        } else {
            kpd.load(queryPassword, keyFile);
        }
        return kpd;
    }

    public static Module getModule(String m) {
        if (m == null) {
            return DEFAULT_MODULE;
        }
        try {
            return Module.valueOf(m.toUpperCase());
        } catch (Throwable e) {
            return DEFAULT_MODULE;
        }
    }

    public static Path getDefaultKeyFile(Path database) {
        String keyFileName = new StringBuilder(SOSPath.getFileNameWithoutExtension(database.getFileName())).append(".key").toString();
        Path parentDir = database.getParent();
        Path keyFile = parentDir == null ? Paths.get(keyFileName) : parentDir.resolve(keyFileName);
        if (Files.notExists(keyFile)) {// .key
            keyFile = Paths.get(keyFile.toString() + "x");// .keyx
            if (Files.notExists(keyFile)) {
                keyFile = null;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getDefaultKeyFile]%s", getFilePath(keyFile)));
        }
        return keyFile;
    }

    public static Path getCurrentPath(Path f) {
        if (f == null) {
            return null;
        }
        String appdata = System.getenv(ENV_VAR_APPDATA_PATH);
        if (SOSString.isEmpty(appdata)) {
            return f;
        } else {
            return Paths.get(appdata).resolve(f);
        }
    }

    public static String getFilePath(Path path) {
        if (path == null) {
            return null;
        }
        try {
            return path.toFile().getCanonicalPath();
        } catch (Exception ex) {
            return path.toString();
        }
    }

    public static void main(String[] args) {
        int exitStatus = 0;

        // examples:
        // cs://server/SFTP/my_server@user?file=my_file.kdbx
        // cs://server/SFTP/my_server@user?file=my_file.kdbx&key_file=my_keyfile.key&ignore_expired=1
        // cs://server/SFTP/my_server@user?file=my_file.kdbx&key_file=my_keyfile.key&attachment=1
        // cs://server/SFTP/my_server@user?file=my_file.kdbx&key_file=my_keyfile.png

        String uri = null;
        try {
            if (args.length > 0) {
                uri = args[0];
            }

            System.out.println(SOSKeePassDatabase.getProperty(uri));
        } catch (Throwable t) {
            exitStatus = 99;
            t.printStackTrace();
        } finally {
            System.exit(exitStatus);
        }
    }

}
