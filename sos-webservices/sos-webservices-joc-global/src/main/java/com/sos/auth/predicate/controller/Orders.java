package com.sos.auth.predicate.controller;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Orders {

    private final String prefix;

    public Orders(String parentPrefix) {
        prefix = parentPrefix + ":" + "orders";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }
    
    public Predicate<String> getCreate() {
        return JocPermissionsPredicate.createPredicate(prefix, "create");
    }
    
    public Predicate<String> getCancel() {
        return JocPermissionsPredicate.createPredicate(prefix, "cancel");
    }
    
    public Predicate<String> getModify() {
        return JocPermissionsPredicate.createPredicate(prefix, "modify");
    }
    
    public Predicate<String> getSuspendResume() {
        return JocPermissionsPredicate.createPredicate(prefix, "suspend_resume");
    }
    
    public Predicate<String> getResumeFailed() {
        return JocPermissionsPredicate.createPredicate(prefix, "resume_failed");
    }
    
    public Predicate<String> getConfirm() {
        return JocPermissionsPredicate.createPredicate(prefix, "confirm");
    }
    
    public Predicate<String> getManagePositions() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage_positions");
    }
    
}
