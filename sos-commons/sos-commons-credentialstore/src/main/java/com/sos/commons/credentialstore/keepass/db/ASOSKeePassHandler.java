package com.sos.commons.credentialstore.keepass.db;

import java.nio.file.Path;
import java.util.List;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase.Module;
import com.sos.commons.credentialstore.keepass.SOSKeePassPath;
import com.sos.commons.credentialstore.keepass.db.kdb.SOSKdbHandler;
import com.sos.commons.credentialstore.keepass.db.kdbx.dom.SOSKdbxDOMHandler;
import com.sos.commons.credentialstore.keepass.db.kdbx.jaxb.SOSKdbxJAXBHandler;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassAttachmentException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;

public abstract class ASOSKeePassHandler {
  
    public static final int ICON_INDEX_NEW_GROUP = 48; // folder icon
    public static final int ICON_INDEX_NEW_ENTRY = 0; // key icon

    private Path keePassFile;
    private Database<?, ?, ?, ?> database;
    private Credentials credentials;

    public abstract boolean isKdbx();

    public abstract void load(final String pass, final Path keyFile) throws SOSKeePassDatabaseException;

    public abstract List<? extends Entry<?, ?, ?, ?>> getEntries();

    public abstract Entry<?, ?, ?, ?> getEntryByPath(final String path);

    public abstract Entry<?, ?, ?, ?> createEntry(String entryPath) throws SOSKeePassDatabaseException;

    public abstract void saveAs(Path file) throws SOSKeePassDatabaseException;

    public abstract byte[] getBinaryProperty(final Entry<?, ?, ?, ?> entry, String propertyName) throws SOSKeePassAttachmentException;

    public abstract Entry<?, ?, ?, ?> setBinaryProperty(SOSKeePassPath path, Entry<?, ?, ?, ?> entry, Path attachment)
            throws SOSKeePassDatabaseException;

    public abstract Entry<?, ?, ?, ?> setProperty(Entry<?, ?, ?, ?> entry, String name, String value) throws SOSKeePassDatabaseException;

    public static ASOSKeePassHandler newInstance(Path keePassFile, Module module) {
        ASOSKeePassHandler h = null;
        if (SOSKeePassDatabase.isKdbx(keePassFile)) {
            Module m = module == null ? SOSKeePassDatabase.DEFAULT_MODULE : module;
            switch (m) {
            case DOM:
                h = new SOSKdbxDOMHandler();
                break;
            case JAXB:
                h = new SOSKdbxJAXBHandler();
                break;
            }
        } else {
            h = new SOSKdbHandler();
        }
        h.keePassFile = keePassFile;
        return h;
    }

    public Path getKeePassFile() {
        return keePassFile;
    }

    protected void setDatabase(Database<?, ?, ?, ?> val) {
        database = val;
    }

    public Database<?, ?, ?, ?> getDatabase() {
        return database;
    }

    protected void setCredentials(Credentials val) {
        credentials = val;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public boolean isDirty() {
        return database != null && database.isDirty();
    }
}
