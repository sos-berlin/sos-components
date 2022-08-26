package com.sos.joc.monitoring.configuration.objects.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.AElement;

public class Workflow extends AElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(Workflow.class);

    private static String ATTRIBUTE_NAME_PATH = "path";

    private final String controllerId;
    private final String path;

    private List<WorkflowJob> jobs;

    public Workflow(Node node, String controllerId) throws Exception {
        super(node);
        this.controllerId = controllerId;

        path = getAttributeValue(ATTRIBUTE_NAME_PATH, AElement.ASTERISK);
        process();
    }

    private void process() {
        jobs = new ArrayList<>();

        List<Element> children = SOSXML.getChildElemens(getElement());
        if (children != null) {
            List<WorkflowJob> l = new ArrayList<>();
            for (Element child : children) {
                try {
                    l.add(new WorkflowJob((Node) child));
                } catch (Exception e) {
                    LOGGER.error("[process]" + e.toString(), e);
                }
            }
            if (l.size() > 0) {
                jobs = l.stream().filter(SOSCollection.distinctByKey(e -> e.hashCode())).collect(Collectors.toList());
            }
        }
    }

    public String getPath() {
        return path;
    }

    public String getControllerId() {
        return controllerId;
    }

    public List<WorkflowJob> getJobs() {
        return jobs;
    }

}