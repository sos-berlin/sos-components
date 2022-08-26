package com.sos.joc.monitoring.configuration.objects.workflow;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.monitoring.configuration.AElement;

public class WorkflowJob extends AElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowJob.class);

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

    public WorkflowJob(Node node) throws Exception {
        super(node);
        name = getAttributeValue(ATTRIBUTE_NAME_NAME, AElement.ASTERISK);
        label = getAttributeValue(ATTRIBUTE_NAME_LABEL, AElement.ASTERISK);
        criticality = getCriticality(getElement().getAttribute(ATTRIBUTE_NAME_CRITICALITY));
        returnCodeFrom = getReturnCode(ATTRIBUTE_NAME_RETURN_CODE_FROM);
        returnCodeTo = getReturnCode(ATTRIBUTE_NAME_RETURN_CODE_TO);
    }

    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.append(name);
        b.append(label);
        b.append(criticality.name());
        b.append(returnCodeFrom);
        b.append(returnCodeTo);
        return b.toHashCode();
    }

    public static CriticalityType getCriticality(String val) {
        try {
            return CriticalityType.valueOf(val.toUpperCase());
        } catch (Throwable e) {
            LOGGER.error(String.format("[config][parse criticality=%s]%s", val.toUpperCase(), e.toString()), e);
            return CriticalityType.ALL;
        }
    }

    public static CriticalityType getCriticality(Integer val) {
        try {
            return CriticalityType.valueOf(JobCriticality.fromValue(val).value());
        } catch (Throwable e) {
            LOGGER.error(String.format("[config][parse criticality=%s]%s", val, e.toString()), e);
            return CriticalityType.ALL;
        }
    }

    private int getReturnCode(String name) {
        String r = getAttributeValue(name, AElement.ASTERISK);
        if (!r.equals(AElement.ASTERISK)) {
            try {
                return Integer.parseInt(r);
            } catch (Throwable e) {
                LOGGER.error(String.format("[config][parse return_code=%s]%s", r, e.toString()), e);
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
    
}