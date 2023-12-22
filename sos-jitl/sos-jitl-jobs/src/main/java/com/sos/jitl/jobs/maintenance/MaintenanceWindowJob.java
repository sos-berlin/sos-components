package com.sos.jitl.jobs.maintenance;

import com.sos.jitl.jobs.maintenance.classes.MaintenanceWindowImpl;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class MaintenanceWindowJob extends Job<MaintenanceWindowJobArguments> {

    public MaintenanceWindowJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<MaintenanceWindowJobArguments> step) throws Exception {
        step.getLogger().debug("maintenance window: will be executed");
      
        MaintenanceWindowImpl impl = new MaintenanceWindowImpl(step);

        MaintenanceWindowImpl impl = new MaintenanceWindowImpl(step);
        impl.executeApiCall();
    }
}