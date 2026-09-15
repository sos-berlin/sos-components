
package com.sos.joc.model.security.configuration.predicate.joc;

import java.util.function.Predicate;

import com.sos.joc.model.security.configuration.predicate.JocPermissionsPredicate;

public class Documentations {

    private final String prefix;

    public Documentations(String parentPrefix) {
        prefix = parentPrefix + ":" + "documentations";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }
    
}
