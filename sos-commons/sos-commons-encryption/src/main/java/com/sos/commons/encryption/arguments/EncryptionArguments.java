package com.sos.commons.encryption.arguments;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public abstract class EncryptionArguments extends ASOSArguments {

    /** The Certificate/Public Key is used to encrypt a secret<br/>
     * String, because it can use syntax such as cs:// to reference values from a credential store */
    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    /** The Private Key is used to decrypt an encrypted secret.<br />
     * String, because it can use syntax such as cs:// to reference values from a credential store */
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";

    /** Checks whether the value of the given argument is encrypted.
     * <p>
     * A value is considered encrypted if it is not null and starts with the encryption identifier defined in {@link EncryptionUtils#ENCRYPTION_IDENTIFIER},
     * typically "enc:".
     *
     * @param arg the {@link SOSArgument} containing the string value to check; may be null
     * @return {@code true} if the argument is non-null and its value starts with the encryption identifier, {@code false} otherwise */
    public static boolean hasEncryptedValue(SOSArgument<?> arg) {
        return arg == null ? false : hasEncryptedValue(arg.getValue() + "");
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
