package com.sos.js7.converter.autosys.common.v12.job.attr.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;

public class Conditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conditions.class);

    public enum Operator {
        AND, OR
    }

    /** <br/>
     * v(app.varA) = "X" & s(app.JobA) & s(app.JobB) <br/>
     * (v(app.varA) = "X" & s(app.JobA)) | s(app.JobB) <br/>
     * 
     * @param val
     * @return List of Condition and Operator or nested List
     * @throws Exception */
    public static List<Object> parse(String val) throws Exception {
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        String method = "parse";
        if (isTraceEnabled) {
            LOGGER.trace(String.format("[%s][%s][start]...", method, val));
        }
        if (SOSString.isEmpty(val)) {
            return null;
        }
        boolean groupBegin = false;
        boolean groupEnd = false;
        boolean partBegin = false;
        boolean partEnd = false;
        boolean valueBegin = false;
        boolean valueEnd = false;
        int valueCounter = 0;

        List<Object> result = new ArrayList<>();
        List<Object> groupResult = null;
        StringBuilder sb = new StringBuilder();
        String c = new String(val.trim());
        while (c.length() > 0) {
            String s = c.substring(0, 1);
            int substrPos = 1;
            if (s.trim().length() == 0) {
                c = c.substring(substrPos);
                continue;
            }

            boolean append = true;
            if (s.equals("(")) {
                if (!partBegin) {
                    groupBegin = true;
                    groupEnd = false;
                    groupResult = new ArrayList<>();
                    append = false;
                }
            } else if (s.equals(")")) {
                if (partBegin) {
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
                                LOGGER.trace(String.format("[%s][condition][add][groupResult]%s", method, sb));
                            }
                            sb = new StringBuilder();
                        }

                        result.add(groupResult);
                        groupResult = null;
                    }
                }
            } else {
                if (partEnd) {
                    switch (s.toLowerCase()) {
                    case "=":
                        valueBegin = true;
                        valueCounter = 0;
                        break;
                    case "\"":
                        if (valueBegin) {
                            valueCounter++;
                            if (valueCounter == 2) {
                                valueEnd = true;
                            }
                        }
                        break;
                    default:
                        boolean isOperator = false;
                        if (valueEnd) {
                            isOperator = true;
                        } else {
                            if (!valueBegin) {
                                isOperator = true;
                            }
                        }

                        if (isOperator) {
                            if (sb.length() > 0) {
                                Condition cp = new Condition(sb.toString().trim());
                                if (groupResult == null) {
                                    result.add(cp);
                                    if (isTraceEnabled) {
                                        LOGGER.trace(String.format("[%s][condition][add][result]%s", method, sb));
                                    }
                                } else {
                                    groupResult.add(cp);
                                    if (isTraceEnabled) {
                                        LOGGER.trace(String.format("[%s][condition][add][groupResult]%s", method, sb));
                                    }
                                }
                                sb = new StringBuilder();
                            }

                            Operator operator = null;
                            switch (s.toLowerCase()) {
                            case "&":
                                operator = Operator.AND;
                                break;
                            case "a":
                                break;
                            case "n":
                                break;
                            case "d":
                                operator = Operator.AND;
                                break;

                            case "|":
                                operator = Operator.OR;
                                break;
                            case "o":
                                break;
                            case "r":
                                operator = Operator.OR;
                                break;
                            default:
                            }

                            if (operator != null) {
                                if (groupResult == null) {
                                    result.add(operator);
                                    if (isTraceEnabled) {
                                        LOGGER.trace(String.format("[%s][operator][add][result]%s", method, sb));
                                    }
                                } else {
                                    groupResult.add(operator);
                                    if (isTraceEnabled) {
                                        LOGGER.trace(String.format("[%s][operator][add][groupResult]%s", method, sb));
                                    }
                                }

                                valueBegin = false;
                                valueEnd = false;
                                valueCounter = 0;
                                partBegin = false;
                                partEnd = false;
                            }
                            if (isTraceEnabled) {
                                LOGGER.trace(String.format("[%s][isOperator][value begin=%s,end=%s,counter=%s]", method, valueBegin, valueEnd,
                                        valueCounter));
                            }
                            append = false;
                        }

                        break;
                    }
                } else {
                    partBegin = true;
                }
            }

            if (append) {
                sb.append(s);
            }

            if (isTraceEnabled) {
                LOGGER.debug(String.format("[%s][group begin=%s,end=%s][part begin=%s,end=%s][value begin=%s,end=%s][%s]%s", method, groupBegin,
                        groupEnd, partBegin, partEnd, valueBegin, valueEnd, s, sb));
            }

            c = c.substring(substrPos);
        }
        if (sb.length() > 0) {
            // TODO check if Operator ???
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
    public static Map<ConditionType, List<Condition>> getByType(List<Object> conditions) {
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
                result.putAll(getByType((List<Object>) o));
            }
        }
        return result;
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
