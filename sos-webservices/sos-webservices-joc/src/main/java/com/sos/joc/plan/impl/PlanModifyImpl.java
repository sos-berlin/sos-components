package com.sos.joc.plan.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlansModifyFilter;
import com.sos.joc.plan.resource.IPlanModify;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.plan.Plan;
import js7.data.plan.PlanId;
import js7.data.plan.PlanKey;
import js7.data.plan.PlanSchemaId;
import js7.data.plan.PlanStatus;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.plan.JPlan;
import js7.data_for_java.plan.JPlanStatus;
import js7.proxy.javaapi.JControllerProxy;

@Path("plans")
public class PlanModifyImpl extends JOCResourceImpl implements IPlanModify {

    private static final String API_CALL = "./plans/";
    //private static final Logger LOGGER = LoggerFactory.getLogger(PlanModifyImpl.class);
    
    private enum Action {
        OPEN, CLOSE, DELETE
    }

    @Override
    public JOCDefaultResponse openPlan(String accessToken, byte[] filterBytes) {
        return modifyPlan(Action.OPEN, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse closePlan(String accessToken, byte[] filterBytes) {
        return modifyPlan(Action.CLOSE, accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse deletePlan(String accessToken, byte[] filterBytes) {
        return modifyPlan(Action.DELETE, accessToken, filterBytes);
    }
    
    private JOCDefaultResponse modifyPlan(Action action, String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, PlansModifyFilter.class);
            PlansModifyFilter filter = Globals.objectMapper.readValue(filterBytes, PlansModifyFilter.class);
            String controllerId = filter.getControllerId();
            // TODO new permission for plans
            JOCDefaultResponse response = initPermissions(controllerId, true);
            if (response != null) {
                return response;
            }

            JControllerProxy proxy = Proxy.of(controllerId);
            PlanStatus status = getChangeStatus(action);
//            filter.setPlanSchemaIds(Collections.singleton(PlanSchemas.defaultPlanSchemaId));
//            
//            switch (action) {
//            case CLOSE:
//                filter.setOnlyOpenPlans(true);
//                filter.setOnlyClosedPlans(false);
//                break;
//            case DELETE:
//            case OPEN:
//                filter.setOnlyOpenPlans(false);
//                filter.setOnlyClosedPlans(true);
//                break;
//            }
            
//            Map<PlanStatus, List<JPlan>> statusToPlan = proxy.currentState().toPlan().entrySet().stream().map(e -> PlanHelper.getFilteredJPlan(e.getKey(), e
//                    .getValue(), filter)).filter(Objects::nonNull).collect(Collectors.groupingBy(JPlan::state));
//            
//            switch (action) {
//            case OPEN:
//                if (statusToPlan.keySet().contains(JPlanStatus.Open())) {
//                    //
//                }
//                break;
//            case CLOSE:
//                if (statusToPlan.keySet().contains(JPlanStatus.Closed()) || statusToPlan.keySet().contains(JPlanStatus.Deleted())) {
//                    //
//                }
//                break;
//            case DELETE:
//                break;
//            }
            
            Set<PlanId> planIds = filter.getPlanIds().stream().map(pId -> new PlanId(PlanSchemaId.of(pId.getPlanSchemaId()), PlanKey.of(pId
                    .getNoticeSpaceKey()))).collect(Collectors.toSet());

            Stream<JPlan> knownPlans = proxy.currentState().toPlan().entrySet().stream().filter(e -> planIds.contains(e.getKey())).map(
                    Map.Entry::getValue);
            Predicate<JPlan> isClosed = JPlan::isClosed;
            Stream<PlanId> planIdStream = Stream.empty();

            switch (action) {
            case OPEN:
                // only known closed plans and unknown plans can be open
                // remove all open known plans from requested plans
                planIds.removeAll(knownPlans.filter(isClosed.negate()).map(JPlan::asScala).map(Plan::id).collect(Collectors.toList()));
                planIdStream = planIds.stream();
                break;
            case CLOSE:
                // only known open plans can be closed
                planIdStream = knownPlans.filter(isClosed.negate()).map(JPlan::asScala).map(Plan::id);
                break;
            case DELETE:
                // only known finished plans (closed and without orders) can be deleted
                Map<Boolean, Set<JPlan>> finishedToPlans = knownPlans.filter(isClosed).collect(Collectors.groupingBy(jp -> jp.orderIds().isEmpty(), Collectors.toSet()));
                //planIdStream = knownPlans.filter(isClosed).filter(jp -> jp.orderIds().isEmpty()).map(JPlan::asScala).map(Plan::id);
                planIdStream = finishedToPlans.getOrDefault(Boolean.TRUE, Collections.emptySet()).stream().map(JPlan::asScala).map(Plan::id);
                //finishedToPlans.getOrDefault(Boolean.FALSE, Collections.emptySet()).stream().map(jp -> jp.asScala().id());
                break;
            }
            
            planIdStream.map(pId -> JControllerCommand.changePlan(pId, status)).map(JControllerCommand::apply).forEach(command -> proxy.api()
                    .executeCommand(command).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId)));
            
//            List<JControllerCommand> commands = proxy.currentState().toPlan().entrySet().stream().map(e -> PlanHelper.getFilteredJPlan(e.getKey(), e
//                    .getValue(), filter)).filter(Objects::nonNull).map(JPlan::asScala).map(Plan::id).map(pId -> JControllerCommand.changePlan(pId,
//                            status)).map(JControllerCommand::apply).collect(Collectors.toList());
//
//            if (commands.size() == 1) {
//                proxy.api().executeCommand(commands.iterator().next()).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken,
//                        getJocError(), controllerId));
//            } else if (commands.size() > 1) {
//                proxy.api().executeCommand(JControllerCommand.batch(commands)).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken,
//                        getJocError(), controllerId));
//            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private PlanStatus getChangeStatus(Action action) {
        switch (action) {
        case CLOSE: 
            return JPlanStatus.Closed();
        case OPEN:
            return JPlanStatus.Open();
        default: //case DELETE
            return JPlanStatus.Deleted();
        }
    }

}