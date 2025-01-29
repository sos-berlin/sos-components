package com.sos.joc.classes.board;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.ProblemHelper;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.plan.PlanSchemaId;
import js7.data_for_java.plan.JPlanSchema;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.value.JExpression;
import js7.proxy.javaapi.JControllerApi;
import reactor.core.publisher.Flux;

public class PlanSchemas {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanSchemas.class);
    private static final Map<String, Either<Problem, JExpression>> planSchemas = Collections.unmodifiableMap(new HashMap<String, Either<Problem, JExpression>>() {

        private static final long serialVersionUID = 1L;

        {
            put("DailyPlan", JExpression.parse("match($js7OrderId, '#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*', '$1') ?"));
        }
    });
    
    
    // this method is called directly in onProxyCoupled
    public static void updatePlanSchemas(JControllerApi controllerApi, JControllerState currentState, String controller) {
        // TODO check which schema already deployed! 
        try {
            //currentState.pathToBoardState().get("").
            controllerApi.updateItems(Flux.fromStream(getPlanSchemas().map(JUpdateItemOperation::addOrChangeSimple))).thenAccept(e -> {
                if (e.isRight()) {
                    LOGGER.info("Plan schemas " + planSchemas.keySet().toString() + " submitted to " + controller);
                } else {
                    LOGGER.error("Error at submitting plan schemas " + planSchemas.keySet().toString() + " to " + controller + ": " + ProblemHelper.getErrorMessage(e.getLeft()));
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error at submitting plan schemas " + planSchemas.keySet().toString() + " to " + controller, e);
        }
    }
    
    private static Stream<JPlanSchema> getPlanSchemas() {
        return planSchemas.entrySet().stream().filter(e -> e.getValue().isRight()).map(e -> JPlanSchema.of(new PlanSchemaId(e.getKey()), e.getValue()
                .get(), Optional.empty()));
    }

//    private static Stream<JPlanSchema> getPlanSchemas(Set<String> schemas) {
//        Stream<Map.Entry<String, Either<Problem, JExpression>>> stream = planSchemas.entrySet().stream();
//        if (schemas != null) {
//            stream = stream.filter(e -> schemas.contains(e.getKey()));
//        }
//        return stream.filter(e -> e.getValue().isRight()).map(e -> JPlanSchema.of(new PlanSchemaId(e.getKey()), e.getValue().get(), Optional
//                .empty()));
//    }
    

}
