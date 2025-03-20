package com.sos.joc.plan.common;

import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;

import com.sos.joc.model.plan.Plan;
import com.sos.joc.model.plan.PlansFilter;

import js7.data.plan.PlanId;
import js7.data.plan.PlanStatus;
import js7.data_for_java.plan.JPlan;
import js7.data_for_java.plan.JPlanStatus;

public class PlanHelper {
    
    public static com.sos.joc.model.plan.Plan getFilteredPlan(PlanId pId, JPlan jp, PlansFilter filter) {
        return mapJPlanToPlan(getFilteredJPlan(pId, jp, filter));
    }
    
    public static JPlan getFilteredJPlan(PlanId pId, JPlan jp, PlansFilter filter) {
        boolean isClosed = jp.isClosed();
        if (isClosed && filter.getOnlyOpenPlans()) {
            return null;
        }
        if (!isClosed && filter.getOnlyClosedPlans()) {
            return null;
        }
        
        Predicate<JPlan> isDeleted = jp1 -> JPlanStatus.Deleted().equals(jp1.asScala().status());
        if (isDeleted.test(jp)) {
            return null;
        }
        
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
        
        return jp;
    }
    
    private static com.sos.joc.model.plan.Plan mapJPlanToPlan(JPlan jp) {
        if (jp == null) {
            return null;
        }
        Plan plan = new Plan();
        plan.setClosed(jp.isClosed());
        //plan.setStatus(jp.asScala().status().toString());
        PlanStatus state = jp.asScala().status();
//        if (jp.isClosed()) {
//           if (JPlanStatus.Deleted().equals(state)) {
//               //deleted
//           } else if (JPlanStatus.Closed().equals(state)) {
//               //closed
//           } else {
//               //finished
//               //TODO how I know since when the plan is finished?
//               //toString() -> Finished(2025-03-20T06:26:54.891Z)
//               String dateStr = jp.asScala().status().toString().replaceFirst("Finished\\((.*)\\)", "$1");
//               Date date = Date.from(Instant.parse(dateStr));
//           }
//        }
        plan.setPlanId(mapJPlanIdToPlanId(jp.asScala().id()));
        plan.setNumOfNoticeBoards(jp.toPlannedBoard().size());
        plan.setNoticeBoards(null);
        return plan;
    }
    
    private static com.sos.joc.model.plan.PlanId mapJPlanIdToPlanId(PlanId pId) {
        if (pId == null) {
            return null;
        }
        com.sos.joc.model.plan.PlanId planId = new com.sos.joc.model.plan.PlanId();
        planId.setPlanSchemaId(pId.planSchemaId().string());
        planId.setNoticeSpaceKey(pId.planKey().string());
        return planId;
    }

}
