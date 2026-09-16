
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class FileTransfer {

    private final String prefix;

    public FileTransfer(String parentPrefix) {
        prefix = parentPrefix + ":" + "filetransfer";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }

}
