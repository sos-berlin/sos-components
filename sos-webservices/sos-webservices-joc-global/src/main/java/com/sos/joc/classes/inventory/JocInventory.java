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

    public static String getResourceImplPath(final String path) {
        return String.format("./%s/%s", APPLICATION_PATH, path);
    }

    public static Long getType(JobSchedulerObjectType type) {
        Long result = null;
        try {
            result = InventoryMeta.ConfigurationType.valueOf(type.name()).value();
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
            result = InventoryMeta.ConfigurationType.valueOf(type);
        } catch (Exception e) {
        }
        return result;
    }

    public static class InventoryPath {

        private static final String ROOT = "";

        private String path = "";
        private String name = "";
        private String folder = ROOT;
        private String parentFolder = ROOT;

        public InventoryPath(final String inventoryPath) {
            if (!SOSString.isEmpty(inventoryPath)) {
                path = Globals.normalizePath(inventoryPath);
                Path p = Paths.get(path);
                name = p.getFileName().toString();
                folder = normalizeFolder(p.getParent());
                if (folder.equals(ROOT)) {
                    parentFolder = ROOT;
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
            return SOSString.isEmpty(s) ? ROOT : s;
        }
    }

}
