
package com.sos.joc.model.security.configuration.predicate.joc.admin;

import java.util.function.Predicate;

import com.sos.joc.model.security.configuration.predicate.JocPermissionsPredicate;

public class Accounts {

    private final String prefix;

    public Accounts(String parentPrefix) {
        prefix = parentPrefix + ":" + "accounts";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }

}
