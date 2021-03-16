package com.sos.joc.classes.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;

public class SyncStateHelper {
    
    public static final Map<SyncStateText, Integer> severityByStates = Collections.unmodifiableMap(new HashMap<SyncStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(SyncStateText.IN_SYNC, 6);
            put(SyncStateText.NOT_IN_SYNC, 5);
            put(SyncStateText.UNKNOWN, 2);
        }
    });

    public static SyncState getState(SyncStateText stateText) {
        SyncState state = new SyncState();
        state.set_text(stateText);
        state.setSeverity(severityByStates.get(stateText));
        return state;
    }
}
