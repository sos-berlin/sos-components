package com.sos.joc.event.bean.yade.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.yade.YadeEvent;

public class YadeTransferHistoryTerminated extends YadeEvent {

    public YadeTransferHistoryTerminated(String controllerId, Long transferId) {
        super(YadeTransferHistoryTerminated.class.getSimpleName(), controllerId, null);
        putVariable("transferId", String.valueOf(transferId));
    }

    @JsonIgnore
    public Long getTransferId() {
        try {
            return Long.parseLong(getVariables().get("transferId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
