package com.sos.js7.converter.js1.common.jobchain.node;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public abstract class AJobChainNode {

    public enum JobChainNodeType {
        NODE, ORDER_SOURCE, ORDER_SINK, JOB_CHAIN, END
    }

    private final Path jobChainPath;
    private final JobChainNodeType type;
    private final Map<String, String> attributes;

    protected AJobChainNode(Path jobChainPath, JobChainNodeType type, Node node) {
        this.jobChainPath = jobChainPath;
        this.type = type;
        this.attributes = JS7ConverterHelper.attribute2map(node);
    }

    protected Map<String, String> getAttributes() {
        return attributes;
    }

    public static AJobChainNode parse(DirectoryParserResult pr, Path jobChainPath, Node node) {
        AJobChainNode jcn = null;
        switch (node.getNodeName().toLowerCase()) {
        case "job_chain_node":
            jcn = new JobChainNode(pr, jobChainPath, JobChainNodeType.NODE, node);
            break;
        case "file_order_source":
            jcn = new JobChainNodeFileOrderSource(jobChainPath, JobChainNodeType.ORDER_SOURCE, node);
            break;
        case "file_order_sink":
            jcn = new JobChainNodeFileOrderSink(jobChainPath, JobChainNodeType.ORDER_SINK, node);
            break;
        case "job_chain_node.job_chain":
            jcn = new JobChainNodeJobChain(jobChainPath, JobChainNodeType.JOB_CHAIN, node);
            break;
        case "job_chain_node.end":
            jcn = new JobChainNodeEnd(jobChainPath, JobChainNodeType.END, node);
            break;
        }
        return jcn;
    }

    public JobChainNodeType getType() {
        return type;
    }

    public Path getJobChainPath() {
        return jobChainPath;
    }
}
