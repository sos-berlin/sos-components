package com.sos.joc.classes.logs;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sos.auth.classes.SOSAuthCurrentAccountsList;
import com.sos.joc.Globals;

public class LogSessions {
    
    private static LogSessions instance;
    // accesstoken, logSessionToken, logSessionData
    private volatile ConcurrentMap<String, Map<String, LogSession>> sessions = new ConcurrentHashMap<>();
    
    private LogSessions() {
        //EventBus.getInstance().register(this);
    }

    public static LogSessions getInstance() {
        if (instance == null) {
            instance = new LogSessions();
        }
        return instance;
    }
    
    public LogSession getSession(String accessToken, String logToken) {
        return sessions.getOrDefault(accessToken, Collections.emptyMap()).get(logToken);
    }
    
    public void clean() {
        Optional.ofNullable(Globals.jocWebserviceDataContainer.getCurrentAccountsList()).map(SOSAuthCurrentAccountsList::getAccessTokens).ifPresent(
                ats -> sessions.keySet().removeIf(key -> !ats.contains(key)));
    }

}
