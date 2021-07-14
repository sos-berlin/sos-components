package com.sos.joc.monitoring.configuration.objects.workflow;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.AElement;

public class Workflow extends AElement {

    private static String ATTRIBUTE_NAME_PATH = "path";
    
    private final String controllerId;
    private final String path;
    private final boolean global;

    private List<WorkflowJob> jobs;

    public Workflow(Node node, String controllerId) throws Exception {
        super(node);
        this.controllerId = controllerId;

        path = getAttributeValue(ATTRIBUTE_NAME_PATH, AElement.ASTERISK);
        // controllerId = getAttributeValue(ATTRIBUTE_NAME_CONTROLLER_ID, AElement.ASTERISK);
        global = path.equals(AElement.ASTERISK) && controllerId.equals(AElement.ASTERISK);
        jobs = new ArrayList<>();
        process();
    }

    private void process() {
        List<Element> children = SOSXML.getChildElemens(getElement());
        boolean hasGlobalJob = false;

        addJobs: if (children != null) {
            for (Element child : children) {
                try {
                    WorkflowJob job = new WorkflowJob((Node) child);
                    hasGlobalJob = job.isGlobal();
                    if (hasGlobalJob) {
                        break addJobs;
                    }

                    jobs.add(job);
                } catch (Exception e) {

                }
            }
        }

        if (hasGlobalJob) {
            jobs = new ArrayList<>();
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

    public boolean isGlobal() {
        return global && jobs.size() == 0;
    }
}