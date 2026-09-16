
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Encipherment {

    private final String prefix;

    public Encipherment(String parentPrefix) {
        prefix = parentPrefix + ":" + "encipherment";
    }
    
    public Predicate<String> getEncrypt() {
        return JocPermissionsPredicate.createPredicate(prefix, "encrypt");
    }
    
}
