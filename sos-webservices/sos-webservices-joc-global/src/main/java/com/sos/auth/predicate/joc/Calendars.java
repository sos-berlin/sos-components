
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Calendars {

    private final String prefix;

    public Calendars(String parentPrefix) {
        prefix = parentPrefix + ":" + "calendars";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
