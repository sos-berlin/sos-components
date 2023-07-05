package com.sos.jitl.jobs.maintenance;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.maintenance.classes.MaintenanceWindowImpl;

import js7.data_for_java.order.JOutcome;

public class MaintenanceWindowJob extends ABlockingInternalJob<MaintenanceWindowJobArguments> {

    public MaintenanceWindowJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MaintenanceWindowJobArguments> step) throws Exception {
        step.getLogger().debug("maintenance window: will be executed");

        MaintenanceWindowImpl impl = new MaintenanceWindowImpl(step.getLogger(), step.getDeclaredArguments());
        impl.executeApiCall();

        return step.success();
    }
}