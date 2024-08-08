package com.sos.js7.converter.autosys.input.analyzer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.autosys.output.js7.helper.Report;

public class ConditionAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionAnalyzer.class);

    private final Path reportDir;

    // key - condition key
    private Map<String, InConditionHolder> allINConditions;
    // key - job full name
    private Map<String, OutConditionHolder> allOUTConditions;
    // key - condition type, value - condition key
    private Map<ConditionType, Set<Condition>> allConditionsByType;
    // key - job name, value - entire original condition as text
    private Map<String, String> jobsWithORConditions;

    private boolean logAllINConditions = false;
    private boolean logAllOUTConditions = false;

    public ConditionAnalyzer(Path reportDir) {
        this.reportDir = reportDir;
        init();
    }

    public void init() {
        allINConditions = new TreeMap<>();
        allOUTConditions = new TreeMap<>();
        allConditionsByType = new TreeMap<>();
        jobsWithORConditions = new TreeMap<>();
    }

    public void analyze(List<ACommonJob> jobs) {
        for (ACommonJob j : jobs) {
            if (j.isBox()) {
                mapIN(j);
                List<ACommonJob> boxJobs = ((JobBOX) j).getJobs();
                for (ACommonJob bj : boxJobs) {
                    mapIN(bj);
                }
            } else {
                mapIN(j);
            }
        }
        for (ACommonJob j : jobs) {
            if (j.isBox()) {
                mapOUT(j);
                List<ACommonJob> boxJobs = ((JobBOX) j).getJobs();
                for (ACommonJob bj : boxJobs) {
                    mapOUT(bj);
                }
            } else {
                mapOUT(j);
            }
        }
        if (logAllINConditions) {
            LOGGER.info("ALL IN Conditions:");
            allINConditions.entrySet().stream().forEach(e -> {
                LOGGER.info("  Condition=" + e.getKey());// + "-------" + e.getValue().condition);
                for (String jn : e.getValue().jobNames) {
                    LOGGER.info("    JOB=" + jn);
                }

            });
        }
        if (logAllOUTConditions) {
            LOGGER.info("ALL OUT Conditions:");
            allOUTConditions.entrySet().stream().forEach(e -> {
                LOGGER.info("  JOB=" + e.getKey() + "-------" + e.getValue().jobName);
                e.getValue().jobConditions.entrySet().forEach(jc -> {
                    LOGGER.info("    JOB=" + jc.getKey() + ", Conditions:");
                    jc.getValue().entrySet().forEach(jcv -> {
                        LOGGER.info("        " + jcv.getValue());
                    });

                });
            });
        }
    }

    public Map<Condition, Set<String>> getJobOUTConditionsByConditionType(ACommonJob j) {
        OutConditionHolder out = getJobOUTConditions(j);
        if (out == null) {
            return null;
        }
        Map<Condition, Set<String>> m = new HashMap<>();
        out.getJobConditions().entrySet().stream().forEach(e -> {
            e.getValue().entrySet().stream().forEach(ee -> {
                Condition c = ee.getValue();
                Set<String> jobs = m.get(c);
                if (jobs == null) {
                    jobs = new TreeSet<>();
                }
                if (!jobs.contains(e.getKey())) {
                    jobs.add(e.getKey());
                }
                m.put(c, jobs);
            });
        });
        return m;
    }

    public Set<String> getINConditionJobs(Condition c) {
        InConditionHolder in = allINConditions.get(c.getKey());
        if (in == null) {
            return null;
        }
        return in.getJobNames();
    }

    public Map<Condition, Set<String>> getINConditionJobsXXX(JobBOX box) {
        Map<Condition, Set<String>> m = new LinkedHashMap<>();
        for (Map.Entry<String, InConditionHolder> entry : allINConditions.entrySet()) {
            if (entry.getKey().contains(box.getName())) {
                InConditionHolder h = allINConditions.get(entry.getKey());
                if (h != null) {
                    Set<String> jobs = m.get(h.getCondition());
                    if (jobs == null) {
                        jobs = new HashSet<>();
                    }
                    for (String jn : h.getJobNames()) {
                        if (!jobs.contains(jn)) {
                            jobs.add(jn);
                        }
                    }
                    if (jobs.size() > 0) {
                        m.put(h.getCondition(), jobs);
                    }
                }
            }
        }
        return m;
    }

    public Map<Condition, Set<String>> getINConditionJobs(JobBOX box) {
        Map<Condition, Set<String>> m = new LinkedHashMap<>();
        for (Map.Entry<String, InConditionHolder> entry : allINConditions.entrySet()) {
            InConditionHolder h = allINConditions.get(entry.getKey());
            if (h.getCondition().getJobName() == null || !h.getCondition().getJobName().equals(box.getName())) {
                continue;
            }

            Set<String> jobs = m.get(h.getCondition());
            if (jobs == null) {
                jobs = new HashSet<>();
            }
            for (String jn : h.getJobNames()) {
                if (!jobs.contains(jn)) {
                    jobs.add(jn);
                }
            }
            if (jobs.size() > 0) {
                m.put(h.getCondition(), jobs);
            }

        }
        return m;
    }

    private void mapIN(ACommonJob j) {
        // LOGGER.info("[mapIn]job=" + j.getInsertJob().getValue());
        if (j.hasCondition()) {
            for (Condition c : j.conditionsAsList()) {
                InConditionHolder ch = null;
                String condKey = c.getKey();
                if (allINConditions.containsKey(condKey)) {
                    ch = allINConditions.get(condKey);
                } else {
                    ch = new InConditionHolder();
                    ch.condition = c;
                    ch.jobNames = new TreeSet<String>();
                }
                if (!ch.jobNames.contains(j.getName())) {
                    ch.jobNames.add(j.getName());
                }
                allINConditions.put(condKey, ch);
                conditionsByType(c);
            }

            if (j.hasORConditions()) {
                jobsWithORConditions.put(j.getName(), j.getCondition().getOriginalCondition());
            }

            Report.writePerJobConditionReport(reportDir, j);
        }
    }

    private void conditionsByType(Condition c) {
        Set<Condition> s = null;

        ConditionType condKey = c.getType();
        if (allConditionsByType.containsKey(condKey)) {
            s = allConditionsByType.get(condKey);
        } else {
            s = new HashSet<>();
        }
        if (!s.contains(c)) {
            s.add(c);
            allConditionsByType.put(condKey, s);
        }
    }

    private void mapOUT(ACommonJob j) {
        String jobKey = j.getName();
        Map<String, Map<String, Condition>> jobOutConditions = new HashMap<>();
        allINConditions.entrySet().stream().filter(e -> e.getValue().condition.getJobName() != null).forEach(e -> {
            String condJobKey = e.getValue().condition.getJobName();
            if (jobKey.equals(condJobKey)) {
                String condKey = e.getValue().condition.getKey();

                for (String jn : e.getValue().getJobNames()) {
                    Map<String, Condition> jc = jobOutConditions.get(jn);
                    if (jc == null) {
                        jc = new HashMap<>();
                    }
                    if (!jc.containsKey(condKey)) {
                        jc.put(condKey, e.getValue().condition);
                    }
                    jobOutConditions.put(jn, jc);
                }
            }
        });
        if (jobOutConditions.size() > 0) {
            OutConditionHolder ch = null;
            if (allOUTConditions.containsKey(jobKey)) {
                ch = allOUTConditions.get(jobKey);
            } else {
                ch = new OutConditionHolder();
                ch.jobName = jobKey;
            }
            ch.jobConditions = jobOutConditions;
            allOUTConditions.put(jobKey, ch);
        }
    }

    // - Remove from the BOX(mainConditions) conditions that refer to the children BOX jobs (contained in the current BOX)
    // -- e.g. box condition=v(my_var) = "up" & s(my_box.my_box_job1)
    // - Remove from the BOX(mainConditions) conditions that refer to the BOX name itself
    // -- e.g. box condition=v(my_var) = "up" & s(my_box.my_box_name)
    // - Remove duplicates
    // -- e.g. box condition=v(my_var) = "up" & s(my_box.my_box_job1) & s(my_box.my_box_job1)
    private List<Condition> handleJobBoxMainConditions(JobBOX boxJob) throws Exception {
        List<Condition> mainConditions = boxJob.conditionsAsList();

        // Remove from BOX mainCondition conditions related to a "children" Box Job Box
        if (mainConditions != null && mainConditions.size() > 0) {
            Map<String, Condition> toRemoveConditionsRefersToChildrenJobs = new HashMap<>();
            Map<String, Condition> toRemoveConditionsRefersToBoxItself = new HashMap<>();
            Map<String, Condition> toRemoveConditionsDuplicates = new HashMap<>();
            Set<String> all = new HashSet<>();
            for (Condition c : mainConditions) {
                if (c.getJobName() != null) {
                    boolean found = boxJob.getJobs().stream().anyMatch(j -> j.isNameEquals(c));
                    if (found) {
                        toRemoveConditionsRefersToChildrenJobs.put(c.getKey(), c);
                    } else {
                        if (c.getJobName().equals(boxJob.getName())) {
                            toRemoveConditionsRefersToBoxItself.put(c.getKey(), c);
                        }
                    }
                }
                if (all.contains(c.getKey())) {
                    toRemoveConditionsDuplicates.put(c.getKey(), c);
                } else {
                    all.add(c.getKey());
                }
            }

            Report.writePerJobBOXConditionRefersReports(reportDir, boxJob, toRemoveConditionsRefersToChildrenJobs,
                    toRemoveConditionsRefersToBoxItself);

            if (toRemoveConditionsRefersToChildrenJobs.size() > 0) {
                List<Object> boxJobConditions = boxJob.getCondition().getCondition().getValue();
                toRemoveConditionsRefersToChildrenJobs.entrySet().stream().forEach(e -> {
                    Conditions.remove(boxJobConditions, e.getValue());
                });
                mainConditions = boxJob.conditionsAsList();
            }
            if (toRemoveConditionsRefersToBoxItself.size() > 0) {
                List<Object> boxJobConditions = boxJob.getCondition().getCondition().getValue();
                toRemoveConditionsRefersToBoxItself.entrySet().stream().forEach(e -> {
                    Conditions.remove(boxJobConditions, e.getValue());
                });
                mainConditions = boxJob.conditionsAsList();
            }
            if (toRemoveConditionsDuplicates.size() > 0) {
                List<Object> boxJobConditions = boxJob.getCondition().getCondition().getValue();
                toRemoveConditionsDuplicates.entrySet().stream().forEach(e -> {
                    Conditions.remove(boxJobConditions, e.getValue());
                });
                mainConditions = boxJob.conditionsAsList();
            }
        }
        return mainConditions;
    }

    // copy of Autosys2JS7Converter.removeBoxJobMainConditionsFromChildren(box) - was a copy - currently a different implementation
    // BoxJobs.xml - see folders
    // - wrong - casi - fixed
    // -- adpp - adp_ess_acc_rollup_amr_box - fixed
    // - to check - ctba -fixed

    // - TODO
    // - children conditions
    // -- apcd.cdp_p_b_email_reporting_box
    // -- b2bm.mdm_b_daily_batch_group
    // -- ecst.ecost_p_b_pir_feed_box ????
    // -- ctba.ctb_p_c_edi_split_calc_ess_box
    // -- ecst.ecost_p_b_pir_feed_box
    // -- ecst.ecost_p_b_vistex_inbound_box
    // -- !!! gatl.ga_cob_amr_box - gross mehr oder weniger ... Was bedeutet NOTRUNNING????
    // --- gatl.ga_cob_apac_box
    // --- gatl.ga_cob_euro_box
    // -- gatl.ga_gbi_backlog_refresh_amr_box
    // --- gatl.ga_gbi_backlog_refresh_euro_box
    // --- gatl.ga_non_rev_refresh_amr_box
    // --- gatl.ga_non_rev_refresh_apac_box
    // --- gatl.ga_non_rev_refresh_euro_box
    // -- !!! gatl.ga_sap_cw_midweek_amr_box
    // -- !!! gatl.ga_sap_cw_midweek_apac_box
    // -- !!! gatl.ga_sap_cw_midweek_euro_box
    // -- !!! gatl.ga_sap_cw_sproll_amr_box
    // -- !!! gatl.ga_sap_cw_sproll_apac_box
    // -- !!! gatl.ga_sap_cw_sproll_euro_box
    // -- !!! gatl.ga_sap_nw_dat_amr_box
    // -- !!! gatl.ga_sap_nw_dat_apac_box
    // -- !!! gatl.ga_sap_nw_dat_euro_box
    // --- gatl.ga_sap_nw_pst_thu_amr_box ----- ! good example
    // --- gatl.ga_sap_nw_pst_thu_apac_box
    // --- gatl.ga_sap_nw_pst_thu_euro_box
    // --- gatl.ga_sap_nw_wkly_amr_box
    // --- gatl.ga_sap_nw_wkly_euro_box
    // -- !!! gatl.ga_sap_nw_wkly_apac_box - self/back references etc
    // -- !!! gdvp.DR_gdv_p_b_weekly_adj_9000001
    // -- !!! gsma.gsm_p_c_extract_box
    // -- !!! icas...

    // - to check in conditions
    // -- adpp.adp_ess_attributes_amr_box
    // -- adpp.adp_ess_attributes_apac_box
    // -- adpp.adp_ess_attributes_euro_box
    // -- adpp.adp_refresh_tranx_apac_box
    // -- !!! gatl.ga_cob_ww_box - definitiv falsch !!!!!!!!!! in condition BOX auf sich selbst
    // --- gatl.ga_intra_bob_units_box_amr
    // --- gatl.ga_intra_bob_units_box_apac
    // --- gatl.ga_intra_bob_units_box_euro
    // --- gdvp.DR_gdv_p_b_rebuild_index
    // --- gdvp.DR_gdv_p_b_rebuild_index_qtable

    // - good example
    // -- apos.logs_pos_p_b_logs_delete
    // --- b2bm.mdm_b_x_itunes_batch
    // -- gatl.ga_wkly_realignment_amr_box
    // --- gatl.ga_wkly_realignment_apac_box
    // --- gatl.ga_wkly_realignment_euro_box
    // -- ycrd.ycard_p_b_BreakageReport_box_new
    // -- icas.icas_p_dr_b_box_dn

    public void handleJobBoxConditions(List<ACommonJob> boxJobs) throws Exception {
        for (ACommonJob j : boxJobs) {
            if (j.isBox()) {
                handleJobBoxConditions((JobBOX) j);
            }
        }
    }

    public List<ACommonJob> handleJobBoxConditions(JobBOX boxJob) throws Exception {
        boolean doLog = false;

        List<Condition> mainConditions = boxJob.conditionsAsList();
        // Step 1
        mainConditions = handleJobBoxMainConditions(boxJob);
        if (doLog) {
            LOGGER.info("[mainConditions][box=" + boxJob.getName() + "]" + mainConditions);
        }
        // Step 2
        // - Add main conditions (if not exists) to the children jobs, because of case
        // -- box condition=v(my_var) = "up" & s(extern_box.extern_box_job,24.00)
        // -- children job condition=v(my_var) = "up" <- so only 1 variable
        for (ACommonJob j : boxJob.getJobs()) {
            List<Object> jc = j.getCondition().getCondition().getValue();
            if (doLog) {
                LOGGER.info(" [add][before][job=" + j.getName() + "]" + jc);
            }
            for (Condition c : mainConditions) {
                if (doLog) {
                    LOGGER.info("         addIfNotContains=" + c);
                }
                jc = Conditions.addIfNotContains(jc, c);
            }
            if (doLog) {
                LOGGER.info(" [add][after][job=" + j.getName() + "]" + j.getCondition().getCondition().getValue());
            }
        }

        // Step 3
        // - Remove the main conditions from the children jobs
        List<ACommonJob> l = new ArrayList<ACommonJob>();
        Set<String> reportBOXNames = new HashSet<>();
        for (ACommonJob j : boxJob.getJobs()) {
            if (doLog) {
                LOGGER.info("-----------------------------------------------");
            }

            List<Object> jc = j.getCondition().getCondition().getValue();
            if (doLog) {
                LOGGER.info("[remove][job=" + j.getName() + "]" + jc);
            }
            List<Condition> ljc = j.conditionsAsList();
            if (jc != null && jc.size() > 0) {
                boolean remove = true;
                int counter = 0;
                for (Condition c : mainConditions) {
                    Condition fc = Conditions.find(jc, c.getKey());
                    if (fc != null) {
                        counter++;
                    }
                }
                if (counter == mainConditions.size()) {
                    remove = false;
                }
                if (remove) {
                    for (Condition c : mainConditions) {
                        jc = Conditions.remove(jc, c);
                        if (doLog) {
                            LOGGER.info("    " + c);
                        }
                    }
                }
            }
            // END

            // ADDITIONAL ---------------- remove duplicates IN from children
            ljc = j.conditionsAsList();
            if (doLog) {
                LOGGER.info("[removeDuplicates][before][job=" + j.getName() + "][allConditions]" + ljc);
            }
            if (ljc.size() > 1) {
                for (Condition c : ljc) {
                    if (doLog) {
                        LOGGER.info("  [removeDuplicates][" + j.getName() + "][condition]" + c);
                    }

                    boolean used = false;
                    try {
                        used = isAlreadyUsed(boxJob, j, ljc, c);
                    } catch (StackOverflowError e) {
                        used = false;

                        LOGGER.warn("[handleJobBoxConditions][BOX=" + boxJob.getName() + "][recursion detected][" + j.getName() + "]" + c
                                .getOriginalValue());

                        Report.writePerJobBOXConditionRecursionReport(reportDir, boxJob, j, reportBOXNames);
                        // throw e;
                    }
                    if (used) {
                        Conditions.remove(j.getCondition().getCondition().getValue(), c);
                    }

                    if (doLog) {
                        LOGGER.info("    used=" + used + "============" + c.getKey());
                    }

                }
            }
            if (doLog) {
                LOGGER.info("[removeDuplicates][after][job=" + j.getName() + "][allConditions]" + j.conditionsAsList());
            }
            l.add(j);
        }
        return l;
    }

    /** For a given current job(childrenJob), check if a condition(conditionToCheck) is used in the "previous" job<br/>
     * Helper method to decide if this condition should be removed from the current job, because it has already been handled/used
     * 
     * @param boxJob
     * @param childrenJob
     * @param allJobConditions
     * @param conditionToCheck
     * @return */
    private boolean isAlreadyUsed(JobBOX boxJob, ACommonJob childrenJob, List<Condition> allJobConditions, Condition conditionToCheck) {
        List<Condition> otherJobConditions = allJobConditions.stream().filter(c -> c.getJobName() != null && !c.equals(conditionToCheck)).collect(
                Collectors.toList());

        for (Condition oc : otherJobConditions) {
            ACommonJob job = boxJob.getJobs().stream().filter(j -> !j.isNameEquals(childrenJob) && j.isNameEquals(oc)).findFirst().orElse(null);
            if (job != null) {
                List<Condition> cl = job.conditionsAsList();
                for (Condition c : cl) {
                    // variables and job conditions
                    if (c.equals(conditionToCheck)) {
                        return true;
                    }
                    boolean used = isAlreadyUsed(boxJob, childrenJob, cl, conditionToCheck);
                    if (used) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<ACommonJob> handleStandaloneJobsConditions(List<ACommonJob> allJobs) throws Exception {
        boolean doLog = false;

        List<ACommonJob> l = new ArrayList<ACommonJob>();
        for (ACommonJob j : allJobs) {
            if (doLog) {
                LOGGER.info("-----------------------------------------------");
            }

            // END

            // ADDITIONAL ---------------- remove duplicates IN from children
            List<Condition> ljc = j.conditionsAsList();
            if (doLog) {
                LOGGER.info("[removeDuplicates][before][job=" + j.getName() + "][allConditions]" + ljc);
            }

            if (ljc.size() > 1) {
                for (Condition c : ljc) {
                    if (doLog) {
                        LOGGER.info("  [removeDuplicates][" + j.getName() + "][condition]" + c);
                    }

                    boolean used = false;
                    try {
                        used = isAlreadyUsed(allJobs, j, ljc, c);
                    } catch (StackOverflowError e) {
                        used = false;

                        LOGGER.warn("[handleStandaloneJobsConditions][recursion detected][" + j.getName() + "]" + c.getOriginalValue());

                        // throw e;
                    }
                    if (used) {
                        Conditions.remove(j.getCondition().getCondition().getValue(), c);
                    }

                    if (doLog) {
                        LOGGER.info("    used=" + used + "============" + c.getKey());
                    }

                }
            }
            if (doLog) {
                LOGGER.info("[removeDuplicates][after][job=" + j.getName() + "][allConditions]" + j.conditionsAsList());
            }
            l.add(j);
        }
        return l;
    }

    private boolean isAlreadyUsed(List<ACommonJob> allJobs, ACommonJob childrenJob, List<Condition> allJobConditions, Condition conditionToCheck) {
        List<Condition> otherJobConditions = allJobConditions.stream().filter(c -> c.getJobName() != null && !c.equals(conditionToCheck)).collect(
                Collectors.toList());

        for (Condition oc : otherJobConditions) {
            ACommonJob job = allJobs.stream().filter(j -> !j.isNameEquals(childrenJob) && j.isNameEquals(oc)).findFirst().orElse(null);
            if (job != null) {
                List<Condition> cl = job.conditionsAsList();
                for (Condition c : cl) {
                    // variables and job conditions
                    if (c.equals(conditionToCheck)) {
                        return true;
                    }
                    boolean used = isAlreadyUsed(allJobs, childrenJob, cl, conditionToCheck);
                    if (used) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public OutConditionHolder getJobOUTConditions(ACommonJob j) {
        return allOUTConditions.get(j.getName());
    }

    public boolean hasOutConditionForOtherJob(ACommonJob j, ACommonJob o) {
        OutConditionHolder h = allOUTConditions.get(j.getName());
        if (h == null) {
            return false;
        }
        return h.getJobConditions().containsKey(o.getName());
    }

    public Map<ConditionType, Set<Condition>> getAllConditionsByType() {
        return allConditionsByType;
    }

    public Map<String, String> getJobsWithORConditions() {
        return jobsWithORConditions;
    }

    public Map<String, InConditionHolder> getAllINConditions() {
        return allINConditions;
    }

    public class InConditionHolder {

        private Condition condition;
        private Set<String> jobNames;

        public Condition getCondition() {
            return condition;
        }

        public Set<String> getJobNames() {
            return jobNames;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("condition=").append(condition);
            sb.append(",jobNames=").append(jobNames);

            return sb.toString();
        }
    }

    public class OutConditionHolder {

        private String jobName;
        private Map<String, Map<String, Condition>> jobConditions;

        public String getJobName() {
            return jobName;
        }

        public Map<String, Map<String, Condition>> getJobConditions() {
            return jobConditions;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("jobName=").append(jobName);
            sb.append(",jobConditions=").append(jobConditions);

            return sb.toString();
        }

    }
}
