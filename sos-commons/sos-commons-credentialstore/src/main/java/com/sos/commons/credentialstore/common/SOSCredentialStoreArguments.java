package com.sos.commons.credentialstore.common;

import com.sos.commons.credentialstore.exceptions.SOSCredentialStoreException;
import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public class SOSCredentialStoreArguments extends ASOSArguments {

    public static final String ARG_NAME_FILE = "credential_store_file";
    public static final String ARG_NAME_KEY_FILE = "credential_store_key_file";
    public static final String ARG_NAME_PASSWORD = "credential_store_password";
    public static final String ARG_NAME_ENTRY_PATH = "credential_store_entry_path";

    // KeePass
    // TODO set <Path> instead of <String> for credentialStoreFile,credentialStoreKeyFile
    // it works but should be tested when value is not path conform, e.g. contains env variables names etc...
    private SOSArgument<String> file = new SOSArgument<String>(ARG_NAME_FILE, false);
    private SOSArgument<String> keyFile = new SOSArgument<String>(ARG_NAME_KEY_FILE, false);
    private SOSArgument<String> password = new SOSArgument<String>(ARG_NAME_PASSWORD, false, DisplayMode.MASKED);
    private SOSArgument<String> entryPath = new SOSArgument<String>(ARG_NAME_ENTRY_PATH, false);

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

    public SOSArgument<String> getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String val) {
        entryPath.setValue(val);
    }

    public SOSCredentialStoreResolver newResolver() {
        return new SOSCredentialStoreResolver();
    }

    public class SOSCredentialStoreResolver {

        private SOSKeePassResolver resolver;

        private SOSCredentialStoreResolver() {
            resolver = new SOSKeePassResolver(file.getValue(), keyFile.getValue(), password.getValue());
            resolver.setEntryPath(entryPath.getValue());
        }

        public String resolve(String cs) throws SOSCredentialStoreException {
            return resolver.resolve(cs);
        }
    }

}
