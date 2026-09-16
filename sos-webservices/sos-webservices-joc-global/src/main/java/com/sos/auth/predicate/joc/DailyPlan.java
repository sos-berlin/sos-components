
package com.sos.auth.predicate.joc;

import java.util.function.Predicate;

import com.sos.auth.predicate.JocPermissionsPredicate;

public class DailyPlan {

    private final String prefix;

    public DailyPlan(String parentPrefix) {
        prefix = parentPrefix + ":" + "dailyplan";
    }
    
    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getManage() {
        return JocPermissionsPredicate.createPredicate(prefix, "manage");
    }
    
}
