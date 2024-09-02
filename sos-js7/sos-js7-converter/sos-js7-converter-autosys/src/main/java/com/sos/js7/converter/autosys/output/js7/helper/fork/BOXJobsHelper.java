package com.sos.js7.converter.autosys.output.js7.helper.fork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.output.js7.helper.ConverterBOXJobs;

public class BOXJobsHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BOXJobsHelper.class);

    public static Map<String, BOXJobHelper> CLOSING_BOX_JOB_HELPERS = new HashMap<>();

    public static void clear() {
        CLOSING_BOX_JOB_HELPERS.clear();
    }

    public static List<BOXJobHelper> getFirstLevelChildren(JobBOX box) {
        List<ACommonJob> children = new ArrayList<>(box.getJobs());
        if (children.size() < 2) {
            return toBOXJobHelper(box, children);
        }
        boolean doLog = false;// box.getName().equals("b2bm.mdm_b_x_itunes_batch");

        List<ACommonJob> withoutConditions = children.stream().filter(e -> !e.hasCondition()).collect(Collectors.toList());
        List<ACommonJob> result = new ArrayList<>(withoutConditions);
        children.removeAll(withoutConditions);

        if (doLog) {
            // LOGGER.info("result=" + result);
        }

        List<ACommonJob> withNotThisBoxConditions = new ArrayList<>();
        for (ACommonJob j : children) {
            if (doLog) {
                LOGGER.info("-----" + j.getName());
            }

            List<Condition> conditions = j.conditionsAsList();
            long count = 0;
            for (Condition c : conditions) {
                if (c.getType().equals(ConditionType.VARIABLE)) {
                    continue;
                }
                // TODO -??? remove if supported
                if (c.getType().equals(ConditionType.NOTRUNNING) || c.getType().equals(ConditionType.TERMINATED) || c.getType().equals(
                        ConditionType.EXITCODE)) {
                    // continue;
                }
                if (doLog) {
                    LOGGER.info("JJJJ1[" + j.getName() + "]=" + c.getJobName());
                }
                count += box.getJobs().stream().filter(cj -> cj.isNameEquals(c)).count();

                if (doLog) {
                    for (ACommonJob x : box.getJobs()) {
                        LOGGER.info("    " + x.getName() + "=" + c.getJobName());
                    }
                }
            }

            if (doLog) {
                LOGGER.info("JJJJ2[" + j.getName() + "]=" + count);
            }

            if (count == 0) {
                // LOGGER.info("[getFirstForkChildren][add][notThisBoxCondition]" + j.getName());
                withNotThisBoxConditions.add(j);
            }
        }
        result.addAll(withNotThisBoxConditions);
        return toBOXJobHelper(box, result);
    }

    private static List<BOXJobHelper> toBOXJobHelper(JobBOX box, List<ACommonJob> jobs) {
        return jobs.stream().map(j -> new BOXJobHelper(box, j, null)).collect(Collectors.toList());
    }

    public static List<ACommonJob> getJobChildrenXXX(JobBOX box, ACommonJob currentJob) {
        return box.getJobs().stream().filter(j -> {
            if (j.getName().equalsIgnoreCase(currentJob.getName())) {
                return false;
            } else {
                return j.conditionsAsList().stream().anyMatch(c -> c.getJobName() != null && currentJob.isNameEquals(c));
            }
        }).collect(Collectors.toList());
    }

    private static List<ACommonJob> getJobs(JobBOX box, String jobName) {
        return box.getJobs().stream().filter(j -> j.getName().equals(jobName)).collect(Collectors.toList());
    }

    private static Map<Condition, List<ACommonJob>> getThisBoxConditions(JobBOX box, ACommonJob j) {
        return j.conditionsAsList().stream().filter(c -> c.getJobName() != null && getJobs(box, c.getJobName()).size() > 0).collect(Collectors.toMap(
                c -> c, c -> getJobs(box, c.getJobName())));
    }

    public static BranchHelper getJobChildren(JobBOX box, ACommonJob currentJob) {
        List<BOXJobHelper> children = new ArrayList<>();
        List<BOXJobHelper> closing = new ArrayList<>();

        boolean doLog = box.getName().equals("ctba.ctb_p_c_edi_split_calc_ess_box");
        if (doLog) {
            LOGGER.info("currentJob=" + currentJob);
        }
        for (ACommonJob j : box.getJobs()) {
            if (j.getName().equals(currentJob.getName())) {
                continue;
            }

            Map<Condition, List<ACommonJob>> cl = getThisBoxConditions(box, j);
            if (cl.entrySet().stream().anyMatch(c -> currentJob.isNameEquals(c.getKey()))) {
                if (cl.size() == 1) {
                    children.add(new BOXJobHelper(box, j, null));
                } else {
                    String key = j.getName();
                    if (!CLOSING_BOX_JOB_HELPERS.containsKey(key)) {
                        CLOSING_BOX_JOB_HELPERS.put(key, new BOXJobHelper(box, j, cl));
                    }
                    if (doLog) {
                        LOGGER.info("               cl=" + key);
                    }
                    closing.add(CLOSING_BOX_JOB_HELPERS.get(key));
                }
            }
        }
        if (doLog) {
            LOGGER.info("                " + children);
        }
        return new BranchHelper(children, closing);
    }

}
