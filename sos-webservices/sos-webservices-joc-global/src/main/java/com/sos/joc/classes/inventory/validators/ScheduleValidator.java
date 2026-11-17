package com.sos.joc.classes.inventory.validators;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.order.BlockPosition;

public class ScheduleValidator {

    public static void validate(IConfigurationObject config, InventoryDBLayer dbLayer) throws Exception {
        Schedule schedule = (Schedule) config;
        validateCalendarRefs(schedule, dbLayer);

        schedule = JocInventory.setWorkflowNames(schedule);
        int namesSize = schedule.getWorkflowNames().size();
        if (namesSize == 0) {
            throw new JocConfigurationException("Missing assigned Workflows");
        }
        String position = "$.workflowNames";
        for (String workflowName : schedule.getWorkflowNames()) {
            String json = Validator.validateWorkflowRef(workflowName, dbLayer, position);
            Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
            Requirements r = w.getOrderPreparation();
            if (namesSize >= JocInventory.SCHEDULE_MIN_MULTIPLE_WORKFLOWS_SIZE) {// check only multiple workflows
                if (orderPreparationHasRequiredParameters(w.getOrderPreparation())) {
                    throw new JocConfigurationException(String.format(
                            "%s: Multiple workflows with required order variables are not permitted: schedule=%s, workflowName=%s", position, schedule
                                    .getPath(), workflowName));
                }
            }
            validateOrderParameterisations(schedule.getOrderParameterisations(), r, workflowName, w, "$.orderParameterisations");
        }
        validateTimeZone(schedule.getCalendars());
    }

    public static void revalidate(IConfigurationObject config, Map<ConfigurationType, Set<String>> invObjNames,
            Map<String, String> workflowJsonsByName) throws Exception {
        Schedule schedule = (Schedule) config;
        validateCalendarRefs(schedule, invObjNames.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, Collections.emptySet()), invObjNames
                .getOrDefault(ConfigurationType.NONWORKINGDAYSCALENDAR, Collections.emptySet()));

