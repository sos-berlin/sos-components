package com.sos.js7.converter.autosys.output.js7.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions.Operator;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.input.analyzer.ConditionAnalyzer;
import com.sos.js7.converter.autosys.input.analyzer.ConditionAnalyzer.OutConditionHolder;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);

    // without space at end
    private static final String JS7_AND = " " + JS7ConverterHelper.JS7_NOTICE_AND;
    private static final String JS7_OR = " " + JS7ConverterHelper.JS7_NOTICE_OR;

    private static final boolean NOT_CREATE_NOTICES_IF_JOB_NOT_FOUND = true;

    public static final Map<Condition, Path> JS7_BOARDS = new HashMap<>();
    public static final Set<Condition> JS7_CONSUME_NOTICES = new HashSet<>();

    public static void clear() {
        JS7_BOARDS.clear();
        JS7_CONSUME_NOTICES.clear();
    }

    private static BoardExpectConsumHelper noticesAsString(AutosysAnalyzer analyzer, ACommonJob j) {
        if (j.hasCondition()) {
            StringBuilder expect = new StringBuilder();
            StringBuilder consume = new StringBuilder();
            toJS7(analyzer, j.getCondition().getCondition().getValue(), expect, consume);

            BoardExpectConsumHelper h = new BoardExpectConsumHelper();
            h.setExpectNotices(getNotices(expect));
            h.setConsumeNotices(getNotices(consume));

            return h.isNotEmpty() ? h : null;
        }
        return null;
    }

    private static String getNotices(StringBuilder input) {
        String val = input.toString().trim();
        if (SOSString.isEmpty(val)) {
            return null;
        }
        return JS7ConverterHelper.parentheses(SOSString.trim(val.toString(), JS7ConverterHelper.JS7_NOTICE_AND, JS7ConverterHelper.JS7_NOTICE_OR));
    }

    public static BoardExpectConsumHelper expectNotices(AutosysAnalyzer analyzer, ACommonJob j) {
        BoardExpectConsumHelper h = noticesAsString(analyzer, j);
        if (h == null || !h.isNotEmpty()) {
            return null;
        }
        return h;
    }

    public static String getBoardTitle(Condition c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Expect notice for ");

        switch (c.getType()) {
        case DONE:
            if (c.getJobName() != null) {
                sb.append("done job: ").append(c.getJobName());
            } else {
                sb.append(c.toString());
            }
            break;
        case FAILURE:
            if (c.getJobName() != null) {
                sb.append("failed job: ").append(c.getJobName());
            } else {
                sb.append(c.toString());
            }
            break;
        case SUCCESS:
            if (c.getJobName() != null) {
                sb.append("successful job: ").append(c.getJobName());
            } else {
                sb.append(c.toString());
            }
            break;
        case VARIABLE:
            sb.append("variable: " + c.getName());
            break;
        case EXITCODE:
        case TERMINATED:
        case NOTRUNNING:
        case SOS_UNKNOWN:
        default:
            sb.append(c.toString());
            break;
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    // TODO
    private static void toJS7(AutosysAnalyzer analyzer, List<Object> l, StringBuilder expect, StringBuilder consume) {
        for (Object o : l) {
            if (o instanceof Condition) {
                Condition c = (Condition) o;

                boolean isConsume = false;
                if (c.getJobName() != null) {
                    ACommonJob j = analyzer.getAllJobs().get(c.getJobName());
                    if (j != null) {
                        if (j.getRunTime().isCyclic()) {
                            isConsume = true;
                        }
                    }
                }

                String name = getBoardName(analyzer, c);
                if (name != null) {
                    if (isConsume) {
                        consume.append(quote(name));
                        if (!JS7_CONSUME_NOTICES.contains(c)) {
                            JS7_CONSUME_NOTICES.add(c);
                        }
                    } else {
                        expect.append(quote(name));
                    }
                }
            } else if (o instanceof Operator) {
                String e = expect.toString();
                if (e.endsWith(JS7_AND + " ") || e.endsWith(JS7_OR + " ")) {

                } else {
                    Operator op = (Operator) o;
                    switch (op) {
                    case AND:
                        expect.append(JS7_AND);
                        break;
                    case OR:
                        expect.append(JS7_OR);
                        break;
                    default:
                    }
                    expect.append(" ");
                }
            } else if (o instanceof List) {
                toJS7(analyzer, (List<Object>) o, expect, consume);
            }
        }
    }

    private static String getBoardName(AutosysAnalyzer analyzer, Condition c) {

        String js7Name = JS7ConverterHelper.getJS7ObjectName(c.getKey());

        Path boardPath = JS7_BOARDS.get(c);
        if (boardPath == null) {
            if (c.getJobName() == null) {
                JS7_BOARDS.put(c, Paths.get(js7Name));
            } else {
                ACommonJob j = analyzer.getAllJobs().get(c.getJobName());
                if (j == null) {
                    if (NOT_CREATE_NOTICES_IF_JOB_NOT_FOUND) {
                        LOGGER.info("IGNORED=" + c);
                        js7Name = null;
                    } else {
                        JS7_BOARDS.put(c, Paths.get(js7Name));
                    }
                } else {
                    JS7_BOARDS.put(c, PathResolver.getJS7ParentPath(j, js7Name).resolve(js7Name));
                }
            }
        }

        return js7Name;
    }

    private static String quote(String val) {
        return "'" + val + "'";
    }

    public static Map<ConditionType, Set<Condition>> getDistinctOutConditionsByType(ACommonJob j, ConditionAnalyzer a) {
        Set<Condition> s = getDistinctOutConditions(j, a);
        if (s == null) {
            return null;
        }

        return s.stream().collect(Collectors.groupingBy(Condition::getType, Collectors.toSet()));
    }

    public static Set<Condition> getDistinctOutConditions(ACommonJob j, ConditionAnalyzer a) {
        OutConditionHolder h = a.getJobOUTConditions(j);
        if (h == null) {
            return null;
        }

        Set<Condition> s = new HashSet<>();
        for (Map.Entry<String, Map<String, Condition>> me : h.getJobConditions().entrySet()) {
            for (Map.Entry<String, Condition> e : me.getValue().entrySet()) {
                if (!s.contains(e.getValue())) {
                    s.add(e.getValue());
                }
            }
        }
        return s;
    }

    public static PostNotices newPostNotices(AutosysAnalyzer analyzer, Set<Condition> outContitions) {
        if (outContitions == null || outContitions.size() == 0) {
            return null;
        }
        List<String> l = new ArrayList<>();
        for (Condition c : outContitions) {
            String n = getBoardName(analyzer, c);
            if (n == null) {
                continue;
            }
            if (!l.contains(n)) {
                l.add(n);
            }
        }

        return new PostNotices(l);
    }

}
