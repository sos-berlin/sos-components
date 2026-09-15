package com.sos.auth.predicate.controller;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Locks {
    
    private final String prefix;

    public Locks(String parentPrefix) {
        prefix = parentPrefix + ":" + "locks";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

}
