package com.sos.joc.classes.board;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.ProblemHelper;

import js7.data.plan.PlanSchemaId;
import js7.data.value.StringValue;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.plan.JPlanSchema;
import js7.data_for_java.plan.JPlanSchemaState;
import js7.data_for_java.value.JExprFunction;
import js7.proxy.javaapi.JControllerApi;
import reactor.core.publisher.Flux;

public class PlanSchemas {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanSchemas.class);
    public static final String defaultPlanSchemaId = "DailyPlan"; 
    private static final Map<String, JPlanSchema> planSchemas = Collections.unmodifiableMap(new HashMap<String, JPlanSchema>() {

        private static final long serialVersionUID = 1L;

        {
            put(defaultPlanSchemaId, JPlanSchema.of(PlanSchemaId.of(defaultPlanSchemaId), 
                    Optional.of(JExprFunction.apply("(day) => $day < $thresholdDay").asScala()),
                    Collections.singletonMap("thresholdDay", StringValue.empty())));
        }
    });

    // this method is called directly in onProxyCoupled
    public static void updatePlanSchemas(JControllerApi controllerApi, JControllerState currentState, String controller) {
        try {
            Map<Boolean, List<JPlanSchema>> planSchemasMap = getGroupedPlanSchemas(currentState);
            if (planSchemasMap.containsKey(Boolean.FALSE)) { // new schemas
                String schemaStr = planSchemasMap.get(Boolean.FALSE).size() == 1 ? "schema" : "schemas";
                String newPlanSchemas = getPlanSchemasToString(planSchemasMap.get(Boolean.FALSE));
                controllerApi.updateItems(Flux.fromStream(planSchemasMap.get(Boolean.FALSE).stream().map(JUpdateItemOperation::addOrChangeSimple)))
                        .thenAccept(e -> {
                            if (e.isRight()) {
                                LOGGER.info(String.format("Plan %s %s submitted to %s", schemaStr, newPlanSchemas, controller));
                            } else {
                                LOGGER.error(String.format("Error at submitting plan %s %s to %s: %s", schemaStr, newPlanSchemas, controller,
                                        ProblemHelper.getErrorMessage(e.getLeft())));
                            }
                        });
            }
            if (planSchemasMap.containsKey(Boolean.TRUE)) { // already known schemas
                String alreadyPlanSchemas = getPlanSchemasToString(planSchemasMap.get(Boolean.TRUE));
                String schemaStr = planSchemasMap.get(Boolean.TRUE).size() == 1 ? "schema" : "schemas";
                LOGGER.info(String.format("Plan %s %s already submitted to %s", schemaStr, alreadyPlanSchemas, controller));
            }
        } catch (Exception e) {
            String schemaStr = planSchemas.size() == 1 ? "schema" : "schemas";
            LOGGER.error(String.format("Error at submitting plan %s %s to %s", schemaStr, getPlanSchemasToString(planSchemas.keySet().stream()),
                    controller), e);
        }
    }
    
    public static boolean dailyPlanPlanSchemaExists(JControllerState currentState) {
        return currentState.idToPlanSchemaState().containsKey(PlanSchemaId.of(defaultPlanSchemaId));
    }
    
    public static PlanSchemaId getDailyPlanPlanSchemaIfExists(JControllerState currentState) {
        PlanSchemaId id = PlanSchemaId.of(defaultPlanSchemaId);
        return currentState.idToPlanSchemaState().containsKey(id) ? id : PlanSchemaId.Global;
    }
    
    private static Map<Boolean, List<JPlanSchema>> getGroupedPlanSchemas(JControllerState currentState) {
        Function<JPlanSchema, JPlanSchema> cloneWithoutRevision = ps -> JPlanSchema.of(ps.id(), ps.planIsClosedFunction(), ps
                .namedValues());
        Set<JPlanSchema> knownPlanSchemas = currentState.idToPlanSchemaState().values().stream().filter(p -> !p.asScala().isGlobal()).map(
                JPlanSchemaState::item).map(cloneWithoutRevision).collect(Collectors.toSet());
        return planSchemas.values().stream().collect(Collectors.groupingBy(knownPlanSchemas::contains));
    }
    
    private static String getPlanSchemasToString(Collection<JPlanSchema> psIds) {
        return getPlanSchemasToString(psIds.stream().map(JPlanSchema::id).map(PlanSchemaId::string));
    }
    
    private static String getPlanSchemasToString(Stream<String> psIds) {
        return psIds.collect(Collectors.joining("', '", "'", "'"));
    }

}
