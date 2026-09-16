
package com.sos.auth.predicate.controller;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Agents {

    private final String prefix;

    public Agents(String parentPrefix) {
        prefix = parentPrefix + ":" + "agents";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
