package com.sos.joc.monitoring.configuration.objects.workflow;

import org.w3c.dom.Node;

import com.sos.joc.monitoring.configuration.AElement;

public class WorkflowJob extends AElement {

    private static String ATTRIBUTE_NAME_NAME = "name";
    private static String ATTRIBUTE_NAME_LABEL = "label";
    private static String ATTRIBUTE_NAME_RETURN_CODE_FROM = "return_code_from";
    private static String ATTRIBUTE_NAME_RETURN_CODE_TO = "return_code_to";

    private final String name;
    private final String label;
    private final String returnCodeFrom;
    private final String returnCodeTo;
    private final boolean global;

    public WorkflowJob(Node node) throws Exception {
        super(node);
        name = getAttributeValue(ATTRIBUTE_NAME_NAME, AElement.ASTERISK);
        label = getAttributeValue(ATTRIBUTE_NAME_LABEL, AElement.ASTERISK);
        returnCodeFrom = getAttributeValue(ATTRIBUTE_NAME_RETURN_CODE_FROM, AElement.ASTERISK);
        returnCodeTo = getAttributeValue(ATTRIBUTE_NAME_RETURN_CODE_TO, AElement.ASTERISK);
        global = name.equals(AElement.ASTERISK) && label.equals(AElement.ASTERISK) && returnCodeFrom.equals(AElement.ASTERISK) && returnCodeTo.equals(
                AElement.ASTERISK);
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

    public boolean isGlobal() {
        return global;
    }
}