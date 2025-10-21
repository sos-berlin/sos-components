package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.sos.inventory.model.job.Job;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStream;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;
import com.sos.js7.converter.js1.output.js7.JS12JS7Converter;
import com.sos.js7.converter.js1.output.js7.JS7JobStreamsConverter;
import com.sos.js7.converter.js1.output.js7.JS7RunTimeConverter;

public class JobStreamsHelper {

    public static Map<String, JobStreamJS1JS7Job> convert(JS12JS7Converter js7Converter, JS7ConverterResult result, Path file, JobStream jobStream) {

        ParserReport.INSTANCE.addAnalyzerRecord(file, "JOBSTREAM=" + jobStream.getJobStream(), "jobs=" + jobStream.getJobs().size());

        Map<String, JobStreamJS1JS7Job> jobs = new HashMap<>();
        for (JobStreamJob js1JobStreamJob : jobStream.getJobs()) {
            Path path = JS7JobStreamsConverter.getJobPath(js7Converter.getPr().getRoot().getPath(), js1JobStreamJob);
            StandaloneJob js1Job = js7Converter.findStandaloneJobByPath(path);
            if (js1Job == null) {
                ConverterReport.INSTANCE.addAnalyzerRecord(file, "convert=" + jobStream.getJobStream(), "[" + path + "]StandaloneJob not found");
            } else {
                js7Converter.getJS1JobStreamJobs().add(path);
                JobHelper jh = js7Converter.getJob(result, js1Job, null, null, null);
                if (jh != null) {
                    Job js7Job = jh.getJS7Job();
                    js7Job.setAdmissionTimeScheme(JS7RunTimeConverter.toJobAdmissionTimeScheme(js1Job));

                    String js7JobName = JS7ConverterHelper.getJS7ObjectName(path, js1Job.getName());
                    jobs.put(js1JobStreamJob.getJob(), new JobStreamJS1JS7Job(js1Job, js1JobStreamJob, js7Job, js7JobName));
                }
            }
        }
        return jobs;
    }

}
