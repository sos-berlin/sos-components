package com.sos.webservices.order.classes;

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

    private List<String> listOfWorkflowPaths;
    private List<String> listOfSchedulePaths;
    private List<Folder> listOfWorkflowFolders;
    private List<Folder> listOfScheduleFolders;
    private List<String> listOfPermittedWorkflowNames;
    private List<String> listOfPermittedScheduleNames;
    private boolean hasPermission;

    public void getPermittedNames(SOSShiroFolderPermissions folderPermissions, String controllerId, FilterDailyPlannedOrders filter)
            throws SOSHibernateException {

        folderPermissions.setSchedulerId(controllerId);
        if (filter == null) {
            filter = new FilterDailyPlannedOrders();
        }

        boolean withSchedulePathFilter = listOfSchedulePaths != null && !listOfSchedulePaths.isEmpty();
        boolean withWorkflowPathFilter = listOfWorkflowPaths != null && !listOfWorkflowPaths.isEmpty();

        listOfPermittedWorkflowNames = new ArrayList<String>();
        listOfPermittedScheduleNames = new ArrayList<String>();

        hasPermission = true;

        if (listOfSchedulePaths != null && !listOfSchedulePaths.isEmpty()) {
            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("FolderPermissionEvaluator");
                DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);
                dbLayerSchedules.getSchedulePathNameMap(listOfSchedulePaths).forEach((path, name) -> {
                    if (folderPermissions.isPermittedForFolder(path)) {
                        listOfPermittedScheduleNames.add(name);
                    }
                });
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
        if (listOfWorkflowPaths != null && !listOfWorkflowPaths.isEmpty()) {
            for (String path : listOfWorkflowPaths) {
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
                    listOfPermittedWorkflowNames.add(p.getFileName().toString());
                }
            }
        }

        // TODO hasPermission
        // currently e.g. see listOfPermittedWorkflowNames
        // 2 workflows selected : 1 is valid, 2 is not valid - this is allowed
        // 1 workflow selected (and listOfPermittedScheduleNames is not empty): 1 is not valid - this is not allowed
        if (listOfScheduleFolders != null && !listOfScheduleFolders.isEmpty()) {
            Set<Folder> permittedSchedulefolders = addPermittedFolder(listOfScheduleFolders, folderPermissions);
            if (permittedSchedulefolders.isEmpty()) {
                hasPermission = false;
            } else {
                filter.addScheduleFolders(permittedSchedulefolders);
            }
        }
        if (listOfWorkflowFolders != null && !listOfWorkflowFolders.isEmpty()) {
            Set<Folder> permittedWorkflowfolders = addPermittedFolder(listOfWorkflowFolders, folderPermissions);
            if (permittedWorkflowfolders.isEmpty()) {
                hasPermission = false;
            } else {
                filter.addWorkflowFolders(permittedWorkflowfolders);
            }
        }

        if (withSchedulePathFilter && listOfPermittedScheduleNames.isEmpty()) {
            hasPermission = false;
        } else {
            filter.setListOfScheduleNames(listOfPermittedScheduleNames);
        }

        if (withWorkflowPathFilter && listOfPermittedWorkflowNames.isEmpty()) {
            hasPermission = false;
        } else {
            filter.setListOfWorkflowNames(listOfPermittedWorkflowNames);
        }

        if (folderPermissions.getListOfFolders(controllerId).size() > 0) {
            if (listOfWorkflowFolders == null) {
                filter.addWorkflowFolders(folderPermissions.getListOfFolders(controllerId));
            }
            if (listOfScheduleFolders == null) {
                filter.addScheduleFolders(folderPermissions.getListOfFolders(controllerId));
            }
        }

    }

    private Set<Folder> addPermittedFolder(Collection<Folder> folders, SOSShiroFolderPermissions folderPermissions) {
        return folderPermissions.getPermittedFolders(folders);
    }

    public List<String> getListOfPermittedWorkflowNames() {
        return listOfPermittedWorkflowNames;
    }

    public List<String> getListOfPermittedScheduleNames() {
        return listOfPermittedScheduleNames;
    }

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setListOfWorkflowPaths(List<String> listOfWorkflowPaths) {
        this.listOfWorkflowPaths = listOfWorkflowPaths;
    }

    public void setListOfSchedulePaths(List<String> listOfSchedulePaths) {
        this.listOfSchedulePaths = listOfSchedulePaths;
    }

    public void setListOfWorkflowFolders(List<Folder> listOfWorkflowFolders) {
        this.listOfWorkflowFolders = listOfWorkflowFolders;
    }

    public void setListOfScheduleFolders(List<Folder> listOfScheduleFolders) {
        this.listOfScheduleFolders = listOfScheduleFolders;
    }
}
