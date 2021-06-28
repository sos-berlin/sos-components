package com.sos.joc.monitoring.configuration.objects.workflow;

import org.w3c.dom.Node;

import com.sos.joc.monitoring.configuration.AElement;

public class ElementWorkflow extends AElement {

    private static String ATTRIBUTE_NAME_NAME = "name";
    private static String ATTRIBUTE_NAME_CONTROLLER_ID = "controller_ID";

    private final String name;
    private final String controllerId;

    public ElementWorkflow(Node node) throws Exception {
        super(node);
        name = getElement().getAttribute(ATTRIBUTE_NAME_NAME);
        controllerId = getElement().getAttribute(ATTRIBUTE_NAME_CONTROLLER_ID);
    }
    
    public String getName() {
        return name;
    }

    public String getControllerId() {
        return controllerId;
    }

}