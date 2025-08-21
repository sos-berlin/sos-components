package com.sos.joc.dailyplan.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.projections.ProjectionsDayResponse;
import com.sos.joc.model.dailyplan.projections.items.meta.ControllerInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.MetaItem;
import com.sos.joc.model.dailyplan.projections.items.meta.ScheduleInfoItem;
import com.sos.joc.model.dailyplan.projections.items.meta.WorkflowsItem;
import com.sos.joc.model.dailyplan.projections.items.year.DateItem;
import com.sos.joc.model.dailyplan.projections.items.year.DatePeriodItem;

public class ProjectionsImpl extends JOCOrderResourceImpl {

    public static Optional<Set<String>> getNamesOptional(List<String> paths) {
        Optional<Set<String>> names = Optional.empty();
        if (paths != null && !paths.isEmpty()) {
            names = Optional.of(paths.stream().map(JocInventory::pathToName).collect(Collectors.toSet()));
        }
        return names;
    }

    public static Map<String, ScheduleOrderCounter> mapScheduleOrderCounter(Optional<MetaItem> metaContentOpt, Set<String> allowedControllers) {
        if (metaContentOpt.isEmpty()) {
            return Map.of();
        }

        final Map<String, ScheduleOrderCounter> map = new HashMap<>();
        MetaItem meta = metaContentOpt.get();
        if (meta.getAdditionalProperties() != null) {
            m: for (Map.Entry<String, ControllerInfoItem> entry : meta.getAdditionalProperties().entrySet()) {
                if (allowedControllers.contains(entry.getKey())) {
                    entry.getValue().getAdditionalProperties().forEach((k, v) -> {
                        map.put(k, new ScheduleOrderCounter(v.getOrderNames(), v.getTotalOrders()));
                    });
                    break m;
                }
            }
        }
        return map;
    }

    public static void setDateItemNumOfOrders(DateItem dateItem, Map<String, ScheduleOrderCounter> scheduleOrderCounter) {
        // Planned
        if (dateItem.getPlanned() != null && dateItem.getPlanned()) {
            dateItem.setNumOfOrders(dateItem.getPeriods().size());
            return;
        }

        // Projection
        int total = 0;
        for (DatePeriodItem p : dateItem.getPeriods()) {
            if (p.getSchedule() == null) {
                total += 1;
            } else {
                ScheduleOrderCounter oc = scheduleOrderCounter.get(p.getSchedule());
                total += (oc == null ? 1 : oc.getTotalAsLong().intValue());
            }
        }
        dateItem.setNumOfOrders(total);
    }

    public boolean setPermittedSchedules(Optional<MetaItem> metaContentOpt, Set<String> allowedControllers, Optional<Set<String>> scheduleNames,
            List<Folder> scheduleFolders, Optional<Set<String>> nonPeriodScheduleNames, Optional<Set<String>> workflowNames,
            List<Folder> workflowFolders, Set<String> permittedSchedules, SOSAuthFolderPermissions folderPermissions) throws DBMissingDataException {

        boolean schedulesRemoved = false;
        MetaItem metaItem = metaContentOpt.orElse(null);
        if (metaItem != null && metaItem.getAdditionalProperties() != null) {
            if (filterControllerIds(metaItem, allowedControllers)) {
                schedulesRemoved = true;
            }
            for (String controllerId : allowedControllers) {
                Set<Folder> permittedFolders = folderPermissions == null ? Collections.emptySet() : folderPermissions.getListOfFolders(controllerId);
                if (filterPermittedSchedules(metaItem.getAdditionalProperties().get(controllerId), permittedFolders, scheduleNames, scheduleFolders,
                        nonPeriodScheduleNames, workflowNames, workflowFolders, permittedSchedules)) {
                    schedulesRemoved = true;
                }
            }
            metaContentOpt = Optional.of(metaItem);
        }
        return schedulesRemoved;
    }

    public Long getMonth(String month) {
        return Long.valueOf(month.replaceFirst("^(\\d{4})-(\\d{2}).*", "$1$2"));
    }

    public boolean isPlanned(DateItem d) {
        return d != null && d.getPlanned() != null && d.getPlanned();
    }

    public boolean isPlanned(ProjectionsDayResponse dr) {
        return dr != null && dr.getPlanned() != null && dr.getPlanned();
    }

    private boolean isExcludedFromProjection(ScheduleInfoItem sii) {
        return sii != null && sii.getExcludedFromProjection() != null && sii.getExcludedFromProjection();
    }

    /** "excludedFromProjection" is set if a schedule has been removed (or "Plan Order automatically" is deactivated) */
    public Set<String> getSchedulesExcludedFromProjection(Optional<MetaItem> metaContentOpt, boolean removeIfExcluded) {
        Set<String> r = new HashSet<>();
        if (!metaContentOpt.isPresent()) {
            return r;
        }

        MetaItem mi = metaContentOpt.get();
        Iterator<Map.Entry<String, ControllerInfoItem>> iterCII = mi.getAdditionalProperties().entrySet().iterator();
        while (iterCII.hasNext()) {
            Map.Entry<String, ControllerInfoItem> cii = iterCII.next();
            Iterator<Map.Entry<String, ScheduleInfoItem>> iterSII = cii.getValue().getAdditionalProperties().entrySet().iterator();
            while (iterSII.hasNext()) {
                Map.Entry<String, ScheduleInfoItem> sii = iterSII.next();
                if (isExcludedFromProjection(sii.getValue())) {
                    r.add(sii.getKey());
                    if (removeIfExcluded) {
                        iterSII.remove();
                    }
                }
            }
            if (removeIfExcluded) {
                if (cii.getValue().getAdditionalProperties().entrySet().size() == 0) {
                    iterCII.remove();
                }
            }
        }
        return r;
    }

