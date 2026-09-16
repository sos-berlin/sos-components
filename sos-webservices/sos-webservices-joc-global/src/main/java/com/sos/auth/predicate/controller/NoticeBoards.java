package com.sos.auth.predicate.controller;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class NoticeBoards {
    
    private final String prefix;

    public NoticeBoards(String parentPrefix) {
        prefix = parentPrefix + ":" + "noticeboards";
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }
    
    public Predicate<String> getPost() {
        return JocPermissionsPredicate.createPredicate(prefix, "post");
    }
    
    public Predicate<String> getDelete() {
        return JocPermissionsPredicate.createPredicate(prefix, "delete");
    }

}
