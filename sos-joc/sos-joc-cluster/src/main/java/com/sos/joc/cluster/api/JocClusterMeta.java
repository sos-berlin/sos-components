package com.sos.joc.cluster.api;

public class JocClusterMeta {

    public static final String API_PATH = "/cluster/api/";
    public static final String HEADER_NAME_ACCESS_TOKEN = "X-Access-Token";

    public static enum RequestPath {
        switchMember, start, stop, restart, status
    }
}