    private boolean filterControllerIds(MetaItem metaItem, Set<String> allowedControllers) {
        boolean controllerRemoved = false;
        if (metaItem != null && metaItem.getAdditionalProperties() != null) {
            // metaItem.getAdditionalProperties().keySet().removeIf(controllerId -> !allowedControllers.contains(controllerId));
            controllerRemoved = metaItem.getAdditionalProperties().keySet().retainAll(allowedControllers);
        }
        return controllerRemoved;
    }

    private void filterPermittedWorkflows(WorkflowsItem workflowsItem, Set<Folder> permittedFolders, Optional<Set<String>> workflowNames) {
        // boolean workflowsRemoved = false;

        if (workflowsItem != null && workflowsItem.getAdditionalProperties() != null) {
            // int numOfWorkflow = workflowsItem.getAdditionalProperties().keySet().size();

            workflowNames.ifPresent(wn -> workflowsItem.getAdditionalProperties().keySet().removeIf(workflow -> !wn.contains(JocInventory.pathToName(
                    workflow))));

            if (!permittedFolders.isEmpty()) {
                workflowsItem.getAdditionalProperties().keySet().removeIf(wPath -> !canAdd(wPath, permittedFolders));
            }

            // int newNumOfWorkflow = workflowsItem.getAdditionalProperties().keySet().size();
            // if (numOfWorkflow > newNumOfWorkflow) {
            // workflowsRemoved = true;
            // }
        }

        // return workflowsRemoved;
    }

    private boolean filterPermittedSchedules(ControllerInfoItem cii, Set<Folder> permittedFolders, Optional<Set<String>> scheduleNames,
            List<Folder> scheduleFolders, Optional<Set<String>> nonPeriodScheduleNames, Optional<Set<String>> workflowNames,
            List<Folder> workflowFolders, Set<String> permittedSchedules) {
        boolean schedulesRemoved = false;

        if (cii != null && cii.getAdditionalProperties() != null) {
            int numOfSchedules = cii.getAdditionalProperties().keySet().size();

            scheduleNames.ifPresent(sn -> cii.getAdditionalProperties().keySet().removeIf(schedule -> !sn.contains(JocInventory.pathToName(
                    schedule))));

            nonPeriodScheduleNames.ifPresent(sn -> cii.getAdditionalProperties().keySet().removeIf(schedule -> sn.contains(JocInventory.pathToName(
                    schedule))));

            Set<Folder> permittedFolders1 = new HashSet<>();
            if (scheduleFolders != null && !scheduleFolders.isEmpty()) {
                permittedFolders1.addAll(SOSAuthFolderPermissions.getPermittedFolders(scheduleFolders, permittedFolders));
                if (permittedFolders1.isEmpty()) { // no folder permissions
                    cii.getAdditionalProperties().clear();
                }
            } else {
                permittedFolders1.addAll(permittedFolders);
            }
            if (!permittedFolders1.isEmpty()) {
                cii.getAdditionalProperties().keySet().removeIf(schedule -> !canAdd(schedule, permittedFolders1));
            }

            Set<Folder> permittedFolders2 = new HashSet<>();
            if (workflowFolders != null && !workflowFolders.isEmpty()) {
                permittedFolders2.addAll(SOSAuthFolderPermissions.getPermittedFolders(workflowFolders, permittedFolders));
                if (permittedFolders2.isEmpty()) { // no folder permissions
                    cii.getAdditionalProperties().clear();
                }
            } else {
                permittedFolders2.addAll(permittedFolders);
            }

            cii.getAdditionalProperties().values().forEach(sii -> {
                if (sii != null) {
                    if (!permittedFolders2.isEmpty() || workflowNames.isPresent()) {
                        filterPermittedWorkflows(sii.getWorkflows(), permittedFolders2, workflowNames);
                    }
                    if (sii.getWorkflows() != null) {
                        sii.setWorkflowPaths(sii.getWorkflows().getAdditionalProperties().keySet());
                        sii.setWorkflows(null);
                        sii.setTotalOrders(null);
                    }
                    if (sii.getOrderNames() != null && sii.getOrderNames().size() == 0) {
                        sii.setOrderNames(null);
                    }
                }
            });
            cii.getAdditionalProperties().values().removeIf(sii -> sii == null || sii.getWorkflowPaths() == null || sii.getWorkflowPaths().isEmpty());

            int newNumOfSchedules = cii.getAdditionalProperties().keySet().size();
            if (numOfSchedules > newNumOfSchedules) {
                schedulesRemoved = true;
            }
            permittedSchedules.addAll(cii.getAdditionalProperties().keySet());
        }
        return schedulesRemoved;
    }
}
