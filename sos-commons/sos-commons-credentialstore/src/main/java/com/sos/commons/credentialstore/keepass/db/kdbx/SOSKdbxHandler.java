package com.sos.commons.credentialstore.keepass.db.kdbx;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.credentialstore.keepass.db.ASOSKeePassHandler;
import com.sos.commons.credentialstore.keepass.db.kdbx.credentials.SOSKdbxCreds;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassAttachmentException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;

public class SOSKdbxHandler extends ASOSKeePassHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKdbxHandler.class);

    @Override
    public boolean isKdbx() {
        return true;
    }

    @Override
    public void load(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        setCredentials(pass, keyFile);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[load]%s", SOSKeePassDatabase.getFilePath(getKeePassFile())));
        }

        KdbxDatabase database = null;
        try (InputStream is = Files.newInputStream(getKeePassFile())) {
            database = KdbxDatabase.read(getCredentials(), is);
        } catch (Throwable e) {
            throw new SOSKeePassDatabaseException(String.format("[%s]%s", SOSKeePassDatabase.getFilePath(getKeePassFile()), e.toString()), e);
        }
        setDatabase(database);
    }

    @Override
    public List<? extends Entry> getEntries() {
        if (getDatabase() == null) {
            return null;
        }
        return getDatabase().findEntries("");
    }

    @Override
    public Entry getEntryByPath(String path) {
        if (getDatabase() == null || path == null) {
            return null;
        }
        List<? extends Entry> l = getDatabase().findEntries(new Entry.Matcher() {

            public boolean matches(Entry entry) {
                String p = path.startsWith("/") ? path : "/" + path;
                return entry.getPath().equals(p);
            }
        });
        return l == null || l.size() == 0 ? null : l.get(0);
    }

    @Override
    public byte[] getBinaryProperty(Entry entry, String propertyName) throws SOSKeePassAttachmentException {
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
        } catch (Throwable e) {
            throw new SOSKeePassAttachmentException(e);
        }
        return null;
    }

    @Override
    public Entry setBinaryProperty(SOSKeePassPath path, Entry entry, Path attachment) throws SOSKeePassAttachmentException {
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
        } catch (Throwable t) {
            throw new SOSKeePassAttachmentException(t);
        }
        return entry;
    }

    @Override
    public Entry setProperty(Entry entry, String name, String value) {
        entry.setProperty(name, value);
        return entry;
    }

    @Override
    public Entry createEntry(final String entryPath) throws SOSKeePassDatabaseException {
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
            getDatabase().write(getCredentials(), fos);
        } catch (Throwable e) {
            throw new SOSKeePassDatabaseException(e);
        }
    }

    private Entry createEntry(String[] entryPath) throws SOSKeePassDatabaseException {
        String method = "createEntry";

        KdbxDatabase d = (KdbxDatabase) getDatabase();
        Group lastGroup = d.getRootGroup();
        Entry entry = null;

        for (int i = 2; i < entryPath.length; i++) {
            String name = entryPath[i];

            if (i == entryPath.length - 1) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][addEntry]%s", method, name));
                }
                entry = addEntry(d, lastGroup, name);
            } else {
                List<Group> result = lastGroup.findGroups(name);
                if (result == null || result.size() == 0) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][addGroup]%s", method, name));
                    }

                    lastGroup = addGroup(d, lastGroup, name);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][useExistingGroup]%s", method, name));
                    }
                    lastGroup = result.get(0);
                }
            }
        }
        setDatabase(d);
        return entry;
    }

    private Group addGroup(KdbxDatabase d, Group parentGroup, String groupName) {
        Group g = d.newGroup(groupName);
        g.setIcon(d.newIcon(ICON_INDEX_NEW_GROUP));
        Group group = parentGroup.addGroup(g);
        return group;
    }

    private Entry addEntry(KdbxDatabase d, Group parentGroup, String entryName) {
        Entry e = d.newEntry(entryName);
        e.setIcon(d.newIcon(ICON_INDEX_NEW_ENTRY));

        Entry entry = parentGroup.addEntry(e);
        return entry;
    }

    private void setCredentials(final String pass, final Path keyFile) throws SOSKeePassDatabaseException {
        SOSKdbxCreds cred = new SOSKdbxCreds();
        cred.load(pass, keyFile);
        setCredentials(cred);
    }

}
