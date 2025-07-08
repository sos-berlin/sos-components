package com.sos.commons.util.keystore;

import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class KeyStoreFile {

    private KeyStoreType type;
    private Path path;
    private String password;

    // KeyStore
    private String keyPassword;
    private List<String> aliases;

    private KeyStore keyStore;

    public KeyStoreType getType() {
        return type;
    }

    public void setType(KeyStoreType val) {
        type = val;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore val) {
        keyStore = val;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path val) {
        path = val;
    }

    public String getPassword() {
        return password;
    }

    public char[] getPasswordChars() {
        // password == null ? "".toCharArray() : password.toCharArray();
        return password == null ? null : password.toCharArray();
    }

    public void setPassword(String val) {
        password = val;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public char[] getKeyPasswordChars() {
        return keyPassword == null ? null : keyPassword.toCharArray();
    }

    public void setKeyPassword(String val) {
        keyPassword = val;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> val) {
        aliases = val;
    }

    public static String toString(String prefix, KeyStoreFile f) {
        return prefix + " " + f.getType() + " " + f.getPath();
    }

    public static String toString(String prefix, List<KeyStoreFile> lf) {
        List<String> m = new ArrayList<>();
        for (KeyStoreFile f : lf) {
            m.add(toString(prefix, f));
        }
        return String.join(", ", m);
    }

}
