package com.sos.js7.converter.js1.common.job;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class OrderJob extends ACommonJob {

    private static final String ATTR_IDLE_TIMEOUT = "idle_timeout";
    private static final String ELEMENT_DELAY_ORDER_AFTER_SETBACK = "delay_order_after_setback";

    private List<DelayOrderAfterSetback> delayOrderAfterSetback;

    private String idleTimeout; // duration - Limit for the waiting_for_order state

    public OrderJob() {
        super(Type.ORDER);
    }

    public SOSXMLXPath parse(DirectoryParserResult pr, SOSXMLXPath xpath, Node node, Map<String, String> attributes, Path currentPath)
            throws Exception {
        idleTimeout = JS7ConverterHelper.stringValue(attributes.get(ATTR_IDLE_TIMEOUT));

        NodeList l = xpath.selectNodes(node, "./" + ELEMENT_DELAY_ORDER_AFTER_SETBACK);
        if (l != null && l.getLength() > 0) {
            this.delayOrderAfterSetback = new ArrayList<>();
            for (int i = 0; i < l.getLength(); i++) {
                this.delayOrderAfterSetback.add(new DelayOrderAfterSetback(xpath, l.item(i)));
            }
        }
        return xpath;
    }

    public List<DelayOrderAfterSetback> getDelayOrderAfterSetback() {
        return delayOrderAfterSetback;
    }

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public class DelayOrderAfterSetback {

        private static final String ATTR_DELAY = "delay";
        private static final String ATTR_IS_MAXIMUM = "is_maximum";
        private static final String ATTR_SETBACK_COUNT = "setback_count";

        // seconds|HH:MM|HH:MM:SS
        private String delay;
        // yes|no
        private Boolean isMaximum;
        private Integer setbackCount;

        private DelayOrderAfterSetback(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
            Map<String, String> attributes = JS7ConverterHelper.attribute2map(node);
            delay = JS7ConverterHelper.stringValue(attributes.get(ATTR_DELAY));
            isMaximum = JS7ConverterHelper.booleanValue(attributes.get(ATTR_IS_MAXIMUM));
            setbackCount = JS7ConverterHelper.integerValue(attributes.get(ATTR_SETBACK_COUNT));
        }

        public String getDelay() {
            return delay;
        }

        public Boolean getIsMaximum() {
            return isMaximum;
        }

        public Integer getSetbackCount() {
            return setbackCount;
        }

    }
}
