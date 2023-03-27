package com.sos.js7.converter.commons.config.items;

public class MockConfig extends AConfigItem {

    private static final String CONFIG_KEY = "mockConfig";

    private String windowsScript;
    private String unixScript;
    private String jitlJobsMockLevel; // see com.sos.jitl.jobs.common.JobArguments

    public MockConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        // SHELL
        case "shell.windowsScript":
            withWindowsScript(val);
            break;
        case "shell.unixScript":
            withUnixScript(val);
            break;
        // JITL
        case "jitl.mockLevel":
            withJitlJobsMockLevel(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return !hasScript() && jitlJobsMockLevel == null;
    }

    public MockConfig withWindowsScript(String val) {
        this.windowsScript = val;
        return this;
    }

    public MockConfig withUnixScript(String val) {
        this.unixScript = val;
        return this;
    }

    public MockConfig withJitlJobsMockLevel(String val) {
        this.jitlJobsMockLevel = val;
        return this;
    }

    public String getWindowsScript() {
        return windowsScript;
    }

    public String getUnixScript() {
        return unixScript;
    }

    public String getJitlJobsMockLevel() {
        return jitlJobsMockLevel;
    }

    public boolean hasScript() {
        return windowsScript != null || unixScript != null;
    }

}
