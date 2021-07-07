package com.sos.joc.monitoring.configuration.objects.workflow;

import org.w3c.dom.Node;

import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.monitoring.configuration.AElement;

public class WorkflowJob extends AElement {

    public enum CriticalityType {
        ALL, NORMAL, CRITICAL
    }

    private static String ATTRIBUTE_NAME_NAME = "name";
    private static String ATTRIBUTE_NAME_LABEL = "label";
    private static String ATTRIBUTE_NAME_CRITICALITY = "criticality";
    private static String ATTRIBUTE_NAME_RETURN_CODE_FROM = "return_code_from";
    private static String ATTRIBUTE_NAME_RETURN_CODE_TO = "return_code_to";

    private final String name;
    private final String label;
    private final CriticalityType criticality;
    private final int returnCodeFrom;
    private final int returnCodeTo;
    private final boolean global;

    public WorkflowJob(Node node) throws Exception {
        super(node);
        name = getAttributeValue(ATTRIBUTE_NAME_NAME, AElement.ASTERISK);
        label = getAttributeValue(ATTRIBUTE_NAME_LABEL, AElement.ASTERISK);
        criticality = getCriticality(getElement().getAttribute(ATTRIBUTE_NAME_CRITICALITY));
        returnCodeFrom = getReturnCode(ATTRIBUTE_NAME_RETURN_CODE_FROM);
        returnCodeTo = getReturnCode(ATTRIBUTE_NAME_RETURN_CODE_TO);
        global = name.equals(AElement.ASTERISK) && label.equals(AElement.ASTERISK) && criticality.equals(CriticalityType.ALL) && returnCodeFrom == -1
                && returnCodeTo == -1;
    }

    public static CriticalityType getCriticality(String val) {
        try {
            return CriticalityType.valueOf(val.toUpperCase());
        } catch (Throwable e) {
            return CriticalityType.ALL;
        }
    }

    public static CriticalityType getCriticality(Integer val) {
        try {
            return CriticalityType.valueOf(JobCriticality.fromValue(val).value());
        } catch (Throwable e) {
            return CriticalityType.ALL;
        }
    }

    private int getReturnCode(String name) {
        String r = getAttributeValue(name, AElement.ASTERISK);
        if (!r.equals(AElement.ASTERISK)) {
            try {
                return Integer.parseInt(r);
            } catch (Throwable e) {
            }
        }
        return -1;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public CriticalityType getCriticality() {
        return criticality;
    }

    public int getReturnCodeFrom() {
        return returnCodeFrom;
    }

    public int getReturnCodeTo() {
        return returnCodeTo;
    }

    public boolean isGlobal() {
        return global;
    }
}