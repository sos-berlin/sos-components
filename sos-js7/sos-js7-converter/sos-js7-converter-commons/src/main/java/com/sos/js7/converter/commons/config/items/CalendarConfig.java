package com.sos.js7.converter.commons.config.items;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import com.sos.commons.util.SOSString;

public class CalendarConfig extends AConfigItem {

    private static final String CONFIG_KEY = "calendarConfig";

    private Path forcedFolder;

    public CalendarConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "forced.folder":
            withForcedFolder(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return forcedFolder == null;
    }

    public CalendarConfig withForcedFolder(String val) {
        if (!SOSString.isEmpty(val)) {
            String f = val.replaceAll("\\\\", "/");
            String[] arr = StringUtils.strip(f, "/").split("/");

            forcedFolder = Paths.get("");
            for (int i = 0; i < arr.length; i++) {
                forcedFolder = forcedFolder.resolve(arr[i]);
            }
        }
        return this;
    }

    public Path getForcedFolder() {
        return forcedFolder;
    }
}
