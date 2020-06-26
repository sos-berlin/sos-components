package com.sos.joc.cluster.api.bean.request.switchmember;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// JocClusterMeta.API_PATH/RequestPath.switchMember
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JocClusterSwitchMemberRequest {

    @JsonProperty("memberId")
    private String memberId;

    @JsonProperty("memberId")
    public String getMemberId() {
        return memberId;
    }

    @JsonProperty("memberId")
    public void setMemberId(String val) {
        memberId = val;
    }

}
