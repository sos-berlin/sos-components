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
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.autosys.output.js7.helper.bean.Condition2ConsumeNotice;
import com.sos.js7.converter.autosys.output.js7.helper.bean.Condition2ConsumeNotice.Condition2ConsumeNoticeType;
import com.sos.js7.converter.autosys.output.js7.helper.bean.Job2Condition;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);

    public static final Map<Condition, Path> JS7_BOARDS = new HashMap<>();
    public static final Set<Condition2ConsumeNotice> JS7_CONSUME_NOTICES = AutosysConverterHelper.newContition2ConsumeNoticeTreeSet();

    // without space at end
    private static final String JS7_AND = " " + JS7ConverterHelper.JS7_NOTICE_AND;
    private static final String JS7_OR = " " + JS7ConverterHelper.JS7_NOTICE_OR;

    // create a ConsumeNotive if condition job is a cyclic job
    // was true before 2024-10-31 ...
    private static boolean CYCLIC_TO_COMSUME = false;

    public static void clear() {
        JS7_BOARDS.clear();
        JS7_CONSUME_NOTICES.clear();
    }

    private static BoardExpectConsumHelper noticesAsString(AutosysAnalyzer analyzer, ACommonJob j) {
        if (j.hasCondition()) {
            List<Condition> conditions = new ArrayList<>();
            StringBuilder expect = new StringBuilder();
            StringBuilder consume = new StringBuilder();
            toJS7(analyzer, j, j.getCondition().getCondition().getValue(), conditions, expect, consume);

            BoardExpectConsumHelper h = new BoardExpectConsumHelper();
            h.setConditions(conditions);
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

        return JS7ConverterHelper.getJS7InventoryObjectTitle(sb.toString());
    }

    @SuppressWarnings("unchecked")
    // TODO
    private static void toJS7(AutosysAnalyzer analyzer, ACommonJob currentJob, List<Object> l, List<Condition> conditions, StringBuilder expect,
            StringBuilder consume) {
        for (Object o : l) {
            if (o instanceof Condition) {
                Condition c = (Condition) o;

                Condition2ConsumeNotice consumeBean = null;
                if (CYCLIC_TO_COMSUME && c.getJobName() != null) {
                    ACommonJob j = analyzer.getAllJobs().get(c.getJobName());
                    if (j != null) {
                        if (j.getRunTime().isCyclic()) {
                            consumeBean = new Condition2ConsumeNotice(c, Condition2ConsumeNoticeType.CYCLIC);
                        }
                    }
                }

                String name = getBoardName(analyzer, currentJob, new Job2Condition(null, c));
                if (name != null) {
                    if (consumeBean != null) {
                        // TODO currently only AND
                        if (consume.length() > 0) {
                            consume.append(JS7_AND).append(" ");
                        }

                        consume.append(quote(name));
                        if (!JS7_CONSUME_NOTICES.contains(consumeBean)) {
                            JS7_CONSUME_NOTICES.add(consumeBean);
                        }
                    } else {
                        expect.append(quote(name));
                    }
                    conditions.add(c);
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
                toJS7(analyzer, currentJob, (List<Object>) o, conditions, expect, consume);
            }
        }
    }

    private static String getBoardName(AutosysAnalyzer analyzer, ACommonJob currentJob, Job2Condition j2c) {

        String js7Name = JS7ConverterHelper.getJS7ObjectName(j2c.getCondition().getKey());

        Path boardPath = JS7_BOARDS.get(j2c.getCondition());
        if (boardPath == null || currentJob.isBoxChildJob()) {
            if (j2c.getCondition().getJobName() == null) {
                JS7_BOARDS.put(j2c.getCondition(), Paths.get(js7Name));
            } else {
                ACommonJob j = analyzer.getAllJobs().get(j2c.getCondition().getJobName());
                if (j == null) {
                    if (Autosys2JS7Converter.NOT_CREATE_NOTICES_IF_JOB_NOT_FOUND) {
                        LOGGER.info("IGNORED BECAUSE JOB NOT FOUND=" + j2c.getCondition());
                        js7Name = null;
                    } else {
                        JS7_BOARDS.put(j2c.getCondition(), Paths.get(js7Name));
                    }
                } else {
                    if (currentJob.isBoxChildJob() && currentJob.getBoxName().equals(j.getBoxName())) {
                        if (j2c.getJob() == null || currentJob.getBoxName().equals(j2c.getJob().getBoxName())) {
                            js7Name = null;
                        } else {
                            JS7_BOARDS.put(j2c.getCondition(), PathResolver.getJS7ParentPath(j, js7Name).resolve(js7Name));
                        }
                    } else {
                        JS7_BOARDS.put(j2c.getCondition(), PathResolver.getJS7ParentPath(j, js7Name).resolve(js7Name));
                    }
                }
            }
        }
        return js7Name;
    }

    private static String quote(String val) {
        return "'" + val + "'";
    }

    @SuppressWarnings("unused")
    private static Map<ConditionType, Set<Condition>> getDistinctOutConditionsByType(ACommonJob j, ConditionAnalyzer a) {
        Set<Condition> s = getDistinctOutConditions(j, a);
        if (s == null) {
            return null;
        }

        return s.stream().collect(Collectors.groupingBy(Condition::getType, Collectors.toSet()));
    }

    private static Set<Condition> getDistinctOutConditions(ACommonJob j, ConditionAnalyzer a) {
        OutConditionHolder h = a.getJobOUTConditions(j);
        if (h == null) {
            return null;
        }

        LOGGER.info("D=" + h);

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

    public static PostNotices newPostNotices(AutosysAnalyzer analyzer, ACommonJob currentJob, Set<Job2Condition> outContitions) {
        if (outContitions == null || outContitions.size() == 0) {
            return null;
        }
        List<String> l = new ArrayList<>();
        for (Job2Condition c : outContitions) {
            String n = getBoardName(analyzer, currentJob, c);
            if (n == null) {
                continue;
            }

            if (!l.contains(n)) {
                l.add(n);
            }
        }
        if (l.size() > 0) {
            l.sort((e1, e2) -> e1.compareTo(e2));
            return new PostNotices(l);
        }
        return null;
    }

    public static Integer getLifeTimeInMinutes(Condition c) {
        // null, because a default value 24h will be used - see JS7ConverterHelper.createNoticeBoard
        if (c == null) {
            return null;
        }
        Integer l = c.getLookBackAsMinutes();
        // l.intValue() < 0 - lockBack=0
        // l.intValue() > 24 * 60 - more as 24h
        if (l == null || l.intValue() <= 0 || l.intValue() > 24 * 60) {
            return null;
        }
        return l;
    }

}
