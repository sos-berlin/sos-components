package com.sos.commons.credentialstore.keepass;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.kdb.KdbEntry;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;
import org.linguafranca.pwdb.kdbx.simple.SimpleIcon;
import org.linguafranca.pwdb.kdbx.simple.model.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassAttachmentException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassCredentialException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassEntryExpiredException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassEntryNotFoundException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassPropertyNotFoundException;
import com.sos.commons.credentialstore.keepass.extensions.credentials.SOSKdbxCreds;
import com.sos.commons.credentialstore.keepass.extensions.simple.SOSSimpleDatabase;

import com.sos.commons.util.SOSString;

public class SOSKeePassDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeePassDatabase.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    public static final String ENV_VAR_APPDATA_PATH = "APPDATA_PATH";
    /* see KdbDatabase constructor: KDB files don't have a single root group, this is a synthetic surrogate */
    public static final String KDB_ROOT_GROUP_NAME = "Root";
    public static final String KDB_GROUP_TITLE = "Meta-Info";
    public static final String STANDARD_PROPERTY_NAME_ATTACHMENT = "Attachment";
    public static final int ICON_NEW_GROUP_INDEX = 48; // folder icon

    private Path _file;
    private boolean _isKdbx;
    private Database<?, ?, ?, ?> _database;
    private SOSKeePassPath _keepassPath;
    private Entry<?, ?, ?, ?> _entry;
    private boolean _changed;

    private Credentials _credentials;

    public SOSKeePassDatabase(final Path file) throws SOSKeePassDatabaseException {
        if (file == null) {
            throw new SOSKeePassDatabaseException("KeePass file is null");
        }
        _file = file;
        _isKdbx = file.toString().toLowerCase().endsWith(".kdbx");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]isKdbx=%s", SOSKeePassDatabase.class.getSimpleName(), getFilePath(_file), _isKdbx));
        }
    }

    public void load(final String password) throws SOSKeePassDatabaseException {
        load(password, null);
    }

    public void load(final String password, final Path keyFile) throws SOSKeePassDatabaseException {
        if (isTraceEnabled) {
            String pass = password == null ? "" : "pass=?, ";
            LOGGER.trace(String.format("%skeyFile=%s", pass, keyFile));
        }
        if (_isKdbx) {
            _database = getKDBXDatabase(password, keyFile);
        } else {
            _database = getKDBDatabase(password, keyFile);
        }
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntries() {
        LOGGER.debug("[getEntries]");

        if (_database == null) {
            return null;
        }
        if (_isKdbx) {
            return _database.findEntries("");
        } else {
            return _database.findEntries(new Entry.Matcher() {

                public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                    return !entry.getTitle().equals(KDB_GROUP_TITLE);
                }
            });
        }
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntriesByTitle(final String match) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getEntriesByTitle]%s", match));
        }
        if (_database == null) {
            return null;
        }
        return _database.findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return entry.getTitle().matches(match == null ? "" : match);
            }
        });
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntriesByUsername(final String match) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getEntriesByUsername]%s", match));
        }
        if (_database == null) {
            return null;
        }
        return _database.findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return entry.getUsername().matches(match == null ? "" : match);
            }
        });
    }

    public List<? extends Entry<?, ?, ?, ?>> getEntriesByUrl(final String match) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getEntriesByUrl]%s", match));
        }
        if (_database == null) {
            return null;
        }
        return _database.findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                return entry.getUrl().matches(match == null ? "" : match);
            }
        });
    }

    public Entry<?, ?, ?, ?> getEntryByPath(final String path) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getEntryByPath]%s", path));
        }
        if (_database == null || path == null) {
            return null;
        }
        List<? extends Entry<?, ?, ?, ?>> l = _database.findEntries(new Entry.Matcher() {

            public boolean matches(@SuppressWarnings("rawtypes") Entry entry) {
                String p = path.startsWith("/") ? path : "/" + path;
                return _isKdbx ? entry.getPath().equals(p) : entry.getPath().equals("/" + KDB_ROOT_GROUP_NAME + p);
            }
        });
        return l == null || l.size() == 0 ? null : l.get(0);
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
        return getAttachment(_isKdbx, entry, propertyName);
    }

    private static byte[] getAttachment(boolean isKdbx, final Entry<?, ?, ?, ?> entry, String propertyName) throws SOSKeePassDatabaseException {
        if (entry == null) {
            throw new SOSKeePassEntryNotFoundException("entry is null");
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getAttachment][%s]%s", entry.getPath(), propertyName));
        }
        if (propertyName != null && propertyName.equalsIgnoreCase(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
            propertyName = null;
        }
        byte[] data = null;
        try {
            if (isKdbx) {
                if (propertyName == null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[getAttachment][%s]get first BinaryProperty", entry.getPath()));
                    }
                    List<String> l = entry.getBinaryPropertyNames();
                    if (l != null && l.size() > 0) {
                        data = entry.getBinaryProperty(l.get(0));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[getAttachment][%s][%s]get BinaryProperty", entry.getPath(), propertyName));
                    }
                    data = entry.getBinaryProperty(propertyName);
                }
            } else {
                data = ((KdbEntry) entry).getBinaryData();
            }
        } catch (Throwable e) {
            throw new SOSKeePassAttachmentException(e);
        }
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
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[exportAttachment2File][%s][%s]%s", entry.getPath(), propertyName, targetFile));
        }

        byte[] data = getAttachment(_isKdbx, entry, propertyName);

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

    private Credentials getKDBCredentials(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        String method = "getKDBCredentials";
        Credentials cred = null;

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
                    LOGGER.debug(String.format("[%s]keyFile=%s", method, getFilePath(keyFile)));
                }
                throw new SOSKeePassCredentialException(
                        "Composite key with key file without master password is not supported for KeePass 1.x format. Please provide a valid password.");
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]pass=?, keyFile=%s", method, getFilePath(keyFile)));
            }
            InputStream is = null;
            try {
                is = Files.newInputStream(keyFile);
                cred = new KdbCredentials.KeyFile(pass.getBytes(), is);
            } catch (Throwable e) {
                throw new SOSKeePassCredentialException(String.format("[%s]%s", getFilePath(keyFile), e.toString()), e);
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

    private KdbDatabase getKDBDatabase(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        Credentials cred = getKDBCredentials(pass, keyFile);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getKDBDatabase]%s", getFilePath(_file)));
        }

        KdbDatabase database = null;
        InputStream is = null;
        try {
            is = Files.newInputStream(_file);
            database = KdbDatabase.load(cred, is);
        } catch (Throwable e) {
            throw new SOSKeePassDatabaseException(String.format("[%s]%s", getFilePath(_file), e.toString()), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable te) {
                }
            }
        }
        return database;
    }

    private Credentials getKDBXCredentials(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        SOSKdbxCreds cred = new SOSKdbxCreds();
        cred.load(pass, keyFile);

        return cred;
    }

    private SimpleDatabase getKDBXDatabase(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        Credentials cred = getKDBXCredentials(pass, keyFile);
        _credentials = cred;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getKDBXDatabase]%s", _file));
        }
        return SOSSimpleDatabase.load(cred, _file);
    }

    public Path getFile() {
        return _file;
    }

    public Database<?, ?, ?, ?> getDatabase() {
        return _database;
    }

    public boolean isKDBX() {
        return _isKdbx;
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

    public void setEntry(Entry<?, ?, ?, ?> val) {
        _entry = val;
    }

    public Entry<?, ?, ?, ?> createEntry(String entryPath) throws SOSKeePassDatabaseException {
        String method = "createEntry";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][entryPath]%s", method, entryPath));
        }
        if (!_isKdbx) {
            throw new SOSKeePassDatabaseException(".kdx format is not yet supported");
        }

        String[] arr = entryPath.split("/");
        if (!_database.getRootGroup().getName().equals(arr[1])) {
            throw new SOSKeePassDatabaseException(String.format("[%s]could't create entry. Root node not matching: %s != %s", entryPath, arr[1],
                    _database.getRootGroup().getName()));
        }

        SimpleDatabase sd = (SimpleDatabase) _database;
        SimpleGroup lastGroup = sd.getRootGroup();
        SimpleEntry entry = null;

        for (int i = 2; i < arr.length; i++) {
            String name = arr[i];

            if (i == arr.length - 1) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][addEntry]%s", method, name));
                }

                entry = addEntry(sd, lastGroup, name);
                _changed = true;
            } else {
                List<? extends SimpleGroup> result = lastGroup.findGroups(name);
                if (result == null || result.size() == 0) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][addGroup]%s", method, name));
                    }

                    lastGroup = addGroup(sd, lastGroup, name);
                    _changed = true;
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][useExistingGroup]%s", method, name));
                    }
                    lastGroup = result.get(0);
                }
            }
        }

        _database = sd;

        return entry;
    }

    private SimpleEntry addEntry(SimpleDatabase sd, SimpleGroup parentGroup, String entryName) {
        SimpleEntry se = sd.newEntry(entryName);
        setEntryTimesAsUTC(se);

        SimpleEntry entry = parentGroup.addEntry(se);
        setEntryLastModificationTimeAsUTC(parentGroup);

        return entry;
    }

    private SimpleGroup addGroup(SimpleDatabase sd, SimpleGroup parentGroup, String groupName) {
        SimpleGroup sg = sd.newGroup(groupName);
        sg.setIcon(new SimpleIcon(ICON_NEW_GROUP_INDEX));
        setEntryTimesAsUTC(sg);

        SimpleGroup group = parentGroup.addGroup(sg);
        setEntryLastModificationTimeAsUTC(parentGroup);

        return group;
    }

    private Date getNowAsUTC() {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void setEntryTimesAsUTC(Object obj) {
        Field f = FieldUtils.getField(obj.getClass(), "times", true);
        try {
            Times t = new Times(getNowAsUTC());
            f.set(obj, t);
        } catch (Throwable e) {
        }
    }

    private void setEntryLastModificationTimeAsUTC(Object obj) {
        Field f = FieldUtils.getField(obj.getClass(), "times", true);
        try {
            Times t = (Times) f.get(obj);
            t.setLastModificationTime(getNowAsUTC());
        } catch (Throwable e) {
        }
    }

    public Entry<?, ?, ?, ?> getEntry(SOSKeePassPath path) throws SOSKeePassDatabaseException {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[getEntry]%s", path.getEntry()));
        }

        Entry<?, ?, ?, ?> entry = getEntryByPath(path.getEntry());
        if (entry == null) {
            if (path.isCreateEntry()) {
                entry = createEntry(path.getEntry());
            } else {
                throw new SOSKeePassEntryNotFoundException(String.format("[%s][%s]entry not found", getFile(), path.getEntry()));
            }
        }
        if (entry.getExpires() && !path.isIgnoreExpired()) {
            throw new SOSKeePassEntryExpiredException(String.format("[%s][%s]entry is expired (%s)", getFile(), path.getEntry(), entry
                    .getExpiryTime()));
        }
        return entry;
    }

    private Entry<?, ?, ?, ?> setBinaryProperty(SOSKeePassPath path, Entry<?, ?, ?, ?> entry, Path attachment) throws SOSKeePassAttachmentException {
        String method = "setBinaryProperty";
        if (Files.notExists(attachment)) {
            throw new SOSKeePassAttachmentException(String.format("[%s]attachment file not founded", getFilePath(attachment)));
        }
        String propertyName = getBinaryPropertyName(path, attachment);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]%s", method, path.getEntry(), propertyName, attachment));
        }
        try {
            entry.setBinaryProperty(propertyName, Files.readAllBytes(attachment));
        } catch (Throwable t) {
            throw new SOSKeePassAttachmentException(t);
        }

        setEntryLastModificationTimeAsUTC(entry);

        _changed = true;
        return entry;
    }

    private Entry<?, ?, ?, ?> setProperty(Entry<?, ?, ?, ?> entry, String name, String value) {
        entry.setProperty(name, value);
        setEntryLastModificationTimeAsUTC(entry);
        return entry;
    }

    private static String getBinaryPropertyName(SOSKeePassPath path, Path attachment) {
        String propertyName = path.getPropertyName();
        if (path.getPropertyName().equals(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
            propertyName = attachment.getFileName().toString();
        }
        return propertyName;
    }

    public void save() throws SOSKeePassDatabaseException {
        saveAs(_file);
    }

    public void saveAs(String file) throws SOSKeePassDatabaseException {
        saveAs(Paths.get(file));
    }

    public void saveAs(Path file) throws SOSKeePassDatabaseException {
        if (!_isKdbx) {
            throw new SOSKeePassDatabaseException(".kdx format is not yet supported");
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[save]%s", file));
        }
        new SOSSimpleDatabase().saveAs((SimpleDatabase) _database, _credentials, file);
    }

    public static String getProperty(String uri) throws Exception {
        SOSKeePassDatabase kpd = loadFromUri(uri);
        SOSKeePassPath path = kpd.getKeePassPath();
        Entry<?, ?, ?, ?> entry = kpd.getEntry(path);

        String val = null;
        String queryParamSetProperty = path.getQueryParameters().get(SOSKeePassPath.QUERY_PARAMETER_SET_PROPERTY);
        if (SOSString.isEmpty(queryParamSetProperty)) {
            if (path.isAttachment() || path.getPropertyName().equals(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
                val = new String(getAttachment(path.isKdbx(), entry, path.getPropertyName()));
            } else {
                val = entry.getProperty(path.getPropertyName());
                if (val == null) {
                    throw new SOSKeePassPropertyNotFoundException(String.format("[%s]property not found", path.toString()));
                }
            }
        } else {
            if (!kpd.isKDBX()) {
                throw new Exception(".kdx format is not yet supported");
            }

            if (path.isAttachment() || path.getPropertyName().equals(STANDARD_PROPERTY_NAME_ATTACHMENT)) {
                Path attachment = Paths.get(queryParamSetProperty);
                entry = kpd.setBinaryProperty(path, entry, attachment);
                if (path.isStdoutOnSetBinaryProperty()) {
                    val = new String(entry.getBinaryProperty(getBinaryPropertyName(path, attachment)));
                } else {
                    val = "";
                }
            } else {
                kpd.setProperty(entry, path.getPropertyName(), queryParamSetProperty);
                val = queryParamSetProperty;
            }
            kpd.setChanged(true);
        }
        if (kpd.isChanged()) {
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
            val = getAttachment(path.isKdbx(), entry, path.getPropertyName());
        } else {
            if (!kpd.isKDBX()) {
                throw new Exception(".kdx format is not yet supported");
            }
            Path attachment = Paths.get(queryParamSetProperty);
            entry = kpd.setBinaryProperty(path, entry, attachment);
            kpd.setChanged(true);

            val = entry.getBinaryProperty(getBinaryPropertyName(path, attachment));
        }

        if (kpd.isChanged()) {
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

        Path file = Paths.get(queryFile);
        Path keyFile = null;
        if (SOSString.isEmpty(queryKeyFile)) {
            keyFile = getDefaultKeyFile(file);
            if (Files.notExists(keyFile)) {
                if (SOSString.isEmpty(queryPassword)) {
                    throw new SOSKeePassDatabaseException(String.format("[%s][%s]key file not found. password is empty", uri, getFilePath(keyFile)));
                }
                keyFile = null;
            }
        } else {
            keyFile = Paths.get(queryKeyFile);
            if (Files.notExists(keyFile)) {
                throw new SOSKeePassDatabaseException(String.format("[%s][%s]key file not found", uri, getFilePath(keyFile)));
            }
        }
        SOSKeePassDatabase kpd = new SOSKeePassDatabase(file);
        kpd.setKeePassPath(path);
        if (keyFile == null) {
            kpd.load(queryPassword);
        } else {
            kpd.load(queryPassword, keyFile);
        }
        return kpd;
    }

    public static Path getDefaultKeyFile(Path database) {
        Path keyFile = null;
        String keyFileName = new StringBuilder(com.google.common.io.Files.getNameWithoutExtension(database.getFileName().toString())).append(".key")
                .toString();
        Path parentDir = database.getParent();
        if (parentDir == null) {
            keyFile = Paths.get(keyFileName);
        } else {
            keyFile = parentDir.resolve(keyFileName);
        }
        if (isDebugEnabled) {
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

    public Credentials getCredentials() {
        return _credentials;
    }

    protected void setChanged(boolean val) {
        _changed = val;
    }

    public boolean isChanged() {
        return _changed;
    }

    public static String getFilePath(Path path) {
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