        schedule = JocInventory.setWorkflowNames(schedule);
        int namesSize = schedule.getWorkflowNames().size();
        if (namesSize == 0) {
            throw new JocConfigurationException("Missing assigned Workflows");
        }
        String position = "$.workflowNames";
        for (String workflowName : schedule.getWorkflowNames()) {
            String json = Validator.validateWorkflowRef(workflowName, workflowJsonsByName, position);
            Workflow w = Globals.objectMapper.readValue(json, Workflow.class);
            Requirements r = w.getOrderPreparation();
            if (namesSize >= JocInventory.SCHEDULE_MIN_MULTIPLE_WORKFLOWS_SIZE) {// check only multiple workflows
                if (r != null && r.getParameters() != null && r.getParameters().getAdditionalProperties() != null && r.getParameters()
                        .getAdditionalProperties().size() > 0) {
                    throw new JocConfigurationException(String.format(
                            "%s: Multiple workflows with order variables are not permitted: schedule=%s, workflowName=%s, %s order variables",
                            position, schedule.getPath(), workflowName, r.getParameters().getAdditionalProperties().size()));
                }
            }
            validateOrderParameterisations(schedule.getOrderParameterisations(), r, workflowName, w, "$.orderParameterisations");
        }
        validateTimeZone(schedule.getCalendars());
    }

    private static void validateCalendarRefs(Schedule schedule, InventoryDBLayer dbLayer) throws SOSHibernateException, JocConfigurationException {
        List<String> calendarNames = getCalendarRefsInSchedule(schedule).distinct().collect(Collectors.toList());
        Validator.validateCalendarRefs(calendarNames, dbLayer);
    }

    private static void validateCalendarRefs(Schedule schedule, Set<String> allWorkingDaysCalendarNames, Set<String> allNonWorkingDaysCalendarNames)
            throws JocConfigurationException {
        Set<String> calendarNames = getCalendarRefsInSchedule(schedule).collect(Collectors.toSet());

        calendarNames.removeAll(allWorkingDaysCalendarNames);
        calendarNames.removeAll(allNonWorkingDaysCalendarNames);
        if (!calendarNames.isEmpty()) {
            throw new JocConfigurationException("Missing assigned Calendars: " + calendarNames.toString());
        }
    }

    private static Stream<String> getCalendarRefsInSchedule(Schedule schedule) {
        Stream<String> calendarNames = schedule.getCalendars().stream().map(AssignedCalendars::getCalendarName);
        calendarNames = Stream.concat(calendarNames, Optional.ofNullable(schedule.getNonWorkingDayCalendars()).map(s -> s.stream().map(
                AssignedNonWorkingDayCalendars::getCalendarName)).orElse(Stream.empty()));
        calendarNames = Stream.concat(calendarNames, schedule.getCalendars().stream().map(AssignedCalendars::getExcludes).filter(Objects::nonNull)
                .map(Frequencies::getNonWorkingDayCalendars).filter(Objects::nonNull).flatMap(Set::stream));
        return calendarNames;
    }

    private static void validateTimeZone(List<AssignedCalendars> cals) {
        if (cals != null) {
            String position = "$.calendars";
            AtomicInteger index = new AtomicInteger(0);
            cals.stream().map(AssignedCalendars::getTimeZone).filter(Objects::nonNull).forEach(t -> Validator.validateTimeZone(t, position + "["
                    + index.getAndIncrement() + "]"));
        }
    }

    private static void validateOrderParameterisations(List<OrderParameterisation> variableSets, Requirements orderPreparation, String workflowName,
            Workflow workflow, String position) throws JocConfigurationException {
        boolean allowEmptyArguments = ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc());
        if (variableSets != null) {
            if (variableSets.size() != variableSets.stream().map(OrderParameterisation::getOrderName).distinct().mapToInt(e -> 1).sum()) {
                throw new JocConfigurationException(position + ": Order names has to be unique");
            }
            variableSets.stream().map(OrderParameterisation::getVariables).forEach(v -> {
                if (v == null) {
                    v = new Variables();
                }
                try {
                    OrdersHelper.checkArguments(v, orderPreparation, allowEmptyArguments);
                } catch (Exception e1) {
                    throw new JocConfigurationException(position + ": " + e1.getMessage());
                }
            });
            Predicate<OrderPositions> hasPositionSetting = p -> p.getStartPosition() != null || (p.getEndPositions() != null && !p.getEndPositions()
                    .isEmpty()) || p.getBlockPosition() != null;
            if (variableSets.stream().map(OrderParameterisation::getPositions).filter(Objects::nonNull).anyMatch(hasPositionSetting)) {
                Set<BlockPosition> availableBlockPositions = WorkflowsHelper.getWorkflowBlockPositions(workflow.getInstructions());
                Map<String, List<Object>> labelMap = WorkflowsHelper.getLabelToPositionsMap(workflow, true);

                variableSets.stream().map(OrderParameterisation::getPositions).filter(Objects::nonNull).filter(hasPositionSetting).forEach(
                        p -> Validator.checkAddOrderPositions(p, availableBlockPositions, workflowName, labelMap, position + ".positions."));
            }
            Validator.testJavaNameRulesAtTags(position, variableSets.stream().map(OrderParameterisation::getTags).filter(Objects::nonNull).flatMap(
                    Set::stream).collect(Collectors.toSet()));
        } else {
            try {
                OrdersHelper.checkArguments(new Variables(), orderPreparation, allowEmptyArguments);
            } catch (Exception e1) {
                throw new JocConfigurationException(position + ": " + e1.getMessage());
            }
        }
    }

    public static boolean orderPreparationHasRequiredParameters(Requirements orderPrep) {
        if (orderPrep != null && orderPrep.getParameters() != null && orderPrep.getParameters().getAdditionalProperties() != null) {
            // find required param in orderPreparation
            return orderPrep.getParameters().getAdditionalProperties().values().stream().filter(i -> i.getFinal() == null).anyMatch(i -> i
                    .getDefault() == null);
        }
        return false;
    }

}
