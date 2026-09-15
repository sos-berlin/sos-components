
package com.sos.joc.model.security.configuration.predicate.joc.admin;

import java.util.function.Predicate;

import com.sos.joc.model.security.configuration.predicate.JocPermissionsPredicate;

public class Controllers {

    private final String prefix;

    public Controllers(String parentPrefix) {
        prefix = parentPrefix + ":" + "controllers";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }

}
