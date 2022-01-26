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
    private SOSArgument<String> credentialStoreFile = new SOSArgument<String>(ARG_NAME_FILE, false);
    private SOSArgument<String> credentialStoreKeyFile = new SOSArgument<String>(ARG_NAME_KEY_FILE, false);
    private SOSArgument<String> credentialStorePassword = new SOSArgument<String>(ARG_NAME_PASSWORD, false, DisplayMode.MASKED);
    private SOSArgument<String> credentialStoreEntryPath = new SOSArgument<String>(ARG_NAME_ENTRY_PATH, false);

    public SOSArgument<String> getCredentialStoreFile() {
        return credentialStoreFile;
    }

    public void setCredentialStoreFile(String val) {
        credentialStoreFile.setValue(val);
    }

    public SOSArgument<String> getCredentialStoreKeyFile() {
        return credentialStoreKeyFile;
    }

    public void setCredentialStoreKeyFile(String val) {
        credentialStoreKeyFile.setValue(val);
    }

    public SOSArgument<String> getCredentialStorePassword() {
        return credentialStorePassword;
    }

    public void setCredentialStorePassword(String val) {
        credentialStorePassword.setValue(val);
    }

    public SOSArgument<String> getCredentialStoreEntryPath() {
        return credentialStoreEntryPath;
    }

    public void setCredentialStoreEntryPath(String val) {
        credentialStoreEntryPath.setValue(val);
    }

    public SOSCredentialStoreResolver newResolver() {
        return new SOSCredentialStoreResolver();
    }

    public class SOSCredentialStoreResolver {

        private SOSKeePassResolver resolver;

        private SOSCredentialStoreResolver() {
            resolver = new SOSKeePassResolver(credentialStoreFile.getValue(), credentialStoreKeyFile.getValue(), credentialStorePassword.getValue());
            resolver.setEntryPath(credentialStoreEntryPath.getValue());
        }

        public String resolve(String cs) throws SOSCredentialStoreException {
            return resolver.resolve(cs);
        }
    }

}
