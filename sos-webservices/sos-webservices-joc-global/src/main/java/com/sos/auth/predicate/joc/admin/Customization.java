
package com.sos.auth.predicate.joc.admin;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Customization {

    private final String prefix;

    public Customization(String parentPrefix) {
        prefix = parentPrefix + ":" + "customization";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }
    
    public Predicate<String> getShare() {
        return JocPermissionsPredicate.createPredicate(prefix, "share");
    }

}
