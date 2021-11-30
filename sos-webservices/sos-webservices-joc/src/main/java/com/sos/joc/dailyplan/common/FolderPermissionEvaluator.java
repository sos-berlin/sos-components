package com.sos.joc.dailyplan.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.model.common.Folder;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

public class FolderPermissionEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderPermissionEvaluator.class);

    private List<Folder> workflowFolders;
    private List<Folder> scheduleFolders;

    private List<String> workflowPaths;
    private List<String> schedulePaths;
    private List<String> permittedWorkflowNames;
    private List<String> permittedScheduleNames;
    private boolean hasPermission;

    public void getPermittedNames(SOSShiroFolderPermissions folderPermissions, String controllerId, FilterDailyPlannedOrders filter)
            throws SOSHibernateException {

        folderPermissions.setSchedulerId(controllerId);
        if (filter == null) {
            filter = new FilterDailyPlannedOrders();
        }

        boolean withSchedulePathFilter = schedulePaths != null && !schedulePaths.isEmpty();
        boolean withWorkflowPathFilter = workflowPaths != null && !workflowPaths.isEmpty();

        permittedWorkflowNames = new ArrayList<String>();
        permittedScheduleNames = new ArrayList<String>();

        hasPermission = true;

        if (schedulePaths != null && !schedulePaths.isEmpty()) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection("FolderPermissionEvaluator");
                DBLayerSchedules dbLayer = new DBLayerSchedules(session);
                dbLayer.getSchedulePathNameMap(schedulePaths).forEach((path, name) -> {
                    if (folderPermissions.isPermittedForFolder(path)) {
                        permittedScheduleNames.add(name);
                    }
                });
            } finally {
                Globals.disconnect(session);
            }
        }
        if (workflowPaths != null && !workflowPaths.isEmpty()) {
            for (String path : workflowPaths) {
                if (path == null || path.isEmpty()) {
                    LOGGER.debug("path is empty");
                    continue;
                }
                String wpath = WorkflowPaths.getPathOrNull(path);
                if (wpath == null) {// to avoid a NPE exception by p.getParent() for not deployed workflows
                    LOGGER.debug(String.format("[%s][skip]deployment path not found", path));
                    continue;
                }
                LOGGER.debug(String.format("[path=%s]wpath=%s", path, wpath));
                Path p = Paths.get(wpath);
                if (folderPermissions.isPermittedForFolder(p.getParent().toString().replace('\\', '/'))) {
                    permittedWorkflowNames.add(p.getFileName().toString());
                }
            }
        }

        // TODO hasPermission
        // currently e.g. see listOfPermittedWorkflowNames
        // 2 workflows selected : 1 is valid, 2 is not valid - this is allowed
        // 1 workflow selected (and listOfPermittedScheduleNames is not empty): 1 is not valid - this is not allowed
        if (scheduleFolders != null && !scheduleFolders.isEmpty()) {
            Set<Folder> permitted = addPermittedFolder(scheduleFolders, folderPermissions);
            if (permitted.isEmpty()) {
                // hasPermission = false; //maybe the schedules were deleted
            } else {
                filter.addScheduleFolders(permitted);
            }
        }
        if (workflowFolders != null && !workflowFolders.isEmpty()) {
            Set<Folder> permitted = addPermittedFolder(workflowFolders, folderPermissions);
            if (permitted.isEmpty()) {
                // hasPermission = false;
            } else {
                filter.addWorkflowFolders(permitted);
            }
        }

        if (withSchedulePathFilter && permittedScheduleNames.isEmpty()) {
            hasPermission = false;
        } else {
            filter.setScheduleNames(permittedScheduleNames);
        }

        if (withWorkflowPathFilter && permittedWorkflowNames.isEmpty()) {
            hasPermission = false;
        } else {
            filter.setWorkflowNames(permittedWorkflowNames);
        }

        if (folderPermissions.getListOfFolders(controllerId).size() > 0) {
            if (workflowFolders == null) {
                filter.addWorkflowFolders(folderPermissions.getListOfFolders(controllerId));
            }
            if (scheduleFolders == null) {
                filter.addScheduleFolders(folderPermissions.getListOfFolders(controllerId));
            }
        }

    }

    private Set<Folder> addPermittedFolder(Collection<Folder> folders, SOSShiroFolderPermissions folderPermissions) {
        return folderPermissions.getPermittedFolders(folders);
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

    public List<String> getPermittedWorkflowNames() {
        return permittedWorkflowNames;
    }

    public List<String> getPermittedScheduleNames() {
        return permittedScheduleNames;
    }

}
