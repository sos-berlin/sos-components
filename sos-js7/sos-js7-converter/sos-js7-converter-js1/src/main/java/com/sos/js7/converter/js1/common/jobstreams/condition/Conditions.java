package com.sos.js7.converter.js1.common.jobstreams.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobstreams.condition.Condition.ConditionType;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamJS1JS7Job;

public class Conditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conditions.class);

    public enum Operator {
        AND, OR, NOT, AND_NOT, OR_NOT
    }

    private static final String OPERATOR_PLACEHOLDER_AND_NOT = "•";
    private static final String OPERATOR_PLACEHOLDER_OR_NOT = "½";
    private static final String OPERATOR_PLACEHOLDER_NOT = "!"; // "¿"; // ¶

    private int maxGroupLevel = 0;

    /** <br/>
     * fileexist:test.txt and global:my_event and global:my_event[today] and my_event and my_event[today] and rc:0 and rc:[0-10] and job:rc:1 <br/>
     * 
     * @param val
     * @return List of Condition and Operator or nested List
     * @throws Exception */
    public List<Object> parse(String val) throws Exception {
        maxGroupLevel = 0;

        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        String method = "parse";
        if (SOSString.isEmpty(val)) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s][%s][start][skip]because empty", method, val));
            }
            return null;
        }

        val = val.trim();
        val = val.replaceAll("\\(", " (").replaceAll("\\)", ") ");
        val = val.replaceAll("\\s+", " ");
        val = val.replaceAll(" (?i)and ", " & ").replaceAll("&&", "&");
        val = val.replaceAll(" (?i)or ", " | ").replaceAll("\\|\\|", "|");
        val = val.replaceAll(" (?i)& not ", " " + OPERATOR_PLACEHOLDER_AND_NOT + " ");
        val = val.replaceAll(" & ! ", " " + OPERATOR_PLACEHOLDER_AND_NOT + " ");
        val = val.replaceAll(" (?i)\\| not ", " " + OPERATOR_PLACEHOLDER_OR_NOT + " ");
        val = val.replaceAll(" \\| ! ", " " + OPERATOR_PLACEHOLDER_OR_NOT + " ");
        val = val.replaceAll("^not ", OPERATOR_PLACEHOLDER_NOT + " ");
        // val = val.replaceAll("^not\\(", OPERATOR_PLACEHOLDER_NOT + " "); // TODO - ????
        // val = val.replaceAll(" \\(", "(").replaceAll("\\) ", ")");

        if (isTraceEnabled) {
            LOGGER.trace(String.format("[%s][%s][start]...", method, val));
        }

        int groupLevel = 0;
        boolean groupBegin = false;
        boolean groupEnd = false;
        boolean partBegin = false;
        boolean partEnd = false;

        List<Object> result = new ArrayList<>();
        List<Object> groupResult = null;
        StringBuilder sb = new StringBuilder();
        String c = new String(val.trim());
        int counter = 0;
        while (c.length() > 0) {
            String s = c.substring(0, 1);
            int substrPos = 1;
            if (s.trim().length() == 0) {
                if (partBegin) {
                    partEnd = true;
                    partBegin = false;
                }

                c = c.substring(substrPos);
                continue;
            }

            boolean append = true;
            if (s.equals("(")) {
                if (!partBegin) {
                    groupLevel++;

                    groupBegin = true;
                    groupEnd = false;
                    groupResult = new ArrayList<>();
                    append = false;
                }
            } else if (s.equals(")")) {
                if (partBegin) {
                    if (groupBegin) {
                        if (groupLevel > maxGroupLevel) {
                            maxGroupLevel = groupLevel;
                        }
                        groupLevel--;

                        groupEnd = true;
                        append = false;
                        if (groupResult != null) {
                            if (sb.length() > 0) {
                                // TODO check if Operator ???
                                groupResult.add(new Condition(sb.toString().trim()));
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("[%s][group begin=%s,level=%s][part begin=%s][groupResult][add]condition=%s", method,
                                            groupBegin, groupLevel, partBegin, sb));
                                }
                                sb = new StringBuilder();
                            }
                            result.add(groupResult);
                            groupResult = null;
                        }
                    }
                    partEnd = true;
                    partBegin = false;
                } else {
                    groupEnd = true;
                    append = false;
                    if (groupResult != null) {
                        if (sb.length() > 0) {
                            // TODO check if Operator ???
                            groupResult.add(new Condition(sb.toString().trim()));
                            if (isTraceEnabled) {
                                LOGGER.trace(String.format("[%s][group begin=%s,level=%s][part begin=%s][groupResult][add]condition=%s", method,
                                        groupBegin, groupLevel, partBegin, sb));
                            }
                            sb = new StringBuilder();
                        }

                        result.add(groupResult);
                        groupResult = null;
                    }
                }
            } else {
                if (partEnd) {
                    Operator operator = null;
                    switch (s.toLowerCase()) {
                    case "&":
                        operator = Operator.AND;
                        break;
                    case "|":
                        operator = Operator.OR;
                        break;
                    case OPERATOR_PLACEHOLDER_AND_NOT:
                        operator = Operator.AND_NOT;
                        break;
                    case OPERATOR_PLACEHOLDER_OR_NOT:
                        operator = Operator.OR_NOT;
                        break;
                    case OPERATOR_PLACEHOLDER_NOT:
                        operator = Operator.NOT;
                        break;
                    default:
                        break;
                    }

                    if (operator != null) {
                        if (sb.length() > 0) {
                            Condition cp = new Condition(sb.toString().trim());
                            if (groupResult == null) {
                                result.add(cp);
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("[%s][result][add]condition=%s", method, sb));
                                }
                            } else {
                                groupResult.add(cp);
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("[%s][groupResult level=%s][add]condition=%s", method, groupLevel, sb));
                                }
                            }
                            sb = new StringBuilder();
                        }

                        if (groupResult == null) {
                            result.add(operator);
                            if (isTraceEnabled) {
                                LOGGER.trace(String.format("[%s][result][add][operator=%s]%s", method, operator, sb));
                            }
                        } else {
                            groupResult.add(operator);
                            if (isTraceEnabled) {
                                LOGGER.trace(String.format("[%s][groupResult level=%s][add][operator=%s]%s", method, groupLevel, operator, sb));
                            }
                        }

                        partBegin = false;
                        partEnd = false;

                        append = false;
                    }

                } else {
                    if (counter == 0 && s.equals(OPERATOR_PLACEHOLDER_NOT)) {
                        result.add(Operator.NOT);
                        if (isTraceEnabled) {
                            LOGGER.trace(String.format("[%s][result][add][operator=%s]%s", method, Operator.NOT, sb));
                        }
                        append = false;
                    } else {
                        partBegin = true;
                    }
                }
            }

            if (append) {
                sb.append(s);
            }

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s][group begin=%s,end=%s,level=%s][part begin=%s,end=%s][%s]%s", method, groupBegin, groupEnd,
                        groupLevel, partBegin, partEnd, s, sb));
            }

            c = c.substring(substrPos);
            counter++;
        }
        if (sb.length() > 0) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s][condition][addOnEnd][result]%s", method, sb));
            }
            result.add(new Condition(sb.toString().trim()));
        }

        if (isTraceEnabled) {
            LOGGER.trace(String.format("[%s][%s][end]", method, val));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<ConditionType, List<Condition>> getConditionsByType(List<Object> conditions) {
        if (conditions == null || conditions.size() == 0) {
            return Collections.emptyMap();
        }
        Map<ConditionType, List<Condition>> result = new HashMap<>();
        for (Object o : conditions) {
            if (o instanceof Condition) {
                Condition c = (Condition) o;
                List<Condition> l = result.get(c.getType());
                if (l == null) {
                    l = new ArrayList<>();
                }
                l.add(c);
                result.put(c.getType(), l);
            } else if (o instanceof List) {
                result.putAll(getConditionsByType((List<Object>) o));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<Operator, List<Operator>> getOperatorsByType(List<Object> conditions) {
        if (conditions == null || conditions.size() == 0) {
            return Collections.emptyMap();
        }
        Map<Operator, List<Operator>> result = new HashMap<>();
        for (Object o : conditions) {
            if (o instanceof Operator) {
                Operator c = (Operator) o;
                List<Operator> l = result.get(c);
                if (l == null) {
                    l = new ArrayList<>();
                }
                l.add(c);
                result.put(c, l);
            } else if (o instanceof List) {
                result.putAll(getOperatorsByType((List<Object>) o));
            }
        }
        return result;
    }

    public static boolean onlyANDConditions(Map<Operator, List<Operator>> operators) {
        if (operators == null) {
            return true;
        }
        switch (operators.size()) {
        case 0:
            return true;
        case 1:
            Operator op = operators.keySet().stream().findFirst().orElse(null);
            if (op != null && op.equals(Operator.AND)) {
                return true;
            }
            break;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static List<Condition> getConditions(List<Object> conditions) {
        if (conditions == null || conditions.size() == 0) {
            return Collections.emptyList();
        }
        List<Condition> result = new ArrayList<>();
        for (Object o : conditions) {
            if (o instanceof Condition) {
                result.add((Condition) o);
            } else if (o instanceof List) {
                result.addAll(getConditions((List<Object>) o));
            }
        }
        return result;
    }

    // ----- TODO all job handling methods
    // ------- USE JobStreamJS1JS7Job.js1OutEventNames instead of job name
    public boolean hasJob(List<Condition> conditions, StandaloneJob job) {
        return conditions.stream().filter(e -> e.getType().equals(ConditionType.EVENT) && e.getName().equals(job.getName())).count() > 0;
    }

    public boolean hasAllJobs(List<Condition> conditions, Map<String, List<JobStreamJS1JS7Job>> jobs) {
        // boolean found = false;
        long count = 0;
        int eventConditions = 0;
        for (Condition c : conditions) {
            if (c.getType().equals(ConditionType.EVENT)) {
                // found = jobs.entrySet().stream().filter(e -> e.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob()
                // .getJob()).equals(c.getName())).count() > 0).count() > 0;

                for (Map.Entry<String, List<JobStreamJS1JS7Job>> entry : jobs.entrySet()) {
                    // found = entry.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob().getJob()).equals(c.getName()))
                    // .count() > 0;

                    // LOGGER.info("xxx=" + entry.getKey() + "=found=" + found + "=" + toString(conditions));

                    // if (!found) {
                    // return found;
                    // }
                    long cc = entry.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob().getJob()).equals(c
                            .getName())).count();

                    count += cc;
                }
                // LOGGER.info("XXXXXXXXXXXXXX=" + found + "=" + toString(conditions));
                // if (!found) {
                // return found;
                // }
                eventConditions++;
            }
        }

        // LOGGER.info("YYY=" + eventConditions + "=" + count);

        return eventConditions <= count;
        // return found;
    }

    public boolean hasAllJobsXX(List<Object> allObjects, Map<String, List<JobStreamJS1JS7Job>> jobs) {
        List<Condition> conditions = getConditions(allObjects);

        boolean found = false;
        long count = 0;
        int eventConditions = 0;
        for (Condition c : conditions) {
            if (c.getType().equals(ConditionType.EVENT)) {
                // found = jobs.entrySet().stream().filter(e -> e.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob()
                // .getJob()).equals(c.getName())).count() > 0).count() > 0;

                for (Map.Entry<String, List<JobStreamJS1JS7Job>> entry : jobs.entrySet()) {
                    found = entry.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob().getJob()).equals(c.getName()))
                            .count() > 0;

                    LOGGER.info("xxx=" + entry.getKey() + "=found=" + found + "=" + toString(conditions));

                    if (!found) {
                        // return found;
                    }
                    count += entry.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob().getJob()).equals(c
                            .getName())).count();
                }
                // LOGGER.info("XXXXXXXXXXXXXX=" + found + "=" + toString(conditions));
                // if (!found) {
                // return found;
                // }
                eventConditions++;
            }
        }

        LOGGER.info("YYY=" + eventConditions + "=" + count);

        return eventConditions <= count;
        // return found;
    }

    public String toString(List<Condition> conditions) {
        return conditions.stream().map(o -> SOSString.toString(o)).collect(Collectors.joining(","));
    }

    public List<JobStreamJS1JS7Job> getAllJobs(List<Condition> conditions, Map<String, List<JobStreamJS1JS7Job>> jobs) {
        List<JobStreamJS1JS7Job> result = new ArrayList<>();
        for (Condition c : conditions) {
            if (c.getType().equals(ConditionType.EVENT)) {
                for (Map.Entry<String, List<JobStreamJS1JS7Job>> entry : jobs.entrySet()) {
                    List<JobStreamJS1JS7Job> rjobs = entry.getValue().stream().filter(j -> JS7ConverterHelper.getFileName(j.getJS1JobStreamJob()
                            .getJob()).equals(c.getName())).collect(Collectors.toList());
                    if (rjobs != null) {
                        result.addAll(rjobs);
                    }
                }
            }
        }
        return result;
    }

    public boolean tmpHasAllJobs(List<Object> allObjects, List<JobStreamJob> jobs) {
        List<Condition> conditions = getConditions(allObjects);

        boolean found = false;
        for (Condition c : conditions) {
            if (c.getType().equals(ConditionType.EVENT)) {
                found = jobs.stream().filter(j -> JS7ConverterHelper.getFileName(j.getJob()).equals(c.getName())).count() > 0;
                if (!found) {
                    return found;
                }
            }
        }
        return found;
    }

    public int getMaxGroupLevel() {
        return maxGroupLevel;
    }

    // TODO recursive
    public static Condition find(List<Object> conditions, String conditionKey) {
        if (conditions == null) {
            return null;
        }
        return conditions.stream().filter(c -> c instanceof Condition).map(c -> (Condition) c).filter(c -> c.getKey().equals(conditionKey)).findAny()
                .orElse(null);
    }

    // TODO recursive
    public static List<Object> remove(List<Object> conditions, Condition condition) {
        if (conditions == null) {
            return conditions;
        }
        Condition c = find(conditions, condition.getKey());
        if (c == null) {
            return conditions;
        }

        int i = conditions.indexOf(c);
        if (i > -1) {
            List<Object> toRemove = new ArrayList<>();
            toRemove.add(c);
            int next = i + 1;
            if (conditions.size() > next) {
                Object op = conditions.get(next);
                if (op != null && op instanceof Operator) {
                    toRemove.add(op);
                }
            }
            conditions.removeAll(toRemove);
        }
        return conditions;
    }

}
