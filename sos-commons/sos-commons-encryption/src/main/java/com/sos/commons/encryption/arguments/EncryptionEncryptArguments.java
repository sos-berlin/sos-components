package com.sos.commons.encryption.arguments;

import com.sos.commons.util.arguments.base.SOSArgument;

public class EncryptionEncryptArguments extends EncryptionArguments {

    public static final String CLASS_KEY = "ENCRYPTION_ENCRYPT";

    /** String, because it can use syntax such as cs:// to reference values from a credential store */
    private SOSArgument<String> certificate = new SOSArgument<>(ARG_NAME_ENCIPHERMENT_CERTIFICATE, false);

    public SOSArgument<String> getCertificate() {
        return certificate;
    }
}
