package com.sos.joc.cluster.api;

public class JocClusterMeta {

    public static final String API_PATH = "/api/cluster";

    public static enum RequestPath {
        switchMember, restart, status
    }

    public static enum HandlerIdentifier {
        cluster, history, dailyplan
    }
}
