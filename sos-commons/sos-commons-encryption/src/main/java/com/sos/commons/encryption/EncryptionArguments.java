package com.sos.commons.encryption;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class EncryptionArguments extends ASOSArguments {

    public static final String CLASS_KEY = "ENCRYPTION";

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";

    private SOSArgument<String> certificate = new SOSArgument<>(ARG_NAME_ENCIPHERMENT_CERTIFICATE, false);
    private SOSArgument<String> privateKeyPath = new SOSArgument<>(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, false);

    public SOSArgument<String> getCertificate() {
        return certificate;
    }

    public SOSArgument<String> getPrivateKeyPath() {
        return privateKeyPath;
    }

    /** Checks whether the value of the given argument is encrypted.
     * <p>
     * A value is considered encrypted if it is not null and starts with the encryption identifier defined in {@link EncryptionUtils#ENCRYPTION_IDENTIFIER},
     * typically "enc:".
     *
     * @param arg the {@link SOSArgument} containing the string value to check; may be null
     * @return {@code true} if the argument is non-null and its value starts with the encryption identifier, {@code false} otherwise */
    public static boolean hasEncryptedValue(SOSArgument<String> arg) {
        return arg == null ? false : hasEncryptedValue(arg.getValue());
    }

    /** Checks whether the given value is encrypted.
     * <p>
     * A value is considered encrypted if it is not null and starts with the encryption identifier defined in {@link EncryptionUtils#ENCRYPTION_IDENTIFIER},
     * typically "enc:".
     *
     * @param val the string value to check
     * @return {@code true} if the value is non-null and starts with the encryption identifier, {@code false} otherwise */
    public static boolean hasEncryptedValue(String val) {
        return (val != null && val.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER));
    }

}
