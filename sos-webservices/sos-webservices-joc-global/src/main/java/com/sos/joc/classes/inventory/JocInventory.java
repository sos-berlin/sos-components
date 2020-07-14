package com.sos.joc.classes.inventory;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;
import com.sos.joc.model.common.JobSchedulerObjectType;

public class JocInventory {

    public static final String APPLICATION_PATH = "inventory";
    public static final String ROOT_FOLDER = "/";

    public static String getResourceImplPath(final String path) {
        return String.format("./%s/%s", APPLICATION_PATH, path);
    }

    public static Long getType(JobSchedulerObjectType type) {
        Long result = null;
        try {
            if (type.equals(JobSchedulerObjectType.WORKFLOWJOB)) {// TODO temp mapping
                result = InventoryMeta.ConfigurationType.JOB.value();
            } else {
                result = InventoryMeta.ConfigurationType.valueOf(type.name()).value();
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static ConfigurationType getType(Long type) {
        ConfigurationType result = null;
        try {
            result = InventoryMeta.ConfigurationType.fromValue(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static ConfigurationType getType(String type) {
        ConfigurationType result = null;
        try {
            if (type.equals(JobSchedulerObjectType.WORKFLOWJOB.name())) {// TODO temp mapping
                result = InventoryMeta.ConfigurationType.JOB;
            } else {
                result = InventoryMeta.ConfigurationType.valueOf(type);
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static class InventoryPath {

        private String path = "";
        private String name = "";
        private String folder = ROOT_FOLDER;
        private String parentFolder = ROOT_FOLDER;

        public InventoryPath(final String inventoryPath) {
            if (!SOSString.isEmpty(inventoryPath)) {
                path = Globals.normalizePath(inventoryPath);
                Path p = Paths.get(path);
                name = p.getFileName().toString();
                folder = normalizeFolder(p.getParent());
                if (folder.equals(ROOT_FOLDER)) {
                    parentFolder = ROOT_FOLDER;
                } else {
                    parentFolder = normalizeFolder(p.getParent().getParent());
                }
            }
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public String getFolder() {
            return folder;
        }

        public String getParentFolder() {
            return parentFolder;
        }

        private String normalizeFolder(Path folder) {
            String s = folder.toString().replace('\\', '/');
            return SOSString.isEmpty(s) ? ROOT_FOLDER : s;
        }
    }

}
