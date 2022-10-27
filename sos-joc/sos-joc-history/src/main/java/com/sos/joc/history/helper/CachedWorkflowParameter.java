package com.sos.joc.history.helper;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;

public class CachedWorkflowParameter {

    private final ParameterType type;
    private final String name;
    private final String value;

    public CachedWorkflowParameter(String name, Parameter p) {
        this.type = p.getType();
        this.name = name;
        this.value = getValue(p);

        // if (p.getListParameters() != null && p.getListParameters().getAdditionalProperties() != null) {
        // p.getListParameters().getAdditionalProperties().forEach((listParamName, listParam) -> {
        // });
        // }
    }

    private String getValue(Parameter p) {
        String val = null;
        if (p.getDefault() != null) {
            try {
                val = trim(p.getDefault().toString(), "\"");
            } catch (Throwable e) {
            }
        } else if (!SOSString.isEmpty(p.getFinal())) {
            try {
                val = trim(p.getFinal(), "'");
            } catch (Throwable e) {
            }
        }
        return val;
    }

    private String trim(String val, String stripChar) {
        if (val.startsWith(stripChar) && val.endsWith(stripChar)) {// strip "
            int len = val.length();
            switch (len) {
            case 2:// val="\"\"";
                val = "";
                break;
            default:
                val = val.substring(1, len - 1);
                break;
            }
        }
        return val;
    }

    public ParameterType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
