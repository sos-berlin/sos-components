package com.sos.joc.cluster.api.bean.request.restart;

import com.sos.joc.cluster.api.JocClusterMeta.HandlerIdentifier;

// JocClusterMeta.API_PATH/RequestPath.restart
public class JocClusterRestartRequest {

    private HandlerIdentifier type;

    public HandlerIdentifier getType() {
        return type;
    }

    public void setType(HandlerIdentifier val) {
        type = val;
    }

}
