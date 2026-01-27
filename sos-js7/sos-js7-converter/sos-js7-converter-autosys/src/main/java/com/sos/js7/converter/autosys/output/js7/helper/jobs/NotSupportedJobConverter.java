package com.sos.js7.converter.autosys.output.js7.helper.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.js7.converter.autosys.common.v12.job.JobNotSupported;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.report.ConverterReport;

public class NotSupportedJobConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotSupportedJobConverter.class);

    public static Job setExecutable(Job j, JobNotSupported jilJob, String platform) {
        LOGGER.warn(String.format("[%s][job_type=%s][%s]%s", jilJob.getConverterJobType(), jilJob.getJobType().getValue(), jilJob.getName(),
                getMessageNotSupported(jilJob.getJobType().getValue())));
        ConverterReport.INSTANCE.addAnalyzerRecord(jilJob.getSource(), jilJob.getName(), jilJob.getConverterJobType() + ": "
                + NotSupportedJobConverter.getMessageNotSupported(jilJob.getJobType().getValue()));

        boolean isUnix = platform.equals(Platform.UNIX.name());
        String commentBegin = isUnix ? "# " : "REM ";

        StringBuilder script = new StringBuilder();
        script.append(commentBegin).append("Autosys job_type=" + jilJob.getJobType().getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
        if (jilJob.getUnknown() != null) {
            for (SOSArgument<String> a : jilJob.getUnknown()) {
                script.append(commentBegin).append(a.getName()).append("=").append(a.getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }
        script.append("echo \"" + getMessageNotSupported(jilJob.getJobType().getValue()) + "\"");

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());

        j.setExecutable(es);
        return j;
    }

    public static String getMessageNotSupported(String autosysJobType) {
        return "This job could not be converted correctly by the JS7 converter because the job type " + autosysJobType
                + " is not supported and requires manual adjustment";
    }

    @SuppressWarnings("unused")
    private static String getMessageNotSupported() {
        return "These jobs could not be converted correctly by the JS7 converter because the job type is not supported and requires manual adjustment";
    }
}
