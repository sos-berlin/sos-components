package com.sos.commons.util.keystore;

import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sos.commons.util.SOSCollection;

public class KeyStoreContainer {

    private KeyStoreType type;

    private Path path;
    private KeyStore keyStore;

    private String password;

    // KeyStore entry
    private String keyPassword;
    private List<String> aliases;

    /** @apiNote the KeyStore object is determined internally from the specified path */
    public KeyStoreContainer(KeyStoreType type, Path path) {
        this(type);
        this.path = path;
    }

    /** @apiNote the KeyStore object is determined by a program and used internally */
    public KeyStoreContainer(KeyStoreType type, KeyStore keyStore) {
        this(type);
        this.keyStore = keyStore;
    }

    private KeyStoreContainer(KeyStoreType type) {
        this.type = type;
    }

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
        return keyPassword == null ? getPasswordChars() : keyPassword.toCharArray();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        if (path != null) {
            sb.append(" ").append(path);
        }
        if (password != null) {
            sb.append("  password=********");
        }
        if (!SOSCollection.isEmpty(aliases)) {
            sb.append("  aliases=" + aliases);
        }
        if (keyPassword != null) {
            sb.append("  keyPassword=********");
        }
        return sb.toString();
    }

    public static String toString(String prefix, KeyStoreContainer c) {
        return prefix + " " + c.toString();
    }

    public static String toString(String prefix, List<KeyStoreContainer> lc) {
        List<String> m = new ArrayList<>();
        for (KeyStoreContainer c : lc) {
            m.add(toString(prefix, c));
        }
        return String.join(", ", m);
    }

    /** TODO use this method in KeyStoreArguments when adding a new container<br/>
     * 
     * check password,keyPassword */
    @SuppressWarnings("unused")
    private boolean areEqual(KeyStoreContainer c1, KeyStoreContainer c2) throws Exception {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null || c2 == null) {
            return false;
        }

        // --- Paths
        if (c1.getPath() != null) {
            if (c2.getPath() == null) {
                return false;
            }
            return c1.getPath().equals(c2.getPath());
        } else if (c2.getPath() != null) {
            if (c1.getPath() == null) {
                return false;
            }
            return c1.getPath().equals(c2.getPath());
        }

        // --- KeyStore
        KeyStore ks1 = c1.getKeyStore();
        KeyStore ks2 = c2.getKeyStore();
        if (ks1 == null && ks2 == null) {
            return true;
        }
        if (ks1 == null || ks2 == null) {
            return false;
        }

        // aliases - extract (aliases can't be null)
        List<String> aliases1 = Collections.list(ks1.aliases());
        List<String> aliases2 = Collections.list(ks2.aliases());

        // aliases - sort and compare
        Collections.sort(aliases1);
        Collections.sort(aliases2);
        if (!aliases1.equals(aliases2)) {
            return false;
        }

        for (String alias : aliases1) {
            // certificates - compare
            Certificate cert1 = ks1.getCertificate(alias);
            Certificate cert2 = ks2.getCertificate(alias);

            if (cert1 == null && cert2 != null) {
                return false;
            }
            if (cert1 != null && !cert1.equals(cert2)) {
                return false;
            }

            // key - compare

            // Key key1 = ks1.getKey(alias, keyPassword);
            // Key key2 = ks2.getKey(alias, keyPassword);

            // if (key1 == null && key2 != null) {
            // return false;
            // }
            // if (key1 != null && !key1.equals(key2)) {
            // return false;
            // }
        }

        return true;
    }

}
