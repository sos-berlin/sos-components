package com.sos.joc.event.bean.yade.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.yade.YadeEvent;

public class YadeTransferHistoryTerminated extends YadeEvent {

    public YadeTransferHistoryTerminated(String controllerId, Long transferId) {
        super(YadeTransferHistoryTerminated.class.getSimpleName(), controllerId, null);
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
