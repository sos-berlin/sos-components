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
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringReturnParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;

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

    private JitlJobReturn process(JobStep<FileOrderVariablesJobArguments> step, FileOrderVariablesJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        JitlJobReturn jitlJobReturn = new JitlJobReturn();
        jitlJobReturn.setExitCode(0);

        Map<String, Object> jobArguments = null;
        if (step != null) {
            jobArguments = Job.asNameValueMap(step.getAllCurrentArguments());
        } else {
            jobArguments = new HashMap<String, Object>();
            jobArguments.put("a", "1234");
        }

        ExecuteFileOrderVariables executeFileOrderVariables = new ExecuteFileOrderVariables(logger, jobArguments, args);
        Map<String, Object> resultMap = executeFileOrderVariables.getVariables();

        jitlJobReturn.setResultMap(resultMap);

        return jitlJobReturn;
    }

    public static void main(String[] args) {

        FileOrderVariablesJobArguments arguments = new FileOrderVariablesJobArguments();
        arguments.setJs7SourceFile("c:/temp/1.txt");
        FileOrderVariablesJob fileOrderVariablesJob = new FileOrderVariablesJob(null);

        try {
            fileOrderVariablesJob.process(null, arguments);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}