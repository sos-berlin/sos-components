package com.sos.joc.cluster.api.bean.request.restart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sos.joc.cluster.api.JocClusterMeta.HandlerIdentifier;

// JocClusterMeta.API_PATH/RequestPath.restart
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JocClusterRestartRequest {

    @JsonProperty("type")
    private HandlerIdentifier type;

    @JsonProperty("type")
    public HandlerIdentifier getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(HandlerIdentifier val) {
        type = val;
    }

}
