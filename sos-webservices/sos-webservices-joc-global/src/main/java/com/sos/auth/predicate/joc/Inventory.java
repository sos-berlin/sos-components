
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Inventory {

    private final String prefix;

    public Inventory(String parentPrefix) {
        prefix = parentPrefix + ":" + "inventory";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }
    
    public Predicate<String> getDeploy() {
        return JocPermissionsPredicate.createPredicate(prefix, "deploy");
    }

}
