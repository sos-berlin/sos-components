
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class Cluster {

    private final String prefix;

    public Cluster(String parentPrefix) {
        prefix = parentPrefix + ":" + "cluster";
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }

}
