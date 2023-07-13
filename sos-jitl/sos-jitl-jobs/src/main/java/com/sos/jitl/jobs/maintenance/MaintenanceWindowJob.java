package com.sos.jitl.jobs.maintenance;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.OrderProcessStep;
import com.sos.jitl.jobs.maintenance.classes.MaintenanceWindowImpl;

public class MaintenanceWindowJob extends ABlockingInternalJob<MaintenanceWindowJobArguments> {

    public MaintenanceWindowJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<MaintenanceWindowJobArguments> step) throws Exception {
        step.getLogger().debug("maintenance window: will be executed");

        MaintenanceWindowImpl impl = new MaintenanceWindowImpl(step.getLogger(), step.getDeclaredArguments());
        impl.executeApiCall();
    }
}