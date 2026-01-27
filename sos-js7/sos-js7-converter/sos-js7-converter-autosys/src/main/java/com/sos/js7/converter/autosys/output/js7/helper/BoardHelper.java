package com.sos.js7.converter.autosys.output.js7.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions.Operator;
import com.sos.js7.converter.autosys.config.items.AutosysOutputConfig.CrossInstanceCondition;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.input.analyzer.ConditionAnalyzer;
import com.sos.js7.converter.autosys.input.analyzer.ConditionAnalyzer.OutConditionHolder;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.autosys.output.js7.helper.beans.BoardCrossInstance;
import com.sos.js7.converter.autosys.output.js7.helper.beans.Job2Condition;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class BoardHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardHelper.class);

    public static final Map<Condition, Path> JS7_BOARDS = new HashMap<>();
    public static final Set<Condition> JS7_CONSUME_NOTICES = AutosysConverterHelper.newContitionsTreeSet();

    // without space at end
    private static final String JS7_AND = " " + JS7ConverterHelper.JS7_NOTICE_AND;
    private static final String JS7_OR = " " + JS7ConverterHelper.JS7_NOTICE_OR;

    // create a ConsumeNotice if condition job is a cyclic job
    // was true before 2024-10-31 ...
    private static final boolean CYCLIC_TO_COMSUME = false;

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
        val = val.replace("()", "");
        val = val.replace("( && )", "");
        val = val.replace("( || )", "");

        val = val.replace("( || '", "('");
        val = val.replace("' || )", "')");
        val = val.replace("( && '", "('");
        val = val.replace("' && )", "')");

        val = val.trim();
        if (SOSString.isEmpty(val)) {
            return null;
        }

        return JS7ConverterHelper.parentheses(SOSString.trim(val, JS7ConverterHelper.JS7_NOTICE_AND, JS7ConverterHelper.JS7_NOTICE_OR));
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
        case JS7_INTERNAL:
            sb = new StringBuilder(c.getName());
            break;
        case EXITCODE:
        case TERMINATED:
        case NOTRUNNING:
        case JS7_UNKNOWN:
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
                if (c.isNotrunning()) {
                    if (c.getJobName() != null) {
                        ACommonJob j = analyzer.getAllJobs().get(c.getJobName());
                        if (j != null) {
                            currentJob.addExclusiveResourcePaarIfNotExists(j);
                        }
                    }
                } else {
                    Condition consumeCondition = null;
                    if (CYCLIC_TO_COMSUME && c.getJobName() != null) {
                        ACommonJob j = analyzer.getAllJobs().get(c.getJobName());
                        if (j != null) {
                            if (j.getRunTime().isCyclic()) {
                                consumeCondition = c;
                            }
                        }
                    }

                    String name = getBoardName(analyzer, currentJob, new Job2Condition(null, c));
                    if (name != null) {
                        if (consumeCondition != null) {
                            // TODO currently only AND
                            if (consume.length() > 0) {
                                consume.append(JS7_AND).append(" ");
                            }

                            consume.append(quote(name));
                            if (!JS7_CONSUME_NOTICES.contains(consumeCondition)) {
                                JS7_CONSUME_NOTICES.add(consumeCondition);
                            }
                        } else {
                            expect.append(quote(name));
                        }
                        conditions.add(c);
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
                expect.append("(");
                toJS7(analyzer, currentJob, (List<Object>) o, conditions, expect, consume);
                expect.append(")");
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
                    BoardCrossInstance bci = tryCreateDummyIfCrossInstance(analyzer, j2c.getCondition());
                    if (bci != null) {
                        j = bci.getJob();
                        js7Name = bci.getJS7BoardName();
                    }
                }

                if (j == null) {
                    if (Autosys2JS7Converter.NOT_CREATE_NOTICES_IF_JOB_NOT_FOUND) {
                        LOGGER.info("[Condition(Ignored)][because job not found][current job=" + currentJob.getName() + "][ignored condition=" + j2c
                                .getCondition() + "]job not found=" + j2c.getCondition().getJobName());
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

    private static BoardCrossInstance tryCreateDummyIfCrossInstance(AutosysAnalyzer analyzer, Condition condition) {
        if (condition.getInstanceTag() == null) {
            return null;
        }
        CrossInstanceCondition config = Autosys2JS7Converter.CONFIG.getAutosys().getOutputConfig().getCrossInstanceCondition();
        if (config.isIgnore()) {
            return null;
        }
        condition.setDummy();

        ACommonJob j = analyzer.getAllJobs().get(condition.getJobName());
        if (j == null) {
            j = new JobCMD(null, true);
            j.setInsertJob(condition.getJobName());
            analyzer.getAllJobs().put(condition.getJobName(), j);
        }
        String js7Name = config.isMapToLocal() ? JS7ConverterHelper.getJS7ObjectName(condition.getKeyWithoutInstance()) : JS7ConverterHelper
                .getJS7ObjectName(condition.getKey());
        return new BoardCrossInstance(j, js7Name);
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

    public static PostNotices mergePostNoticesSecondAsFirst(PostNotices current, PostNotices second) {
        if (second == null) {
            return current;
        }

        // merge without duplicates
        // Set<String> mergedSet = new LinkedHashSet<>(tpn.getNoticeBoardNames());
        // mergedSet.addAll(postNoticeToBoxSelf.getNoticeBoardNames());
        Set<String> mergedSet = new LinkedHashSet<>(second.getNoticeBoardNames());
        mergedSet.addAll(current.getNoticeBoardNames());

        current.setNoticeBoardNames(new ArrayList<>(mergedSet));
        return current;
    }

}
