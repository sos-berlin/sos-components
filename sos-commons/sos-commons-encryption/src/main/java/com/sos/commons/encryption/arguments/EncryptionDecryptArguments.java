package com.sos.commons.encryption.arguments;

import java.security.PrivateKey;

import com.sos.commons.util.arguments.base.SOSArgument;

public class EncryptionDecryptArguments extends EncryptionArguments {

    public static final String CLASS_KEY = "ENCRYPTION_DECRYPT";

    /** String, because it can use syntax such as cs:// to reference values from a credential store */
    private SOSArgument<String> privateKeyPath = new SOSArgument<>(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, false);

    /** Internal argument holding the evaluated PrivateKey.<br />
     * This can be used as a transport argument after the PrivateKey has been loaded from the privateKeyPath. */
    private SOSArgument<PrivateKey> privateKey = new SOSArgument<>(null, false);

    public SOSArgument<String> getPrivateKeyPath() {
        return privateKeyPath;
    }

    /** Returns the internal PrivateKey argument.<br/>
     * Can be used internally or as a transport argument once the PrivateKey has been resolved from the privateKeyPath.
     *
     * @return the SOSArgument representing the evaluated PrivateKey */
    public SOSArgument<PrivateKey> getPrivateKey() {
        return privateKey;
    }

}
