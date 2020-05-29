package com.sos.joc.cluster.api;

public class JocClusterMeta {

    public static final String API_PATH = "/cluster/api/";

    public static enum RequestPath {
        switchMember, restart, status
    }
}
