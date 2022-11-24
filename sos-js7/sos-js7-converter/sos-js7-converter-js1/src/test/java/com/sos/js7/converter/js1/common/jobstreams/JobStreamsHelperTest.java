package com.sos.js7.converter.js1.common.jobstreams;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.js1.common.json.jobstreams.JobStream;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamStarter;
import com.sos.js7.converter.js1.output.js7.JS7JobStreamsConverter;

public class JobStreamsHelperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStreamsHelperTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Path live = Paths.get("C://tmp/live");
        String config = "src/test/resources/jobstreams.json";

        List<JobStream> jobStreams = JS7JobStreamsConverter.read(Paths.get(config));
        Set<Path> jobs = new HashSet<>();
        for (JobStream jobStream : jobStreams) {
            LOGGER.info("[jobStream=" + jobStream.getJobStream() + "]-----------------------------");
            for (JobStreamStarter starter : jobStream.getJobstreamStarters()) {
                // LOGGER.info(" starter=" + SOSString.toString(starter));
                LOGGER.info("  [starter=" + starter.getStarterName() + "]");
                LOGGER.info("      title=" + starter.getTitle());
                LOGGER.info("      nextStart=" + starter.getNextStart());
                LOGGER.info("      requiredJob=" + starter.getRequiredJob());
                LOGGER.info("      runTime=" + starter.getRunTime());
                LOGGER.info("      params=" + starter.getParams());
                LOGGER.info("      jobs=" + starter.getJobs().size());
                for (JobStreamJob job : starter.getJobs()) {
                    LOGGER.info("         job=" + job.getJob());
                    LOGGER.info("           startDelay=" + job.getStartDelay());
                    LOGGER.info("           nextPeriod=" + job.getNextPeriod());
                    LOGGER.info("           inConditions=" + job.getInconditions());
                    LOGGER.info("           outConditions=" + job.getOutconditions());
                    LOGGER.info("           skipOutConditions=" + job.getSkipOutCondition());
                }
            }
            LOGGER.info("  [jobs=" + jobStream.getJobs().size() + "]");
            for (JobStreamJob job : jobStream.getJobs()) {
                LOGGER.info("      job=" + job.getJob());
                LOGGER.info("         startDelay=" + job.getStartDelay());
                LOGGER.info("         nextPeriod=" + job.getNextPeriod());
                LOGGER.info("         inConditions=" + job.getInconditions());
                LOGGER.info("         outConditions=" + job.getOutconditions());
                LOGGER.info("         skipOutConditions=" + job.getSkipOutCondition());

                jobs.add(JS7JobStreamsConverter.getJobPath(live, job));
            }
        }

        LOGGER.info("[ALL JOBS=" + jobs.size() + "]-----------------------------");
        for (Path p : jobs) {
            LOGGER.info("    " + p);
        }

    }
}
