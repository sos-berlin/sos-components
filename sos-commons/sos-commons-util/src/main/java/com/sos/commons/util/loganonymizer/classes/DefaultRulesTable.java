package com.sos.commons.util.loganonymizer.classes;

public abstract class DefaultRulesTable {

    protected abstract void a(String item, String search, String replace);

    public DefaultRulesTable() {
        super();
        init();
    }

    private void init() {
        a("ip-address",
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])",
                "<ip-address>");
        a("User", "User:.(.*)", "<user-id>");
        a("agent_id", "agent=(.*)", "agent=<agent_id>");
        a("host1", "Connect(.*.:.......)", "Connect(<host:port>");
        a("user_name", "user_name.*=.*$", "user_name            = <user>");
        a("user_home", "user_home.*=.*$", "user_home            = <user_home>");
    }

}
