package com.sos.jitl.jobs.orderstatustransition;

import com.sos.jitl.jobs.orderstatustransition.classes.OrderStateTransition;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class OrderStateTransitionJob extends Job<OrderStateTransitionJobArguments> {

    private static final String CONTINUE = "CONTINUE";
    private static final String CANCEL = "CANCEL";
    private static final String SUSPEND = "SUSPEND";

    public OrderStateTransitionJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<OrderStateTransitionJobArguments> step) throws Exception {
        OrderStateTransition orderStateTransition = new OrderStateTransition(step.getLogger(), step.getDeclaredArguments());
        if (step.getDeclaredArguments().getControllerId() == null || step.getDeclaredArguments().getControllerId().isEmpty()) {
            step.getDeclaredArguments().setControllerId(step.getControllerId());
        }

        if (step.getDeclaredArguments().getWorkflowFolders().size() == 0) {
            step.getDeclaredArguments().getWorkflowFolders().add("/*");
        }

        step.getDeclaredArguments().setStates(step.getDeclaredArguments().getStates().toUpperCase());
        step.getDeclaredArguments().setTransition(step.getDeclaredArguments().getTransition().toUpperCase());
        if (step.getDeclaredArguments().getTransition().equals(OrderStateText.CANCELLED.name())) {
            step.getDeclaredArguments().setTransition(CANCEL);
        }
        if (step.getDeclaredArguments().getTransition().equals(OrderStateText.INPROGRESS.name())) {
            step.getDeclaredArguments().setTransition(CONTINUE);
        }

        if (!step.getDeclaredArguments().getTransition().equals(CANCEL) && !step.getDeclaredArguments().getTransition().equals(SUSPEND) &&!step.getDeclaredArguments().getTransition().equals(CONTINUE)) {
            throw new Exception("tranistion: Illegal value. Not in [CANCEL|SUSPEND|CONTINUE]");
        }

        orderStateTransition.execute();
    }
}