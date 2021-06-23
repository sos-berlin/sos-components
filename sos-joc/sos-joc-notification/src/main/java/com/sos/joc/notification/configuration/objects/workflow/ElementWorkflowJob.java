package com.sos.joc.notification.configuration.objects.workflow;

import org.w3c.dom.Node;

import com.sos.joc.notification.configuration.AElement;

public class ElementWorkflowJob extends AElement {

    private static String ATTRIBUTE_NAME_NAME = "name";
    private static String ATTRIBUTE_NAME_LABEL = "label";
    private static String ATTRIBUTE_NAME_RETURN_CODE_FROM = "return_code_from";
    private static String ATTRIBUTE_NAME_RETURN_CODE_TO = "return_code_to";

    private final String name;
    private final String label;
    private final String returnCodeFrom;
    private final String returnCodeTo;

    public ElementWorkflowJob(Node node) throws Exception {
        super(node);
        name = getElement().getAttribute(ATTRIBUTE_NAME_NAME);
        label = getElement().getAttribute(ATTRIBUTE_NAME_LABEL);
        returnCodeFrom = getElement().getAttribute(ATTRIBUTE_NAME_RETURN_CODE_FROM);
        returnCodeTo = getElement().getAttribute(ATTRIBUTE_NAME_RETURN_CODE_TO);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getReturnCodeFrom() {
        return returnCodeFrom;
    }

    public String getReturnCodeTo() {
        return returnCodeTo;
    }
}