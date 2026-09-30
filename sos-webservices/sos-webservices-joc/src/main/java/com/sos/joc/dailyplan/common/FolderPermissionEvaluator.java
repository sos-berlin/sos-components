package com.sos.joc.dailyplan.common;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.auth.classes.SOSAuthDetailedFolderPermissions;
import com.sos.auth.records.AuthFolders;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.model.common.Folder;

public class FolderPermissionEvaluator {

    private List<Folder> workflowFolders;
    private List<Folder> scheduleFolders;

    private List<String> workflowPaths;
    private List<String> schedulePaths;
    private List<String> permittedWorkflowNames;
    private Set<String> scheduleNames;
    private boolean hasPermission;

    public FilterDailyPlannedOrders getPermittedNames(AuthFolders permittedFolders) throws SOSHibernateException {

        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

        permittedWorkflowNames = Collections.emptyList();
        scheduleNames = Collections.emptySet();
        hasPermission = true;

        if (schedulePaths != null && !schedulePaths.isEmpty()) {
                scheduleNames = schedulePaths.stream().filter(p -> p != null && !p.isEmpty()).map(
                        JocInventory::pathToName).collect(Collectors.toSet());
                
                filter.setScheduleNames(scheduleNames);

        }
        if (workflowPaths != null && !workflowPaths.isEmpty()) {
            permittedWorkflowNames = workflowPaths.stream().filter(p -> p != null && !p.isEmpty()).map(JocInventory::pathToName).filter(
                    p -> JOCResourceImpl.canAdd(WorkflowPaths.getPath(p), permittedFolders)).distinct().collect(Collectors.toList());

            filter.setWorkflowNames(permittedWorkflowNames);
            hasPermission = permittedWorkflowNames.isEmpty();
        }

        if (scheduleFolders != null && !scheduleFolders.isEmpty()) {
            
            filter.addScheduleFolders(scheduleFolders);
            
        }
        if (workflowFolders != null && !workflowFolders.isEmpty()) {
            
            AuthFolders permittedWorkflowFolders = SOSAuthDetailedFolderPermissions.getPermittedFolders(workflowFolders, permittedFolders);
            if (permittedWorkflowFolders.allow().isPresent()) {
                filter.addWorkflowFolders(permittedWorkflowFolders.allow().get());
            }
        }
        
        return filter;

    }

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setWorkflowFolders(List<Folder> val) {
        workflowFolders = val;
    }

    public void setScheduleFolders(List<Folder> val) {
        scheduleFolders = val;
    }

    public void setWorkflowPaths(List<String> val) {
        workflowPaths = val;
    }

    public void setSchedulePaths(List<String> val) {
        schedulePaths = val;
    }

}
