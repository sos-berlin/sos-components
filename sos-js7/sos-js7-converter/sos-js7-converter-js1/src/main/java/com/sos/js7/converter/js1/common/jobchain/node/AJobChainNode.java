package com.sos.js7.converter.js1.common.jobchain.node;

import java.util.Map;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public abstract class AJobChainNode {

    public enum JobChainNodeType {
        NODE, ORDER_SOURCE, ORDER_SINK, JOB_CHAIN, END
    }

    private final Map<String, String> attributes;
    private final JobChainNodeType type;

    protected AJobChainNode(Node node, JobChainNodeType type) {
        this.attributes = JS7ConverterHelper.attribute2map(node);
        this.type = type;
    }

    protected Map<String, String> getAttributes() {
        return attributes;
    }

    public static AJobChainNode parse(Node node) {
        AJobChainNode jcn = null;
        switch (node.getNodeName().toLowerCase()) {
        case "job_chain_node":
            jcn = new JobChainNode(node, JobChainNodeType.NODE);
            break;
        case "file_order_source":
            jcn = new JobChainNodeFileOrderSource(node, JobChainNodeType.ORDER_SOURCE);
            break;
        case "file_order_sink":
            jcn = new JobChainNodeFileOrderSink(node, JobChainNodeType.ORDER_SINK);
            break;
        case "job_chain_node.job_chain":
            jcn = new JobChainNodeJobChain(node, JobChainNodeType.JOB_CHAIN);
            break;
        case "job_chain_node.end":
            jcn = new JobChainNodeEnd(node, JobChainNodeType.END);
            break;
        }
        return jcn;
    }

    public JobChainNodeType getType() {
        return type;
    }
}
