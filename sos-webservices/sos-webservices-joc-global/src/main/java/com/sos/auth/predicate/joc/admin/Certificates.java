
package com.sos.auth.predicate.joc.admin;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Certificates {

    private final String prefix;

    public Certificates(String parentPrefix) {
        prefix = parentPrefix + ":" + "certificates";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }
    
}
