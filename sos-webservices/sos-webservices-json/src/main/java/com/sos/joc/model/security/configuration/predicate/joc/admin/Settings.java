
package com.sos.joc.model.security.configuration.predicate.joc.admin;

import java.util.function.Predicate;

import com.sos.joc.model.security.configuration.predicate.JocPermissionsPredicate;

public class Settings {

    private final String prefix;

    public Settings(String parentPrefix) {
        prefix = parentPrefix + ":" + "settings";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }

}
