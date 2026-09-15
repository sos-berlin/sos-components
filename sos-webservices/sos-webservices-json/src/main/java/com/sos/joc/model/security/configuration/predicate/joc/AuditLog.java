
package com.sos.joc.model.security.configuration.predicate.joc;

import java.util.function.Predicate;

import com.sos.joc.model.security.configuration.predicate.JocPermissionsPredicate;

public class AuditLog {

    private final String prefix;

    public AuditLog(String parentPrefix) {
        prefix = parentPrefix + ":" + "auditlog";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
