package com.sos.commons.encryption.arguments;

import java.security.PrivateKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.common.EncryptedValue;
import com.sos.commons.encryption.decrypt.Decrypt;
import com.sos.commons.util.arguments.base.SOSArgument;

public class EncryptionDecryptArguments extends EncryptionArguments {

    // Pattern for decrypt(enc:...)
    private static final Pattern DECRYPT_PATTERN = Pattern.compile("decrypt\\((" + EncryptionUtils.ENCRYPTION_IDENTIFIER + "[^\\)]+)\\)");

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

    /** Decrypts the value of a {@link SOSArgument} if needed and updates the argument with the decrypted value.
     * <p>
     * This method checks the argument's current value.<br />
     * If the value is encrypted (either starts with the standard encryption identifier or contains {@code decrypt(enc:...)} patterns), it will be decrypted
     * using the provided private key.<br />
     * The argument's value is updated only if decryption changes it.
     * <p>
     * This method internally calls {@link #decryptIfNeeded(String, String, PrivateKey)} for the actual decryption logic.
     *
     * @param arg the {@link SOSArgument} whose value should be decrypted; if {@code null} or its value is {@code null}, nothing is done
     * @param privKey the private key used for decryption; if {@code null}, no decryption occurs
     * @throws Exception if decryption fails */
    public static void decryptIfNeeded(SOSArgument<?> arg, PrivateKey privKey) throws Exception {
        if (arg == null || arg.getValue() == null) {
            return;
        }
        String v = arg.getValue().toString();
        String decrypted = decryptIfNeeded(arg.getName(), v, privKey);
        if (v.equals(decrypted)) {
            return;
        }
        arg.applyValue(decrypted);
    }

    /** Decrypts a property value if needed.
     * <p>
     * The method supports two cases:
     * <ol>
     * <li>If the property value starts with the standard encryption identifier (e.g., "enc:"), the entire value is decrypted using the provided private
     * key.</li>
     * <li>If the property value contains one or more occurrences of the pattern {@code decrypt(enc:...)} anywhere in the string, each matched encrypted value
     * is decrypted and replaced in the string.</li>
     * </ol>
     * If no encryption is detected, the original value is returned unchanged.
     *
     * @param propertyName the name of the property, used as context for decryption; if {@code null}, "decrypt" is used
     * @param propertyValue the value to decrypt; can contain a single encrypted value or multiple decrypt(enc:...) patterns
     * @param privKey the private key used for decryption; if {@code null}, the propertyValue is returned unchanged
     * @return the decrypted string if encryption patterns are detected, otherwise the original propertyValue
     * @throws Exception if decryption fails for any detected encrypted segment */
    public static String decryptIfNeeded(String propertyName, String propertyValue, PrivateKey privKey) throws Exception {
        if (propertyValue == null || privKey == null) {
            return propertyValue;
        }
        String pn = propertyName == null ? "decrypt" : propertyName;
        if (propertyValue.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER)) {
            return Decrypt.decrypt(EncryptedValue.getInstance(pn, propertyValue), privKey);
        }

        // 2. Matcher for decrypt(enc:...)
        Matcher matcher = DECRYPT_PATTERN.matcher(propertyValue);
        StringBuffer result = new StringBuffer();
        boolean found = false;

        while (matcher.find()) {
            found = true;
            String encPart = matcher.group(1); // "enc:12345"
            String decrypted = Decrypt.decrypt(EncryptedValue.getInstance(pn, encPart), privKey);
            matcher.appendReplacement(result, decrypted);
        }
        if (!found) {
            return propertyValue;
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
