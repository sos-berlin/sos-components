package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.joc.Globals;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.model.order.OrderV;


public class OrderMappingTest {

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        Path order = Paths.get("src/test/resources/failedOrder.json");
        OrderItem oItem = Globals.objectMapper.readValue(Files.readAllBytes(order), OrderItem.class);
        OrderV o = new OrderV();
        o.setArguments(oItem.getArguments());
        o.setAttachedState(oItem.getAttachedState());
        o.setOrderId(oItem.getId());
        List<HistoricOutcome> outcomes = oItem.getHistoricOutcomes();
        if (outcomes != null && !outcomes.isEmpty()) {
            o.setLastOutcome(outcomes.get(outcomes.size() - 1).getOutcome());
        }
        o.setHistoricOutcome(outcomes);
        o.setPosition(oItem.getWorkflowPosition().getPosition());
        Long scheduledFor = oItem.getScheduledFor();
        o.setState(OrdersHelper.getState(oItem.getState().getTYPE(), oItem.getIsSuspended()));
        o.setScheduledFor(scheduledFor);
        o.setWorkflowId(oItem.getWorkflowPosition().getWorkflowId());
        System.out.println(Globals.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(o));
    }

}
