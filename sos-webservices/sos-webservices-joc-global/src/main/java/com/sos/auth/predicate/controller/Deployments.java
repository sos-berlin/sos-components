package com.sos.auth.predicate.controller;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Deployments {
    
    private final String prefix;

    public Deployments(String parentPrefix) {
        prefix = parentPrefix + ":" + "deployment";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }
    
    public Predicate<String> getDeploy() {
        return JocPermissionsPredicate.createPredicate(prefix, "deploy");
    }

}
