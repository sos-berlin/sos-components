package com.sos.js7.converter.autosys.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions.Operator;
import com.sos.js7.converter.commons.report.ConverterReport;

public class AutosysReport {

    public static void analyze(List<ACommonJob> standaloneJobs, List<ACommonJob> boxJobs) {
        ConditionAnalyzerResult ar = analyzeStandalone(standaloneJobs);
        ar = analyzeSummaryConditions(analyzeBoxJobs(ar, boxJobs));
        analyzeSummaryLookBack(ar);
    }

    private static ConditionAnalyzerResult analyzeStandalone(List<ACommonJob> jobs) {
        // CONDITIONS
        List<ACommonJob> jobsWithConditions = jobs.stream().filter(j -> j.getCondition().getCondition().getValue() != null && j.getCondition()
                .getCondition().getValue().size() > 0).collect(Collectors.toList());

        ConditionAnalyzerResult analyzerResult = new AutosysReport().new ConditionAnalyzerResult();
        if (jobsWithConditions.size() > 0) {
            for (ACommonJob j : jobsWithConditions) {
                analyzeJobConditions(j, analyzerResult);
            }
        }
        String c = analyzerResult.hasValues() ? ", condition " + analyzerResult.toString() : "";
        ConverterReport.INSTANCE.addAnalyzerRecord("TOTAL STANDALONE", jobs.size() + ", With condition=" + jobsWithConditions.size() + c);

        // JOBS SUMMARY
        ConverterReport.INSTANCE.addSummaryRecord("TOTAL STANDALONE", jobs.size());
        if (jobs.size() > 0) {
            Map<ConverterJobType, List<ACommonJob>> jobsPerType = jobs.stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                    Collectors.toList()));
            String jpt = String.join(",", jobsPerType.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().size()).collect(Collectors
                    .toList()));
            ConverterReport.INSTANCE.addSummaryRecord("", jpt);
        }
        return analyzerResult;
    }

    private static ConditionAnalyzerResult analyzeBoxJobs(ConditionAnalyzerResult analyzerResult, List<ACommonJob> boxJobs) {
        // CONDITIONS
        int counterChildren = 0;
        int counterChildrenWithDateConditions = 0;
        RunTimeAnalyzerResult rtResult = new AutosysReport().new RunTimeAnalyzerResult();
        Map<Integer, Integer> mapByChildSize = new HashMap<>();
        Map<String, Integer> mapByBoxName = new HashMap<>();
        Map<String, Set<String>> mapByChildAgents = new HashMap<>();
        Map<String, List<String>> mapByChildType = new HashMap<>();
        for (ACommonJob aBoxJob : boxJobs) {
            JobBOX boxJob = (JobBOX) aBoxJob;

            int size = boxJob.getJobs().size();
            counterChildren += size;

            // mapByChildSize
            int counterByChildSize = 0;
            if (mapByChildSize.containsKey(size)) {
                counterByChildSize = mapByChildSize.get(size);
            }
            counterByChildSize++;
            mapByChildSize.put(size, counterByChildSize);

            // mapByBoxName
            int counterByBoxName = 0;
            if (mapByBoxName.containsKey(boxJob.getInsertJob().getValue())) {
                counterByBoxName = mapByBoxName.get(boxJob.getInsertJob().getValue());
            }
            counterByBoxName++;
            mapByBoxName.put(boxJob.getInsertJob().getValue(), counterByBoxName);

            // mapByChildAgents
            Set<String> set = boxJob.getJobs().stream().filter(e -> e instanceof ACommonMachineJob).map(e -> ((ACommonMachineJob) e).getMachine()
                    .getValue()).distinct().collect(Collectors.toSet());
            mapByChildAgents.put(boxJob.getInsertJob().getValue(), set);

            // mapByChildType
            List<String> l = boxJob.getJobs().stream().map(e -> e.getConverterJobType().name()).collect(Collectors.toList());
            mapByChildType.put(boxJob.getInsertJob().getValue(), l);

            // conditions
            List<ACommonJob> jobsWithConditions = boxJob.getJobs().stream().filter(j -> j.getCondition().getCondition().getValue() != null && j
                    .getCondition().getCondition().getValue().size() > 0).collect(Collectors.toList());
            List<Object> boxJobConditions = boxJob.getCondition().getCondition().getValue();

            List<ACommonJob> childrenWithDateConditions = boxJob.getJobs().stream().filter(j -> j.getRunTime().getDateConditions().getValue() != null
                    && j.getRunTime().getDateConditions().getValue()).collect(Collectors.toList());
            for (ACommonJob cj : childrenWithDateConditions) {
                if (cj.getRunTime().getDaysOfWeek().getValue() != null && cj.getRunTime().getDaysOfWeek().getValue().getDays().size() > 0) {
                    rtResult.daysOfWeek++;
                }
                if (cj.getRunTime().getRunCalendar().getValue() != null) {
                    rtResult.runCalendar++;
                }
                if (cj.getRunTime().getRunWindow().getValue() != null) {
                    rtResult.runWindow++;
                }
                if (cj.getRunTime().getStartMins().getValue() != null && cj.getRunTime().getStartMins().getValue().size() > 0) {
                    rtResult.startMins++;
                }
                if (cj.getRunTime().getStartTimes().getValue() != null && cj.getRunTime().getStartTimes().getValue().size() > 0) {
                    rtResult.startTimes++;
                }
                if (cj.getRunTime().getTimezone().getValue() != null) {
                    rtResult.timezone++;
                }
            }

            String name = String.format("BOX JOB=%s", boxJob.getInsertJob().getValue());
            String value = String.format("%s condition, Child jobs: size=%s, with condition=%s, with runtime=%s", (boxJobConditions == null
                    ? " Without" : "With"), size, jobsWithConditions.size(), childrenWithDateConditions.size());
            ConverterReport.INSTANCE.addAnalyzerRecord(name, value);
            if (size == 0) {
                ConverterReport.INSTANCE.addWarningRecord(name, "Child jobs=0");
            }

            counterChildrenWithDateConditions += childrenWithDateConditions.size();
            for (ACommonJob j : childrenWithDateConditions) {
                ConverterReport.INSTANCE.addAnalyzerRecord("   " + j.getConverterJobType() + " " + j.getInsertJob().getValue(), String.format(
                        "[runtime]%s", j.getRunTime().toString()));
            }

            if (boxJobConditions != null) {
                analyzeJobConditions(aBoxJob, analyzerResult);
            }

            if (jobsWithConditions.size() > 0) {
                for (ACommonJob j : jobsWithConditions) {
                    analyzeJobConditions(j, analyzerResult);
                }
            }
        }

        String c = "";
        if (counterChildrenWithDateConditions > 0) {
            c = ", Child jobs with date_conditions=" + counterChildrenWithDateConditions;
            c += "(Run-Times=" + rtResult + ")";
        }
        if (boxJobs.size() > 0) {
            int wcj = mapByChildSize.get(0) == null ? 0 : mapByChildSize.get(0);
            ConverterReport.INSTANCE.addAnalyzerRecord("TOTAL BOX", boxJobs.size() + "(without Child jobs=" + wcj + "), Child jobs=" + counterChildren
                    + c);
        } else {
            ConverterReport.INSTANCE.addAnalyzerRecord("TOTAL BOX", boxJobs.size() + "");

        }
        // JOBS SUMMARY
        ConverterReport.INSTANCE.addSummaryRecord("TOTAL BOX", boxJobs.size());
        if (mapByChildAgents.size() > 0) {
            ConverterReport.INSTANCE.addSummaryRecord("Box/Agents", mapByChildAgents(mapByChildAgents));
        }
        if (mapByChildType.size() > 0) {
            ConverterReport.INSTANCE.addSummaryRecord("Job type/Box", mapByChildType(mapByChildType));
        }
        if (mapByChildSize.size() > 0) {
            ConverterReport.INSTANCE.addSummaryRecord("Box/Jobs", "TOTAL Jobs=" + counterChildren + "(" + intIntmap2String(mapByChildSize) + ")");
        }
        mapByBoxName.entrySet().forEach(e -> {
            if (e.getValue() > 1) {
                ConverterReport.INSTANCE.addSummaryRecord("BOX=" + e.getKey(), e.getValue() + " times");
            }
        });

        return analyzerResult;
    }

    public static String intIntmap2String(Map<Integer, Integer> map) {
        return String.join(",", map.entrySet().stream().sorted((s1, s2) -> s1.getValue().compareTo(s2.getValue())).map(e -> e.getValue() + "=" + e
                .getKey()).collect(Collectors.toList()));
    }

    public static String strIntMap2String(Map<String, Integer> map) {
        return String.join(",", map.entrySet().stream().sorted((s1, s2) -> s1.getValue().compareTo(s2.getValue())).map(e -> e.getValue() + "=" + e
                .getKey()).collect(Collectors.toList()));
    }

    private static String mapByChildAgents(Map<String, Set<String>> mapByChildAgents) {
        Map<Integer, Integer> map = new HashMap<>();
        mapByChildAgents.entrySet().forEach(e -> {
            // ConverterReport.INSTANCE.addSummaryRecord("--" + e.getKey(), e.getValue().size());

            int counter = 0;
            int size = e.getValue().size();
            if (map.containsKey(size)) {
                counter = map.get(size);
            }
            counter++;
            map.put(size, counter);
        });
        return String.join(",", map.entrySet().stream().sorted((s1, s2) -> s1.getValue().compareTo(s2.getValue())).map(e -> e.getValue() + "=" + e
                .getKey()).collect(Collectors.toList()));
    }

    private static String mapByChildType(Map<String, List<String>> mapByChildType) {
        Map<String, Integer> map = new HashMap<>();

        for (Map.Entry<String, List<String>> e : mapByChildType.entrySet()) {
            int counter = 0;
            for (String t : e.getValue()) {
                if (map.containsKey(t)) {
                    counter = map.get(t);
                }
                counter++;
                map.put(t, counter);
            }
        }
        return String.join(",", map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList()));
    }

    // TODO recursive (if group)
    private static void analyzeJobConditions(ACommonJob j, ConditionAnalyzerResult analyzerResult) {
        List<Object> c = j.getCondition().getCondition().getValue();
        for (Object o : c) {
            String name = j.getConverterJobType() + " " + j.getInsertJob().getValue();
            if (o instanceof Condition) {
                Condition cn = (Condition) o;
                switch (cn.getType()) {
                case GLOBAL_VARIABLE:
                    analyzerResult.global++;
                    break;
                case SUCCESS:
                    analyzerResult.success++;
                    break;
                case FAILURE:
                    analyzerResult.failed++;
                    break;
                case DONE:
                    analyzerResult.done++;
                    break;
                case NOTRUNNING:
                    analyzerResult.notrunning++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][TYPE=%s]%s", cn.getType(), j.getCondition()
                            .getOriginalCondition()));
                    break;
                case TERMINATED:
                    analyzerResult.terminated++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][TYPE=%s]%s", cn.getType(), j.getCondition()
                            .getOriginalCondition()));
                    break;
                case EXITCODE:
                    analyzerResult.exitcode++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][TYPE=%s]%s", cn.getType(), j.getCondition()
                            .getOriginalCondition()));
                    break;
                case SOS_UNKNOWN:
                    analyzerResult.unknown++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][TYPE=%s]%s", cn.getType(), j.getCondition()
                            .getOriginalCondition()));
                    break;
                }

                if (cn.getLookBack() != null) {
                    analyzerResult.lookBack++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][with lookBack=%s]%s", cn.getLookBack(), j
                            .getCondition().getOriginalCondition()));

                    Map<String, Integer> lookBacks = new HashMap<>();
                    if (analyzerResult.lookBackJobs.containsKey(cn.getName())) {
                        lookBacks = analyzerResult.lookBackJobs.get(cn.getName());
                    }
                    if (lookBacks.containsKey(cn.getLookBack())) {
                        lookBacks.put(cn.getLookBack(), lookBacks.get(cn.getLookBack()) + 1);
                    } else {
                        lookBacks.put(cn.getLookBack(), 1);
                    }
                    analyzerResult.lookBackJobs.put(cn.getName(), lookBacks);
                } else {
                    if (analyzerResult.jobsWithoutLookBack.containsKey(cn.getName())) {
                        analyzerResult.jobsWithoutLookBack.put(cn.getName(), analyzerResult.jobsWithoutLookBack.get(cn.getName()) + 1);
                    } else {
                        analyzerResult.jobsWithoutLookBack.put(cn.getName(), 1);
                    }
                }

                if (cn.getExternalInstance() != null) {
                    analyzerResult.external++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][with external=%s]%s", cn
                            .getExternalInstance(), j.getCondition().getOriginalCondition()));
                }

            } else if (o instanceof Operator) {
                Operator op = (Operator) o;
                if (op.equals(Operator.OR)) {
                    analyzerResult.orOperator++;
                    ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][OPERATOR=OR]%s", j.getCondition()
                            .getOriginalCondition()));
                }
            } else if (o instanceof List) {
                analyzerResult.group++;
                ConverterReport.INSTANCE.addAnalyzerRecord("    " + name, String.format("[condition][GROUP]%s", j.getCondition()
                        .getOriginalCondition()));
            }
        }
    }

    private static ConditionAnalyzerResult analyzeSummaryLookBack(ConditionAnalyzerResult analyzerResult) {
        if (analyzerResult.lookBackJobs.size() > 0) {
            Map<String, Map<String, Integer>> multipleLookBack = analyzerResult.lookBackJobs.entrySet().stream().filter(e -> e.getValue().size() > 1)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            multipleLookBack.entrySet().forEach(e -> {
                ConverterReport.INSTANCE.addAnalyzerRecord("SUMMARY MULTIPLE USED LOOKBACK Jobs", e.getKey());
                if (analyzerResult.jobsWithoutLookBack.containsKey(e.getKey())) {
                    ConverterReport.INSTANCE.addAnalyzerRecord("", "without lookBack = " + analyzerResult.jobsWithoutLookBack.get(e.getKey())
                            + " times");
                }
                e.getValue().entrySet().forEach(ee -> {
                    ConverterReport.INSTANCE.addAnalyzerRecord("", ee.getKey() + " = " + ee.getValue() + " times");
                });
            });
        }
        return analyzerResult;
    }

    private static ConditionAnalyzerResult analyzeSummaryConditions(ConditionAnalyzerResult analyzerResult) {
        if (analyzerResult.hasValues()) {
            ConverterReport.INSTANCE.addAnalyzerRecord("SUMMARY CONDITIONS", analyzerResult.toString());
        }
        return analyzerResult;
    }

    private class ConditionAnalyzerResult {

        private int global = 0;
        private int success = 0;
        private int failed = 0;
        private int done = 0;
        private int notrunning = 0;
        private int terminated = 0;
        private int exitcode = 0;
        private int unknown = 0;

        private int external = 0;
        private int orOperator = 0;
        private int group = 0;
        private int lookBack = 0;

        private Map<String, Map<String, Integer>> lookBackJobs = new HashMap<>();
        private Map<String, Integer> jobsWithoutLookBack = new HashMap<>();

        @Override
        public String toString() {
            List<String> l = new ArrayList<>();
            if (global > 0) {
                l.add("GLOBAL VARIABLES=" + global);
            }
            if (success > 0) {
                l.add("SUCCESS=" + success);
            }
            if (failed > 0) {
                l.add("FAILED=" + failed);
            }
            if (done > 0) {
                l.add("DONE=" + done);
            }
            if (notrunning > 0) {
                l.add("NOTRUNNING=" + notrunning);
            }
            if (terminated > 0) {
                l.add("TERMINATED=" + terminated);
            }
            if (exitcode > 0) {
                l.add("EXITCODE=" + exitcode);
            }
            if (unknown > 0) {
                l.add("SOS UNKNOWN=" + unknown);
            }

            if (lookBack > 0) {
                l.add("LOOKBACK=" + lookBack);
            }
            if (external > 0) {
                l.add("EXTERNAL=" + external);
            }
            if (orOperator > 0) {
                l.add("OR OPERATOR=" + orOperator);
            }
            if (group > 0) {
                l.add("GROUP=" + group);
            }
            return String.join(",", l);
        }

        // TODO reflection
        private boolean hasValues() {
            return global > 0 || success > 0 || failed > 0 || done > 0 || notrunning > 0 || terminated > 0 || exitcode > 0 || unknown > 0
                    || lookBack > 0 || external > 0 || orOperator > 0 || group > 0;
        }
    }

    private class RunTimeAnalyzerResult {

        int daysOfWeek = 0;
        int runCalendar = 0;
        int runWindow = 0;
        int startMins = 0;
        int startTimes = 0;
        int timezone = 0;

        @Override
        public String toString() {
            List<String> l = new ArrayList<>();
            if (daysOfWeek > 0) {
                l.add("daysOfWeek=" + daysOfWeek);
            }
            if (runCalendar > 0) {
                l.add("runCalendar=" + runCalendar);
            }
            if (runWindow > 0) {
                l.add("runWindow=" + runWindow);
            }
            if (startMins > 0) {
                l.add("startMins=" + startMins);
            }
            if (startTimes > 0) {
                l.add("startTimes=" + startTimes);
            }
            if (timezone > 0) {
                l.add("timezone=" + timezone);
            }
            return String.join(",", l);
        }
    }

}
