package com.sos.jitl.jobs.maintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.maintenance.MaintenanceWindowJobArguments.StateValues;
import com.sos.jitl.jobs.maintenance.classes.Globals;
import com.sos.jitl.jobs.maintenance.classes.MaintenanceWindowImpl;
import com.sos.jitl.jobs.maintenance.classes.MaintenanceWindowJobReturn;

import js7.data_for_java.order.JOutcome;

public class MaintenanceWindowJob extends ABlockingInternalJob<MaintenanceWindowJobArguments> {

    public MaintenanceWindowJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MaintenanceWindowJobArguments> step) throws Exception {

        try {
            MaintenanceWindowJobReturn maintenanceWindowJobReturn = process(step, step.getArguments());
            return step.success(maintenanceWindowJobReturn.getExitCode(), maintenanceWindowJobReturn.getResultMap());
        } catch (Throwable e) {
            throw e;
        }
    }

    private MaintenanceWindowJobReturn process(JobStep<MaintenanceWindowJobArguments> step, MaintenanceWindowJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        MaintenanceWindowJobReturn maintenanceWindowJobReturn = new MaintenanceWindowJobReturn();
        maintenanceWindowJobReturn.setExitCode(0);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        Globals.debug(logger, String.format("maintenance window: %s will be executed.", ""));

        MaintenanceWindowImpl maintenanceWindowImpl = new MaintenanceWindowImpl(logger, args);
        maintenanceWindowImpl.executeApiCall();

        maintenanceWindowJobReturn.setResultMap(resultMap);

        return maintenanceWindowJobReturn;
    }

    public static void main(String[] args) {

        MaintenanceWindowJobArguments arguments = new MaintenanceWindowJobArguments();
        arguments.setControllerHost("controller-2-0-primary");
        arguments.setControllerId("testsuite");
        arguments.setState(StateValues.ACTIVE);

        arguments.setJocHost("joc-2-0-primary");

        List<String> agentIds = new ArrayList<String>();
        List<String> subAgentIds = new ArrayList<String>();
        subAgentIds.add("subagent_third_001");
        agentIds.add("agent_101");

        arguments.setAgentIds(agentIds);
        arguments.setSubAgentIds(subAgentIds);

        MaintenanceWindowJob maintenanceWindowJob = new MaintenanceWindowJob(null);

        try {
            maintenanceWindowJob.process(null, arguments);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}