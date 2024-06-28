package com.sos.joc.monitoring.configuration.objects.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.monitoring.configuration.AElement;

public class WorkflowJob extends AElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowJob.class);

    private static String ATTRIBUTE_NAME_NAME = "name";
    private static String ATTRIBUTE_NAME_LABEL = "label";
    private static String ATTRIBUTE_NAME_CRITICALITY = "criticality";
    private static String ATTRIBUTE_NAME_RETURN_CODE_FROM = "return_code_from";
    private static String ATTRIBUTE_NAME_RETURN_CODE_TO = "return_code_to";

    private final String name;
    private final String label;
    private final List<Integer> criticalities;
    private final String criticalitiesNames;
    private final int returnCodeFrom;
    private final int returnCodeTo;

    public WorkflowJob(Node node) throws Exception {
        super(node);
        name = getAttributeValue(ATTRIBUTE_NAME_NAME, AElement.ASTERISK);
        label = getAttributeValue(ATTRIBUTE_NAME_LABEL, AElement.ASTERISK);
        criticalities = evaluateCriticalities();
        criticalitiesNames = evaluateCriticalitiesNames();
        returnCodeFrom = getReturnCode(ATTRIBUTE_NAME_RETURN_CODE_FROM);
        returnCodeTo = getReturnCode(ATTRIBUTE_NAME_RETURN_CODE_TO);
    }

    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.append(name);
        b.append(label);
        b.append(criticalitiesNames);
        b.append(returnCodeFrom);
        b.append(returnCodeTo);
        return b.toHashCode();
    }

    private List<Integer> evaluateCriticalities() {
        List<Integer> result = new ArrayList<>();
        String criticalities = getElement().getAttribute(ATTRIBUTE_NAME_CRITICALITY);
        if (SOSString.isEmpty(criticalities)) {
            return result;
        }

        try {
            String[] values = criticalities.split(" ");
            for (String val : values) {
                if (val.trim().length() == 0) {
                    continue;
                }
                if ("ALL".equals(val.toUpperCase())) {// compatibility with earlier versions
                    return new ArrayList<>();
                }

                JobCriticality t = JobCriticality.valueOf(val.toUpperCase());
                if (!result.contains(t.intValue())) {
                    result.add(t.intValue());
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[config][parse criticalities=%s]%s", criticalities, e.toString()), e);
            result = new ArrayList<>();
        }
        return result;
    }

    private String evaluateCriticalitiesNames() {
        List<String> l = new ArrayList<>();
        if (criticalities == null || criticalities.size() == 0) {
            for (JobCriticality c : JobCriticality.values()) {
                l.add(c.value());
            }
        } else {
            for (Integer c : criticalities) {
                try {
                    l.add(JobCriticality.fromValue(c).value());
                } catch (Throwable e) {
                    LOGGER.error(String.format("[evaluateCriticalitiesNames][parse JobCriticality IntValue=%s]%s", c, e.toString()), e);
                }
            }
        }
        return "[" + String.join(",", l) + "]";
    }

    public boolean criticalityMatches(Integer criticality) {
        if (criticalities == null || criticalities.size() == 0) {
            return true;
        }
        return criticalities.contains(criticality);
    }

    public static String getCriticalityName(Integer val) {
        try {
            return JobCriticality.fromValue(val).value();
        } catch (Throwable e) {
            LOGGER.error(String.format("[getCriticalityName][parse criticality=%s]%s", val, e.toString()), e);
            return "UNKNOWN";
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

    public String getCriticalitiesNames() {
        return criticalitiesNames;
    }

    public int getReturnCodeFrom() {
        return returnCodeFrom;
    }

    public int getReturnCodeTo() {
        return returnCodeTo;
    }

}