package com.sos.webservices.order.classes;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.model.common.Folder;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

public class FolderPermissionEvaluator {

    private List<String> listOfWorkflowPaths;
    private List<String> listOfSchedulePaths;
    private List<Folder> listOfWorkflowFolders;
    private List<Folder> listOfScheduleFolders;
    private List<String> listOfWorkflowNames;
    private List<String> listOfScheduleNames;
    private List<String> listOfPermittedWorkflowNames;
    private List<String> listOfPermittedScheduleNames;
    private boolean hasPermission;

    public void getPermittedNames(SOSShiroFolderPermissions folderPermissions, String controllerId, FilterDailyPlannedOrders filter)
            throws SOSHibernateException {

        folderPermissions.setSchedulerId(controllerId);
        if (filter == null) {
            filter = new FilterDailyPlannedOrders();
        }

        boolean withWorkflowFolderFilter = listOfWorkflowFolders != null && !listOfWorkflowFolders.isEmpty();
        boolean withScheduleFolderFilter = listOfScheduleFolders != null && !listOfScheduleFolders.isEmpty();
        boolean withWorkflowFolderFilterFromPath = listOfWorkflowPaths != null && !listOfWorkflowPaths.isEmpty();
        boolean withScheduleFolderFilterFromPath = listOfSchedulePaths != null && !listOfSchedulePaths.isEmpty();
        boolean withScheduleNameFilter = listOfScheduleNames != null && !listOfScheduleNames.isEmpty();
        boolean withWorkflowNameFilter = listOfWorkflowNames != null && !listOfWorkflowNames.isEmpty();

        List<Folder> schedulefoldersFromPath = new ArrayList<Folder>();
        List<Folder> workflowfoldersFromPath = new ArrayList<Folder>();
        listOfPermittedWorkflowNames = new ArrayList<String>();
        listOfPermittedScheduleNames = new ArrayList<String>();

        hasPermission = true;

        if (listOfSchedulePaths != null) {
            if (listOfScheduleNames == null) {
                listOfScheduleNames = new ArrayList<String>();
            }
            for (String path : listOfSchedulePaths) {
                String name = Paths.get(path).getFileName().toString();
                String folderName = Paths.get(path).getParent().toString().replace('\\', '/');
                if (folderPermissions.isPermittedForFolder(folderName)) {
                    listOfPermittedScheduleNames.add(name);
                }
                Folder f = new Folder();
                f.setFolder(folderName);
                f.setRecursive(false);
                schedulefoldersFromPath.add(f);
            }
        }
        if (listOfWorkflowPaths != null) {
            if (listOfScheduleNames == null) {
                listOfScheduleNames = new ArrayList<String>();
            }
            for (String path : listOfWorkflowPaths) {
                String name = Paths.get(path).getFileName().toString();
                String folderName = Paths.get(path).getParent().toString().replace('\\', '/');
                if (folderPermissions.isPermittedForFolder(folderName)) {
                    listOfPermittedWorkflowNames.add(name);
                }
                Folder f = new Folder();
                f.setFolder(folderName);
                f.setRecursive(false);
                workflowfoldersFromPath.add(f);
            }
        }

        if (listOfWorkflowNames != null) {

            for (String name : listOfWorkflowNames) {
                String folderName = WorkflowPaths.getPath(name);
                if (folderPermissions.isPermittedForFolder(folderName)) {
                    listOfPermittedWorkflowNames.add(name);
                }
            }
        }

        if (listOfScheduleNames != null) {
            for (String name : listOfScheduleNames) {
                String folderName = getSchedulePath(name);
                if (folderPermissions.isPermittedForFolder(folderName)) {
                    listOfPermittedScheduleNames.add(name);
                }
            }
        }

        Set<Folder> permittedWorkflowfoldersFromPath = null;
        Set<Folder> permittedSchedulefoldersFromPath = null;
        Set<Folder> permittedWorkflowfolders = null;
        Set<Folder> permittedSchedulefolders = null;

        if (withWorkflowFolderFilterFromPath) {
            permittedWorkflowfoldersFromPath = addPermittedFolder(workflowfoldersFromPath, folderPermissions);
        }
        if (withScheduleFolderFilterFromPath) {
            permittedSchedulefoldersFromPath = addPermittedFolder(schedulefoldersFromPath, folderPermissions);
        }
        if (withScheduleFolderFilter) {
            permittedSchedulefolders = addPermittedFolder(listOfScheduleFolders, folderPermissions);
        }
        if (withWorkflowFolderFilter) {
            permittedWorkflowfolders = addPermittedFolder(listOfWorkflowFolders, folderPermissions);
        }

        if (withWorkflowFolderFilterFromPath && (permittedWorkflowfoldersFromPath == null || permittedWorkflowfoldersFromPath.isEmpty())) {
            hasPermission = false;
        }
        if (withScheduleFolderFilterFromPath && (permittedSchedulefoldersFromPath == null || permittedSchedulefoldersFromPath.isEmpty())) {
            hasPermission = false;
        }

        if (withWorkflowFolderFilter && (permittedWorkflowfolders == null || permittedWorkflowfolders.isEmpty())) {
            hasPermission = false;
        } else if (permittedWorkflowfolders != null && !permittedWorkflowfolders.isEmpty()) {
            filter.addWorkflowFolders(new HashSet<Folder>(permittedWorkflowfolders));
        }

        if (withScheduleFolderFilter && (permittedSchedulefolders == null || permittedSchedulefolders.isEmpty())) {
            hasPermission = false;
        } else if (permittedSchedulefolders != null && !permittedSchedulefolders.isEmpty()) {
            filter.addScheduleFolders(new HashSet<Folder>(permittedSchedulefolders));
        }

        if (withScheduleNameFilter && (listOfPermittedScheduleNames == null || listOfPermittedScheduleNames.isEmpty())) {
            hasPermission = false;
        } else if (listOfPermittedScheduleNames != null && !listOfPermittedScheduleNames.isEmpty()) {
            filter.setListOfScheduleNames(listOfPermittedScheduleNames);
        }

        if (withWorkflowNameFilter && (listOfPermittedWorkflowNames == null || listOfPermittedWorkflowNames.isEmpty())) {
            hasPermission = false;
        } else if (listOfPermittedWorkflowNames != null && !listOfPermittedWorkflowNames.isEmpty()) {
            filter.setListOfWorkflowNames(listOfPermittedWorkflowNames);
        }

        if (folderPermissions.getListOfFolders(controllerId).size() > 0) {
            if (listOfWorkflowFolders == null) {
                listOfWorkflowFolders = new ArrayList<Folder>();
                filter.addWorkflowFolders(folderPermissions.getListOfFolders(controllerId));
            }
            if (listOfScheduleFolders == null) {
                listOfScheduleFolders = new ArrayList<Folder>();
                filter.addScheduleFolders(folderPermissions.getListOfFolders(controllerId));
            }
        }

    }

    private Set<Folder> addPermittedFolder(Collection<Folder> folders, SOSShiroFolderPermissions folderPermissions) {
        return folderPermissions.getPermittedFolders(folders);
    }

    private String getSchedulePath(String scheduleName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("FolderPermissionEvaluator");
            DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);
            return dbLayerSchedules.getSchedulePath(scheduleName);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
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

    public void setListOfWorkflowNames(List<String> listOfWorkflowNames) {
        this.listOfWorkflowNames = listOfWorkflowNames;
    }

    public void setListOfScheduleNames(List<String> listOfScheduleNames) {
        this.listOfScheduleNames = listOfScheduleNames;
    }
}
