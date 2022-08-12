package com.sos.jitl.jobs.fileordervariablesjob;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.common.JitlJobReturn;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument.Type;
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringReturnParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;

import js7.data.value.StringValue;
import js7.data_for_java.order.JOutcome;

public class FileOrderVariablesJob extends ABlockingInternalJob<FileOrderVariablesJobArguments> {

    public FileOrderVariablesJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<FileOrderVariablesJobArguments> step) throws Exception {

        try {
            JitlJobReturn jitlJobReturn = process(step, step.getArguments());
            return step.success(jitlJobReturn.getExitCode(), jitlJobReturn.getResultMap());

        } catch (Throwable e) {
            throw e;
        }
    }

    public JitlJobReturn process(JobStep<FileOrderVariablesJobArguments> step, FileOrderVariablesJobArguments args) throws Exception {
        JobLogger logger = null;
        logger = step.getLogger();

        JitlJobReturn jitlJobReturn = new JitlJobReturn();
        jitlJobReturn.setExitCode(0);

        Map<String, Object> jobArguments = null;
        if (step != null) {
            jobArguments = Job.asNameValueMap(step.getAllCurrentArguments());
            if (step.getInternalStep() != null) {
                Object o = Job.getValue(step.getInternalStep().order().arguments().get("file"));
                if (o instanceof String) {
                    String s = (String) o;
                    args.setJs7SourceFile(s);
                }
            }else {
                jobArguments.put("a","1235");
            }
        }

        ExecuteFileOrderVariables executeFileOrderVariables = new ExecuteFileOrderVariables(logger, jobArguments, args);
        Map<String, Object> resultMap = executeFileOrderVariables.getVariables();

        jitlJobReturn.setResultMap(resultMap);

        return jitlJobReturn;
    }

}