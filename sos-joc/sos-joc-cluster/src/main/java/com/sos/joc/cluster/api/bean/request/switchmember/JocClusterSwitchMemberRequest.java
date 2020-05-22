package com.sos.joc.cluster.api.bean.request.switchmember;

// JocClusterMeta.API_PATH/RequestPath.switchMember
public class JocClusterSwitchMemberRequest {

    private String memberId;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String val) {
        memberId = val;
    }

}
