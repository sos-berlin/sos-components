
package com.sos.joc.model.security.configuration.predicate.joc;

import java.util.function.Predicate;

import com.sos.joc.model.security.configuration.predicate.JocPermissionsPredicate;

public class Calendars {

    private final String prefix;

    public Calendars(String parentPrefix) {
        prefix = parentPrefix + ":" + "calendars";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
