package com.sos.commons.encryption.arguments;

import com.sos.commons.util.arguments.base.SOSArgument;

public class EncryptionDecryptArguments extends EncryptionArguments {

    public static final String CLASS_KEY = "ENCRYPTION_DECRYPT";
    
    private SOSArgument<String> privateKeyPath = new SOSArgument<>(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, false);

    public SOSArgument<String> getPrivateKeyPath() {
        return privateKeyPath;
    }

}
