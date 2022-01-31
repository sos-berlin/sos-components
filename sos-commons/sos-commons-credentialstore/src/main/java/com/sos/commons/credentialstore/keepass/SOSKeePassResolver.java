package com.sos.commons.credentialstore.keepass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.linguafranca.pwdb.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassPropertyNotFoundException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassResolverException;

import com.sos.commons.util.SOSString;

public class SOSKeePassResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeePassResolver.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    private Path file;
    private Path keyFile;
    private String password;
    private String entryPath;
    private Map<String, SOSKeePassDatabase> databases = new HashMap<String, SOSKeePassDatabase>();
    private Map<String, Entry<?, ?, ?, ?>> entries = new HashMap<String, Entry<?, ?, ?, ?>>();

    public SOSKeePassResolver() {
    }

    public SOSKeePassResolver(Path databaseFile) {
        this(databaseFile, null, null);
    }

    public SOSKeePassResolver(String databaseFile) {
        this(databaseFile, null, null);
    }

    public SOSKeePassResolver(Path databaseFile, Path databaseKeyFile) {
        this(databaseFile, databaseKeyFile, null);
    }

    public SOSKeePassResolver(String databaseFile, String databaseKeyFile) {
        this(databaseFile, databaseKeyFile, null);
    }

    public SOSKeePassResolver(Path databaseFile, String databasePassword) {
        this(databaseFile, null, databasePassword);
    }

    public SOSKeePassResolver(String databaseFile, String databaseKeyFile, String databasePassword) {
        try {
            if (!SOSString.isEmpty(databaseFile)) {
                file = SOSKeePassDatabase.getCurrentPath(Paths.get(databaseFile.trim()));
            }
        } catch (Throwable e) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s]%s", databaseFile, e.toString()));
            }
        }
        try {
            if (!SOSString.isEmpty(databaseKeyFile)) {
                keyFile = SOSKeePassDatabase.getCurrentPath(Paths.get(databaseKeyFile.trim()));
            }
        } catch (Throwable e) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s]%s", databaseKeyFile, e.toString()));
            }
        }

        if (!SOSString.isEmpty(databasePassword)) {
            password = databasePassword;
        }
    }

    public SOSKeePassResolver(Path databaseFile, Path databaseKeyFile, String databasePassword) {
        file = SOSKeePassDatabase.getCurrentPath(databaseFile);
        keyFile = SOSKeePassDatabase.getCurrentPath(databaseKeyFile);
        if (!SOSString.isEmpty(databasePassword)) {
            password = databasePassword;
        }
    }

    public String resolve(String uri) throws SOSKeePassDatabaseException {
        SOSKeePassPath path = getKeePassPath(uri);
        if (path == null) {
            return uri;
        }
        try {
            SOSKeePassDatabase d = init(path);
            String val;
            if (path.isAttachment() || path.getPropertyName().equals(SOSKeePassDatabase.STANDARD_PROPERTY_NAME_ATTACHMENT)) {
                val = new String(d.getAttachment(d.getEntry(), path.getPropertyName()));
            } else {
                val = d.getEntry().getProperty(path.getPropertyName());
                if (val == null) {
                    throw new SOSKeePassPropertyNotFoundException(String.format("[%s]property not found", path.toString()));
                }
            }
            return val;
        } catch (Throwable e) {
            throw e;
        }
    }

    public byte[] getBinaryProperty(String uri) throws SOSKeePassDatabaseException {
        SOSKeePassPath path = getKeePassPath(uri);
        if (path == null) {
            return null;
        }
        SOSKeePassDatabase d = init(path);
        if (path.isAttachment() || path.getPropertyName().equals(SOSKeePassDatabase.STANDARD_PROPERTY_NAME_ATTACHMENT)) {
            return d.getAttachment(d.getEntry(), path.getPropertyName());
        }
        return null;
    }

    private SOSKeePassPath getKeePassPath(String uri) throws SOSKeePassResolverException {
        if (SOSString.isEmpty(uri)) {
            if (isTraceEnabled) {
                LOGGER.trace("[skip]uri is empty");
            }
            return null;
        }
        SOSKeePassPath path = new SOSKeePassPath(uri);
        if (path.isValid()) {
            return path;
        }
        if (!uri.startsWith(SOSKeePassPath.PATH_PREFIX)) {
            return null;
        }
        if (file == null) {
            throw new SOSKeePassResolverException(String.format("[%s]missing credential store file", uri));
        }

        if (uri.startsWith(new StringBuilder(SOSKeePassPath.PATH_PREFIX).append(SOSKeePassPath.PROPERTY_PREFIX).toString())) {
            path = new SOSKeePassPath(file.toString().toLowerCase().endsWith(".kdbx"), uri, entryPath);
        } else {
            path = new SOSKeePassPath(file.toString().toLowerCase().endsWith(".kdbx"), uri);
        }
        if (!path.isValid()) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[skip][uri=%s][entryPath=%s]invalid path", uri, entryPath));
            }
            path = null;
        }
        return path;
    }

    private SOSKeePassDatabase init(SOSKeePassPath path) throws SOSKeePassDatabaseException {
        Map<String, String> queryParameters = path.getQueryParameters();
        Path f = getCurrentFile(path, queryParameters);
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", f, path.getEntryPath()));
        }

        SOSKeePassDatabase d = null;
        String key = f.toString();
        if (databases.containsKey(key)) {
            d = databases.get(key);
        } else {
            String p = getCurrentPassword(path, queryParameters);
            Path kf = getCurrentKeyFile(path, queryParameters, f, p);
            d = loadDatabase(f, kf, p);
            databases.put(key, d);
        }
        d.setEntry(getCurrentEntry(path, f, d));
        d.setKeePassPath(path);
        return d;
    }

    private SOSKeePassDatabase loadDatabase(Path currentFile, Path currentKeyFile, String currentPassword) throws SOSKeePassDatabaseException {
        SOSKeePassDatabase d = new SOSKeePassDatabase(currentFile);
        if (currentKeyFile == null) {
            d.load(currentPassword);
        } else {
            d.load(currentPassword, currentKeyFile);
        }
        return d;
    }

    private Entry<?, ?, ?, ?> getCurrentEntry(SOSKeePassPath path, Path currentFile, SOSKeePassDatabase currentDatabase)
            throws SOSKeePassDatabaseException {
        Entry<?, ?, ?, ?> entry = null;
        String key = new StringBuilder(currentFile.toString()).append(path.getEntryPath()).toString();
        if (entries.containsKey(key)) {
            entry = entries.get(key);
        } else {
            entry = currentDatabase.getEntry(path);
            entries.put(key, entry);
        }
        return entry;
    }

    private Path getCurrentFile(SOSKeePassPath path, Map<String, String> queryParameters) throws SOSKeePassResolverException {
        Path f = null;
        if (queryParameters != null) {
            String queryFile = queryParameters.get(SOSKeePassPath.QUERY_PARAMETER_FILE);
            if (queryFile != null) {
                f = Paths.get(queryFile);
                if (file == null) {
                    file = f;
                }
            }
        }
        if (f == null) {
            f = file;
        }
        if (f == null) {
            throw new SOSKeePassResolverException(String.format("[%s]missing database file", path.getQuery()));
        }
        return SOSKeePassDatabase.getCurrentPath(f);
    }

    private Path getCurrentKeyFile(SOSKeePassPath path, Map<String, String> queryParameters, Path currentFile, String currentPassword)
            throws SOSKeePassDatabaseException {

        Path kf = null;
        String queryKeyFile = null;
        if (queryParameters != null) {
            queryKeyFile = queryParameters.get(SOSKeePassPath.QUERY_PARAMETER_KEY_FILE);
        }
        if (SOSString.isEmpty(queryKeyFile)) {
            if (keyFile == null) {
                kf = SOSKeePassDatabase.getDefaultKeyFile(currentFile);
                if (kf == null) {
                    if (SOSString.isEmpty(currentPassword)) {
                        throw new SOSKeePassDatabaseException(String.format("[%s]default key file not found. password is empty", path.getQuery()));
                    }
                }
                keyFile = kf;
            } else {
                kf = keyFile;
            }
        } else {
            kf = SOSKeePassDatabase.getCurrentPath(Paths.get(queryKeyFile));
            if (Files.notExists(kf)) {
                throw new SOSKeePassDatabaseException(String.format("[%s][%s]key file not found", path.getQuery(), SOSKeePassDatabase.getFilePath(
                        kf)));
            }
            if (keyFile == null) {
                keyFile = kf;
            }
        }
        return kf;
    }

    private String getCurrentPassword(SOSKeePassPath path, Map<String, String> queryParameters) {
        String p = null;
        if (queryParameters != null) {
            String queryPassword = queryParameters.get(SOSKeePassPath.QUERY_PARAMETER_PASSWORD);
            if (queryPassword != null) {
                p = queryPassword;
            }
        }
        if (p == null) {
            p = password;
        }
        return p;
    }

    public Path getFile() {
        return file;
    }

    public Path getKeyFile() {
        return keyFile;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String val) {
        entryPath = val;
    }

}
