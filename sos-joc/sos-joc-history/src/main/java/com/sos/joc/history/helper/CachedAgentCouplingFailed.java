package com.sos.joc.history.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedAgentCouplingFailed {

    private final Map<String, List<AgentCouplingFailed>> agents;

    public CachedAgentCouplingFailed() {
        agents = new HashMap<>();
    }

    public void add(String agentId, Long eventId, String message) {
        List<AgentCouplingFailed> l;
        if (agents.containsKey(agentId)) {
            l = agents.get(agentId);
        } else {
            l = new ArrayList<>();
        }
        l.add(new AgentCouplingFailed(eventId, message));
        agents.put(agentId, l);
    }

    public AgentCouplingFailed getLast(String agentId, Long nextReadyEventId) {
        List<AgentCouplingFailed> l = agents.get(agentId);
        if (l == null || l.size() == 0) {
            return null;
        }
        return l.stream().filter(f -> f.getEventId() < nextReadyEventId).max(Comparator.comparing(AgentCouplingFailed::getEventId)).orElse(null);
    }

    public void remove(String agentId) {
        agents.remove(agentId);
    }

    public void clear() {
        agents.clear();
    }

    public class AgentCouplingFailed {

        private final Long eventId;
        private final String message;

        public AgentCouplingFailed(Long eventId, String message) {
            this.eventId = eventId;
            this.message = message;
        }

        public Long getEventId() {
            return eventId;
        }

        public String getMessage() {
            return message;
        }
    }

}
