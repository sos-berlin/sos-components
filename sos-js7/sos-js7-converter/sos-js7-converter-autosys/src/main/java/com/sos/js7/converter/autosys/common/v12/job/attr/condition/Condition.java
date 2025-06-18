package com.sos.js7.converter.autosys.common.v12.job.attr.condition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Condition implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Condition.class);

    private static final long serialVersionUID = 1L;

    // reset if:
    // 1) 0 - according to the answer - 0 is the same as in the last 24 hours - is that correct?
    // 2) 24:00
    public static boolean ADJUST_LOOK_BACK_FOR_JS7 = false;

    public enum ConditionType {
        DONE, FAILURE, NOTRUNNING, SUCCESS, TERMINATED, EXITCODE, VARIABLE, JS7_UNKNOWN, JS7_INTERNAL
    }

    private enum MapKeys {
        NAME, LOOKBACK, INSTANCETAG
    }

    private final String originalValue;
    private final ConditionType type;
    private final String name;
    private String value;
    // f(jobA^PROD,24) -> 24
    private final String lookBack;
    // f(jobA^PROD,24) -> ^PROD
    private final String instanceTag;

    private String jobName;
    private boolean dummy;

    // Internal usage
    public Condition(ConditionType type, String name) {
        this.originalValue = null;
        this.type = type;
        this.name = name;
        this.lookBack = null;
        this.instanceTag = null;
    }

    /** <br/>
     * v(app.varA) = "X"<br/>
     * s(app.jobA)<br/>
     * f(jobA^PROD,24)<br/>
     * 
     * @throws Exception */
    public Condition(String val) throws Exception {
        try {
            String v = val.trim();
            this.originalValue = v;
            int i = val.indexOf("(");

            // app.varA
            String innerVal = v.substring(i + 1, v.indexOf(")"));

            Map<MapKeys, String> resultMap = new HashMap<>();
            resultMap = getLookBack(resultMap, innerVal, val);
            resultMap = getExternalInstance(resultMap, val);

            // this.type = getType(v.substring(0, i).toLowerCase(), val);
            this.name = resultMap.get(MapKeys.NAME);
            this.value = getValue(v);
            this.lookBack = getLookBack(resultMap);
            this.instanceTag = resultMap.get(MapKeys.INSTANCETAG);
            this.type = getType(v.substring(0, i).toLowerCase(), val);
        } catch (Throwable e) {
            throw new Exception(String.format("[%s]%s", val, e.toString()), e);
        }
    }

    private String getLookBack(Map<MapKeys, String> resultMap) {
        String l = resultMap.get(MapKeys.LOOKBACK);

        if (ADJUST_LOOK_BACK_FOR_JS7) {
            if (l != null) {
                if (l.equals("0") || l.equals("24") || l.equals("24:00")) {
                    l = null;
                }
            }
        }
        return l;
    }

    private ConditionType getType(String t, String original) {
        jobName = null;

        ConditionType type = ConditionType.JS7_UNKNOWN;
        switch (t) {
        case "d":
        case "done":
            type = ConditionType.DONE;
            jobName = name;
            break;
        case "f":
        case "failure":
            type = ConditionType.FAILURE;
            jobName = name;
            break;
        case "s":
        case "success":
            type = ConditionType.SUCCESS;
            jobName = name;
            break;
        case "v":
        case "value":
            type = ConditionType.VARIABLE;
            break;
        case "n":
        case "notrunning":
            type = ConditionType.NOTRUNNING;
            jobName = name;
            break;
        case "t":
        case "terminated":
            type = ConditionType.TERMINATED;
            jobName = name;
            break;
        default:
            break;
        }
        return type;
    }

    private Map<MapKeys, String> getLookBack(Map<MapKeys, String> map, String val, String original) {
        String[] arr = val.split(",");
        if (arr.length == 2) {
            map.put(MapKeys.NAME, arr[0].trim());
            map.put(MapKeys.LOOKBACK, arr[1].trim());
        } else {
            map.put(MapKeys.NAME, val);
        }
        return map;
    }

    private Map<MapKeys, String> getExternalInstance(Map<MapKeys, String> map, String original) {
        String[] arr = map.get(MapKeys.NAME).split("\\^");
        if (arr.length == 2) {
            map.put(MapKeys.NAME, arr[0].trim());
            map.put(MapKeys.INSTANCETAG, arr[1].trim());
        }
        return map;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    private String getValue(String val) {
        String[] arr = val.split("=");
        return arr.length == 2 ? JS7ConverterHelper.stringValue(arr[1]) : null;
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

    public String getLookBack() {
        return lookBack;
    }

    /** status(job_name, hhhh.mm)<br/>
     * status(job_name^INS, hhhh.mm)<br/>
     * exitcode(job_name, hhhh.mm) operator value <br/>
     * exitcode(job_name^INS, hhhh.mm) operator value<br/>
     * - hhhh Indicates the hours taken for the last run of the condition or predecessor job. You can look back approximately 416.58 days.<br/>
     * Limits: 0-9999 <br/>
     * - mm Indicates the minutes taken for the last run of the condition or predecessor job.<br/>
     * Limits: 0-59 when specifying minutes; 0-9998 for hhhh when specifying hours and minutes (for example, 9998.59)<br/>
     * Examples: 0, 00.15, 06.30, 23.30, 24, 98.30, 720, 9998.59, 9999<br/>
     * <br/>
     * Instead of using a period (.) to separate hhhh and mm, you can use a colon that must be escaped (\:) as shown in the following example: condition:
     * success(Joba,01\:00) and failure(JobB,02\:15) */
    public Integer getLookBackAsMinutes() {
        if (lookBack == null) {
            return null;
        }
        if ("0".equals(lookBack)) {
            return Integer.valueOf(-1);
        }
        // if ("9999".equals(lookBack)) {
        // return Integer.MAX_VALUE;
        // }
        String[] parts = lookBack.split("\\.|\\\\:");
        int hours = 0;
        int minutes = 0;
        try {
            if (parts.length == 1) {
                hours = Integer.parseInt(parts[0]);
            }
            // "06.30" or "01\:00"
            else if (parts.length == 2) {
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
                if (minutes < 0 || minutes > 59) {
                    throw new IllegalArgumentException("[invalid minutes]" + minutes);
                }
            } else {
                throw new IllegalArgumentException("invalid lookback format");
            }
            if (hours < 0 || hours > 9999) {
                throw new IllegalArgumentException("[invalid hours]" + hours);
            }
        } catch (Throwable e) {
            LOGGER.error("[" + lookBack + "]" + e.toString());
            return null;
        }
        return Integer.valueOf(hours * 60 + minutes);
    }

    public String getInstanceTag() {
        return instanceTag;
    }

    public String getKey() {
        return getKey(true);
    }

    public String getKeyWithoutInstance() {
        return getKey(false);
    }

    private String getKey(boolean withInstance) {
        StringBuilder sb = new StringBuilder(type.name());
        sb.append("-").append(name);
        if (instanceTag != null && withInstance) {
            sb.append("-").append(instanceTag);
        }
        if (lookBack != null) {
            sb.append("-").append(lookBack.replaceAll(":", ""));
        }
        if (value != null) {
            sb.append("-").append(value);
        }
        return sb.toString();
    }

    public String getJobName() {
        return jobName;
    }

    public boolean isSuccess() {
        return ConditionType.SUCCESS.equals(type);
    }

    public boolean isNotrunning() {
        return ConditionType.NOTRUNNING.equals(type);
    }

    public void setDummy() {
        dummy = true;
    }

    public boolean isDummy() {
        return dummy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(type.name());
        sb.append(" ").append(getInfo());
        if (jobName != null) {
            // sb.append("(jobName=").append(jobName).append(")");
        }
        return sb.toString();
    }

    public String getInfo() {
        StringBuilder sb = new StringBuilder(name == null ? "" : name);
        if (instanceTag != null) {
            sb.append("^").append(instanceTag);
        }
        if (lookBack != null) {
            sb.append("(").append(lookBack).append(")");
        }
        if (value != null) {
            sb.append("=").append(value);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Condition) {
            return getKey().equals(((Condition) other).getKey());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

}
