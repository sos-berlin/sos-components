package com.sos.js7.converter.commons.config.items;

public class MockConfig extends AConfigItem {

    private static final String CONFIG_KEY = "mockConfig";

    private String forcedWindowsScript;
    private String forcedUnixScript;
    private String forcedJitlJobsMockLevel; // see com.sos.jitl.jobs.common.JobArguments

    public MockConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        // SHELL
        case "forced.shell.windowsscript":
            withForcedWindowsScript(val);
            break;
        case "forced.shell.unixscript":
            withForcedUnixScript(val);
            break;
        // JITL
        case "forced.jitl.mocklevel":
            withForcedJitlJobsMockLevel(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return !hasForcedScript() && forcedJitlJobsMockLevel == null;
    }

    public MockConfig withForcedWindowsScript(String val) {
        this.forcedWindowsScript = val;
        return this;
    }

    public MockConfig withForcedUnixScript(String val) {
        this.forcedUnixScript = val;
        return this;
    }

    public MockConfig withForcedJitlJobsMockLevel(String val) {
        this.forcedJitlJobsMockLevel = val;
        return this;
    }

    public String getForcedWindowsScript() {
        return forcedWindowsScript;
    }

    public String getForcedUnixScript() {
        return forcedUnixScript;
    }

    public String getForcedJitlJobsMockLevel() {
        return forcedJitlJobsMockLevel;
    }

    public boolean hasForcedScript() {
        return forcedWindowsScript != null || forcedUnixScript != null;
    }

}
