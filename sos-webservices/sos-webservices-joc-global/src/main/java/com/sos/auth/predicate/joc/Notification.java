
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Notification {

    private final String prefix;

    public Notification(String parentPrefix) {
        prefix = parentPrefix + ":" + "notification";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }

}
