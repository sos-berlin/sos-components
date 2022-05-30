package com.sos.js7.converter.js1.common.processclass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class RemoteSchedulers {

    private static final String ATTR_IGNORE = "ignore";
    private static final String ELEMENT_REMOTE_SCHEDULER = "remote_scheduler";

    private List<RemoteScheduler> remoteScheduler;

    // yes|no
    private boolean ignore;

    public RemoteSchedulers(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Map<String, String> map = JS7ConverterHelper.attribute2map(node);
        this.ignore = JS7ConverterHelper.booleanValue(map.get(ATTR_IGNORE), false);

        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_REMOTE_SCHEDULER);
        if (l != null && l.getLength() > 0) {
            this.remoteScheduler = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                this.remoteScheduler.add(new RemoteScheduler(l.item(i)));
            }
        }
    }

    public List<RemoteScheduler> getRemoteScheduler() {
        return remoteScheduler;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public class RemoteScheduler {

        private static final String ATTR_HTTP_HEARTBEAT_PERIOD = "http_heartbeat_period";
        private static final String ATTR_HTTP_HEARTBEAT_TIMEOUT = "http_heartbeat_timeout";
        private static final String ATTR_REMOTE_SCHEDULER = "remote_scheduler";

        private Integer httpHeartbeatPeriod; // number (Initial value: 10)
        private Integer httpHeartbeatTimeout; // number (Initial value: 15)
        private String remoteScheduler; // The URL of the remote scheduler - e.g. http://127.0.0.2:5000

        private RemoteScheduler(Node node) {
            Map<String, String> map = JS7ConverterHelper.attribute2map(node);
            this.httpHeartbeatPeriod = JS7ConverterHelper.integerValue(map.get(ATTR_HTTP_HEARTBEAT_PERIOD));
            this.httpHeartbeatTimeout = JS7ConverterHelper.integerValue(map.get(ATTR_HTTP_HEARTBEAT_TIMEOUT));
            this.remoteScheduler = JS7ConverterHelper.stringValue(map.get(ATTR_REMOTE_SCHEDULER));
        }

        public Integer getHttpHeartbeatPeriod() {
            return httpHeartbeatPeriod;
        }

        public Integer getHttpHeartbeatTimeout() {
            return httpHeartbeatTimeout;
        }

        public String getRemoteScheduler() {
            return remoteScheduler;
        }

    }
}
