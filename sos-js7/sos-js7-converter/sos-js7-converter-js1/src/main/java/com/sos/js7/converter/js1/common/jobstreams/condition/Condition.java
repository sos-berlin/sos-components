package com.sos.js7.converter.js1.common.jobstreams.condition;

public class Condition {

    public enum ConditionType {
        EVENT, GLOBAL, RC, FILEEXIST, JOB, JOBCHAIN, SOS_UNKNOWN
    }

    private static final String EXPR_PREFIX_FILEEXIST = "fileexist:";
    private static final String EXPR_PREFIX_GLOBAL = "global:";
    private static final String EXPR_PREFIX_EVENT = "event:";
    private static final String EXPR_PREFIX_RC = "rc:";
    private static final String EXPR_PREFIX_JOB = "job:";
    private static final String EXPR_PREFIX_JOB_RC = "job:rc:";
    private static final String EXPR_PREFIX_JOBCHAIN = "jobchain:";

    private static final String EXPR_CONTAINS_JOB_RC = ".rc:";

    private ConditionType type;
    private String name;
    private String value;
    private boolean supported;// can be converted

    /** Examples:<br/>
     * fileexist:test.txt<br/>
     * 
     * global:my_event<br/>
     * global:my_event[today]<br />
     * 
     * my_event<br/>
     * event:my_event<br/>
     *
     * rc:0<br/>
     * rc:1-10<br/>
     * job:rc:0<br/>
     * job:my_job.rc:1-10<br/>
     * 
     * job:isCompletedToday<br/>
     * job:my_job.isCompletedToday<br/>
     * 
     * jobChain:isStartedToday<br/>
     * jobChain:my_jobchain.isStartedToday<br/>
     * 
     * @throws Exception */
    public Condition(String val) {
        parse(val.trim());
    }

    private void parse(String original) {
        this.type = ConditionType.SOS_UNKNOWN;

        String val = new String(original).toLowerCase();
        if (val.startsWith(EXPR_PREFIX_GLOBAL)) {
            type = ConditionType.GLOBAL;
            supported = false;
            setNameValue(original, EXPR_PREFIX_GLOBAL.length(), true);
        } else if (val.startsWith(EXPR_PREFIX_FILEEXIST)) {
            type = ConditionType.FILEEXIST;
            supported = false;
            setNameValue(original, EXPR_PREFIX_FILEEXIST.length(), false);
        } else if (val.startsWith(EXPR_PREFIX_RC)) {
            type = ConditionType.RC;
            supported = true;
            setNameValue(original, EXPR_PREFIX_RC.length(), false);
        } else if (val.startsWith(EXPR_PREFIX_JOB_RC)) {
            type = ConditionType.RC;
            supported = true;
            setNameValue(original, EXPR_PREFIX_JOB_RC.length(), false);
        } else if (val.startsWith(EXPR_PREFIX_JOB)) {
            if (val.contains(EXPR_CONTAINS_JOB_RC)) {
                type = ConditionType.RC;
                supported = true;
                int rcIndx = original.indexOf(EXPR_CONTAINS_JOB_RC);
                name = original.substring(EXPR_PREFIX_JOB.length(), rcIndx);
                setNameValue(original, rcIndx + EXPR_CONTAINS_JOB_RC.length(), false);
            } else {
                type = ConditionType.JOB;
                setNameValue(original, EXPR_PREFIX_JOB.length(), true);
            }
        } else if (val.startsWith(EXPR_PREFIX_JOBCHAIN)) {
            type = ConditionType.JOBCHAIN;
            supported = false;
            setNameValue(original, EXPR_PREFIX_JOBCHAIN.length(), true);
        } else {
            if (!val.startsWith(EXPR_PREFIX_EVENT) && val.contains(":")) {
                supported = false;
            } else {
                type = ConditionType.EVENT;
                supported = (value == null);// my_event - supported, my_event[today] - not supported
            }
            setNameValue(original, val.startsWith(EXPR_PREFIX_EVENT) ? EXPR_PREFIX_EVENT.length() : 0, true);
        }
    }

    private void setNameValue(String original, int startIndx, boolean setName) {
        int valIndx = original.indexOf("[");
        if (valIndx > -1 && valIndx >= startIndx) {
            if (setName) {
                this.name = original.substring(startIndx, valIndx);
            }
            this.value = original.substring(valIndx + 1).replaceAll("\\]", "");
        } else {
            String val = null;
            if (setName) {
                this.name = original.substring(startIndx);
            } else {
                val = original.substring(startIndx);
            }
            this.value = val;
        }
    }

    public String getKey() {
        StringBuilder sb = new StringBuilder(type.name());
        if (name != null) {
            sb.append("-").append(name);
        }
        if (value != null) {
            sb.append("-").append(value);
        }
        return sb.toString();
    }

    public ConditionType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isSupported() {
        return supported;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        return sb.toString();
    }
}
