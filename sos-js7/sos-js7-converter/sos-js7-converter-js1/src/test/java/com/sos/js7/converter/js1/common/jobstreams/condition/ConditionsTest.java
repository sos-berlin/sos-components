package com.sos.js7.converter.js1.common.jobstreams.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.js1.common.jobstreams.condition.Condition.ConditionType;
import com.sos.js7.converter.js1.common.jobstreams.condition.Conditions.Operator;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamJS1JS7Job;

public class ConditionsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionsTest.class);

    @Ignore
    @SuppressWarnings("unchecked")
    @Test
    public void testParse() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("not (");
        sb.append(" fileexist:test.txt");
        sb.append(" and global:my_event ");
        sb.append(" and global:my_event[today]");
        sb.append(" and my_event");
        sb.append(" and my_event[today]");
        sb.append(" && rc:0");
        sb.append(" and rc:0-10");
        sb.append(" and job:rc:1");
        sb.append(" and job:rc:1-10");
        sb.append(" and job:my_job.rc:10-100");
        sb.append(" and job:isCompletedToday");
        sb.append(" and job:my_job.isCompletedToday");
        sb.append(" AND jobChain:isStartedToday");
        sb.append(")");
        sb.append(" OR jobChain:isStartedToday");
        sb.append(" OR not jobChain:isStartedToday");
        sb.append(" and not jobChain:my_jobchain.isStartedToday");
        sb.append(" && ! jobChain:my_jobchain.isStartedToday");

        // sb = new StringBuilder("");
        // 7sb.append("(a and (b and c)) and (c and d)");

        Conditions conditions = new Conditions();
        List<Object> result = conditions.parse(sb.toString());
        LOGGER.info("--- CONDITIONS (WITH OPERATORS) ---- maxGroupLevel=" + conditions.getMaxGroupLevel());
        if (conditions.getMaxGroupLevel() > 1) { // nested groups like : (a and (b and c)) and (c and d)
            LOGGER.info("--- !!! UNEXPECTED RESULT because the nested groups used");
        }
        for (Object o : result) {
            if (o instanceof ArrayList) {
                LOGGER.info("GROUP: ");
                List<Object> l = (ArrayList<Object>) o;
                for (Object oo : l) {
                    LOGGER.info("    " + SOSString.toString(oo));
                }

            } else {
                LOGGER.info(SOSString.toString(o));
            }
        }

        LOGGER.info("");
        LOGGER.info("--- CONDITIONS BY TYPE ----");
        Map<ConditionType, List<Condition>> ct = Conditions.getConditionsByType(result);
        ct.entrySet().forEach(e -> {
            LOGGER.info("Condition[" + e.getKey().toString() + "]:");
            for (Condition con : e.getValue()) {
                LOGGER.info("    " + SOSString.toString(con));
            }
        });

        LOGGER.info("");
        LOGGER.info("--- OPERATORS BY TYPE ----");
        Map<Operator, List<Operator>> ot = Conditions.getOperatorsByType(result);
        LOGGER.info("  Only AND conditions=" + Conditions.onlyANDConditions(ot));
        ot.entrySet().forEach(e -> {
            LOGGER.info("    Operator[" + e.getKey().toString() + "]count: " + e.getValue().size());
        });
    }

    @Ignore
    @Test
    public void testJobs() throws Exception {
        Map<String, List<JobStreamJS1JS7Job>> jobs = new HashMap<>();
        List<JobStreamJS1JS7Job> w1 = new ArrayList<>();
        w1.add(createJob("my_job_1"));
        jobs.put("w1", w1);

        List<JobStreamJS1JS7Job> w2 = new ArrayList<>();
        w1.add(createJob("my_job_2"));
        jobs.put("w2", w2);

        StringBuilder sb = new StringBuilder();
        sb.append("my_job_1");
        sb.append(" and my_job_2");
        sb.append(" and my_job_3");

        Conditions conditions = new Conditions();
        List<Object> result = conditions.parse(sb.toString());

        conditions.hasAllJobs(Conditions.getConditions(result), jobs);

    }

    private static JobStreamJS1JS7Job createJob(String jobName) {
        JobStreamJob js1JobStreamJob = new JobStreamJob();
        js1JobStreamJob.setJob("/" + jobName);
        JobStreamJS1JS7Job job = new JobStreamJS1JS7Job(null, js1JobStreamJob, null, jobName);
        return job;
    }
}
