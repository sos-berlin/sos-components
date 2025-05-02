package com.sos.commons.credentialstore;

import com.sos.commons.credentialstore.exceptions.SOSCredentialStoreException;
import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class CredentialStoreArguments extends ASOSArguments {

    public static final String CLASS_KEY = "CREDENTIAL_STORE";

    public static final String ARG_NAME_FILE = "credential_store_file";
    public static final String ARG_NAME_KEY_FILE = "credential_store_key_file";
    public static final String ARG_NAME_PASSWORD = "credential_store_password";
    public static final String ARG_NAME_ENTRY_PATH = "credential_store_entry_path";
    public static final String ARG_NAME_KEEPASS_MODULE = "credential_store_keepass_module";

    // KeePass
    // TODO set <Path> instead of <String> for credentialStoreFile,credentialStoreKeyFile
    // it works but should be tested when value is not path conform, e.g. contains env variables names etc...
    private SOSArgument<String> file = new SOSArgument<>(ARG_NAME_FILE, false);
    private SOSArgument<String> keyFile = new SOSArgument<>(ARG_NAME_KEY_FILE, false);
    private SOSArgument<String> password = new SOSArgument<>(ARG_NAME_PASSWORD, false, DisplayMode.MASKED);
    private SOSArgument<String> entryPath = new SOSArgument<>(ARG_NAME_ENTRY_PATH, false);
    private SOSArgument<String> keePassModule = new SOSArgument<>(ARG_NAME_KEEPASS_MODULE, false, SOSKeePassDatabase.DEFAULT_MODULE.name());

    // Internal/Keepass
    private SOSArgument<SOSKeePassDatabase> keepassDatabase = new SOSArgument<>(null, false);
    private SOSArgument<org.linguafranca.pwdb.Entry<?, ?, ?, ?>> keepassDatabaseEntry = new SOSArgument<>(null, false);
    private SOSArgument<String> keepassAttachmentPropertyName = new SOSArgument<>(null, false);

    public SOSArgument<String> getFile() {
        return file;
    }

    public void setFile(String val) {
        file.setValue(val);
    }

    public SOSArgument<String> getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String val) {
        keyFile.setValue(val);
    }

    public SOSArgument<String> getPassword() {
        return password;
    }

    public void setPassword(String val) {
        password.setValue(val);
    }

    public SOSArgument<String> getKeePassModule() {
        return keePassModule;
    }

    public void setKeePassModule(String val) {
        keePassModule.setValue(val);
    }

    public SOSArgument<String> getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String val) {
        entryPath.setValue(val);
    }

    public SOSKeePassDatabase getKeepassDatabase() {
        return keepassDatabase.getValue();
    }

    public void setKeepassDatabase(SOSKeePassDatabase val) {
        keepassDatabase.setValue(val);
    }

    public org.linguafranca.pwdb.Entry<?, ?, ?, ?> getKeepassDatabaseEntry() {
        return keepassDatabaseEntry.getValue();
    }

    public void setKeepassDatabaseEntry(org.linguafranca.pwdb.Entry<?, ?, ?, ?> val) {
        keepassDatabaseEntry.setValue(val);
    }

    public String getKeepassAttachmentPropertyName() {
        return keepassAttachmentPropertyName.getValue();
    }

    public void setKeepassAttachmentPropertyName(String val) {
        keepassAttachmentPropertyName.setValue(val);
    }

    public CredentialStoreResolver newResolver() {
        return new CredentialStoreResolver();
    }

    public class CredentialStoreResolver {

        private SOSKeePassResolver resolver = null;

        private CredentialStoreResolver() {
            if (file.getValue() != null) {
                resolver = new SOSKeePassResolver(file.getValue(), keyFile.getValue(), password.getValue());
                resolver.setEntryPath(entryPath.getValue());
            }
        }

        public String resolve(String cs) throws SOSCredentialStoreException {
            return resolver == null ? cs : resolver.resolve(cs);
        }
    }

}
