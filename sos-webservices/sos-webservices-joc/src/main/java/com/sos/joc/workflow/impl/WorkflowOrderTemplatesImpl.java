package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.workflow.OrderParameterisations;
import com.sos.joc.model.workflow.Schedules;
import com.sos.joc.model.workflow.WorkflowPathFilter;
import com.sos.joc.workflow.resource.IWorkflowOrderTemplates;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("workflow")
public class WorkflowOrderTemplatesImpl extends JOCResourceImpl implements IWorkflowOrderTemplates {

    private static final String API_CALL = "./workflow/order_templates";
    private static final Predicate<String> hasOrderParameterisationPattern = Pattern.compile("\"orderParameterisations\"\\s*:").asPredicate();
    private static final Predicate<DBItemInventoryReleasedConfiguration> hasOrderParameterisation = s -> hasOrderParameterisationPattern.test(s
            .getContent());

    @Override
    public JOCDefaultResponse postOrderTemplates(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, WorkflowPathFilter.class);
            WorkflowPathFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowPathFilter.class);
            String controllerId = workflowFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken)
                    .getWorkflows().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Schedules entity = new Schedules();
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(connection);
            String workflowName = JocInventory.pathToName(workflowFilter.getWorkflowPath());
            List<DBItemInventoryReleasedConfiguration> schedules = dbLayer.getUsedReleasedSchedulesByWorkflowName(workflowName);
            
            entity.setSchedules(schedules.stream().filter(hasOrderParameterisation).map(item -> {
                try {
                    Schedule s = (Schedule) JocInventory.content2IJSObject(item.getContent(), ConfigurationType.SCHEDULE);
                    if (s.getOrderParameterisations() != null && !s.getOrderParameterisations().isEmpty()) {
                        if ((s.getWorkflowName() != null && workflowName.equals(s.getWorkflowName())) || (s.getWorkflowNames() != null && s
                                .getWorkflowNames().contains(workflowName))) {
                            OrderParameterisations op = new OrderParameterisations();
                            op.setOrderParameterisations(s.getOrderParameterisations());
                            op.setName(JocInventory.pathToName(item.getPath()));
                            op.setPath(item.getPath());
                            op.setTitle(s.getTitle());
                            return op;
                        }
                    }
                } catch (IOException e) {
                    //
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));

            entity.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
}
