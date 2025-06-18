package com.sos.js7.converter.autosys.config.items;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.config.items.AConfigItem;

public class AutosysOutputConfig extends AConfigItem {

    private static final String CONFIG_KEY = "autosys.output";

    FolderMapping folderMapping = new FolderMapping("application;group");
    CrossInstanceCondition crossInstanceCondition = new CrossInstanceCondition("ignore");

    public AutosysOutputConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "folder.mapping":
            withFolderMapping(val);
            break;
        case "crossinstance.condition":
            withCrossInstanceCondition(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public AutosysOutputConfig withFolderMapping(String val) {
        folderMapping = new FolderMapping(val);
        return this;
    }

    public AutosysOutputConfig withCrossInstanceCondition(String val) {
        crossInstanceCondition = new CrossInstanceCondition(val);
        return this;
    }

    public FolderMapping getFolderMapping() {
        return folderMapping;
    }

    public CrossInstanceCondition getCrossInstanceCondition() {
        return crossInstanceCondition;
    }

    public class FolderMapping {

        public enum FolderMappingType {
            APPLICATION, GROUP
        }

        private List<FolderMappingType> mapping = List.of(FolderMappingType.APPLICATION, FolderMappingType.GROUP);

        private boolean firstIsApplication = false;
        private boolean secondIsApplication = false;

        private boolean firstIsGroup = false;
        private boolean secondIsGroup = false;

        private FolderMapping(String val) {
            if (SOSString.isEmpty(val)) {
                mapping = List.of();
            } else {
                mapping = Arrays.asList(val.split(";")).stream().map(e -> {
                    try {
                        return FolderMappingType.valueOf(e.trim().toUpperCase());
                    } catch (Throwable ex) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
            switch (mapping.size()) {
            case 1:
                firstIsApplication = isApplication(mapping.get(0));
                if (!firstIsApplication) {
                    firstIsGroup = true;
                }
                break;
            case 2:
                firstIsApplication = isApplication(mapping.get(0));
                if (!firstIsApplication) {
                    firstIsGroup = true;
                    secondIsApplication = true;
                } else {
                    secondIsGroup = true;
                }
                break;
            default:
                break;
            }
        }

        private boolean isApplication(FolderMappingType type) {
            return FolderMappingType.APPLICATION.equals(type);
        }

        @SuppressWarnings("unused")
        private boolean isGroup(FolderMappingType type) {
            return FolderMappingType.GROUP.equals(type);
        }

        public boolean isEmpty() {
            return mapping.isEmpty();
        }

        public boolean firstIsApplication() {
            return firstIsApplication;
        }

        public boolean secondIsApplication() {
            return secondIsApplication;
        }

        public boolean firstIsGroup() {
            return firstIsGroup;
        }

        public boolean secondIsGroup() {
            return secondIsGroup;
        }
    }

    public class CrossInstanceCondition {

        public enum CrossInstanceConditionMode {
            IGNORE, MAPTOLOCAL, MAPTOLOCALKEEPINSTANCE
        }

        private boolean ignore;
        private boolean mapToLocal;
        private boolean mapToLocalKeepInstance;

        private CrossInstanceConditionMode mode = CrossInstanceConditionMode.IGNORE;

        private CrossInstanceCondition(String val) {
            if (!SOSString.isEmpty(val)) {
                mode = CrossInstanceConditionMode.valueOf(val.trim().toUpperCase());
            }
            switch (mode) {
            case MAPTOLOCALKEEPINSTANCE:
                mapToLocalKeepInstance = true;
                break;
            case MAPTOLOCAL:
                mapToLocal = true;
                break;
            case IGNORE:
            default:
                ignore = true;
                break;
            }
        }

        public boolean isIgnore() {
            return ignore;
        }

        public boolean isMapToLocal() {
            return mapToLocal;
        }

        public boolean isMapToLocalKeepInstance() {
            return mapToLocalKeepInstance;
        }
    }
}
