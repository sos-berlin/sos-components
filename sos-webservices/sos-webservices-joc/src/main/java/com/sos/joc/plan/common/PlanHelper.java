package com.sos.joc.plan.common;

import com.sos.joc.model.plan.Plan;
import com.sos.joc.model.plan.PlansFilter;

import js7.data.plan.PlanId;
import js7.data_for_java.plan.JPlan;

public class PlanHelper {
    
    public static com.sos.joc.model.plan.Plan getFilteredPlan(PlanId pId, JPlan jp, PlansFilter filter) {
        Plan plan = new Plan();
        boolean isClosed = jp.isClosed();
        if (isClosed && filter.getOnlyOpenPlans()) {
            return null;
        }
        if (!isClosed && filter.getOnlyClosedPlans()) {
            return null;
        }
        plan.setClosed(isClosed);
        
        String pSchemaId = pId.planSchemaId().string();
        if (filter.getPlanSchemaIds() != null && !filter.getPlanSchemaIds().isEmpty() && !filter.getPlanSchemaIds().contains(pSchemaId)) {
            return null;
        }
        
        String plankey = pId.planKey() == null ? null : pId.planKey().string(); //is maybe null for global schema
        // keys are ignored for global plan schema
        if (filter.getNoticeSpaceKeys() != null && !filter.getNoticeSpaceKeys().isEmpty() && !"Global".equalsIgnoreCase(pSchemaId)) {
            if (!filter.getNoticeSpaceKeys().contains(plankey)) {
                // looking for globs inside filter.getNoticeSpaceKeys()
                if (!filter.getNoticeSpaceKeys().stream().filter(pk -> pk.contains("*") || pk.contains("?")).anyMatch(pk -> plankey.matches(pk.replace("*",
                        ".*").replace("?", ".")))) {
                    return null;
                }
            }
        }
        
        com.sos.joc.model.plan.PlanId planId = new com.sos.joc.model.plan.PlanId();
        planId.setPlanSchemaId(pSchemaId);
        planId.setNoticeSpaceKey(plankey);
        plan.setPlanId(planId);
        plan.setNumOfNoticeBoards(jp.toPlannedBoard().size());
        plan.setNoticeBoards(null);
        return plan;
    }

}
