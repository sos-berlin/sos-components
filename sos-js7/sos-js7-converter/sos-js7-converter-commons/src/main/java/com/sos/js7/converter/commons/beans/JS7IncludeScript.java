package com.sos.js7.converter.commons.beans;

public class JS7IncludeScript extends AJS7Object {

    private final String name;
    private final String script;

    public JS7IncludeScript(String name, String script) {
        this.name = name;
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public String getScript() {
        return script;
    }
}
