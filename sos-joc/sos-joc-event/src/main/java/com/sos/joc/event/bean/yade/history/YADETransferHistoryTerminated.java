package com.sos.joc.event.bean.yade.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.yade.YADEEvent;

public class YADETransferHistoryTerminated extends YADEEvent {

    public YADETransferHistoryTerminated(String controllerId, Long transferId) {
        super(YADETransferHistoryTerminated.class.getSimpleName(), controllerId, null);
        putVariable("transferId", transferId);
    }

    @JsonIgnore
    public Long getTransferId() {
        try {
            return (Long) getVariables().get("transferId");
        } catch (Throwable e) {
            return null;
        }
    }
}
