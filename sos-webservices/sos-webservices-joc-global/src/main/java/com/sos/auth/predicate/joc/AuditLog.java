
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class AuditLog {

    private final String prefix;

    public AuditLog(String parentPrefix) {
        prefix = parentPrefix + ":" + "auditlog";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
