package com.sos.auth.predicate.controller;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Workflows {
    
    private final String prefix;

    public Workflows(String parentPrefix) {
        prefix = parentPrefix + ":" + "workflows";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
