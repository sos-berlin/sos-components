package com.sos.jitl.jobs.orderstatustransition;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.OrderProcessStep;
import com.sos.jitl.jobs.orderstatustransition.classes.OrderStateTransition;
import com.sos.joc.model.order.OrderStateText;

public class OrderStateTransitionJob extends ABlockingInternalJob<OrderStateTransitionJobArguments> {

    private static final String RESUMED = "RESUMED";

    public OrderStateTransitionJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<OrderStateTransitionJobArguments> step) throws Exception {
        OrderStateTransition orderStateTransition = new OrderStateTransition(step.getLogger(), step.getDeclaredArguments());
        if (step.getDeclaredArguments().getControllerId() == null || step.getDeclaredArguments().getControllerId().isEmpty()) {
            step.getDeclaredArguments().setControllerId(step.getControllerId());
        }

        if (step.getDeclaredArguments().getWorkflowFolders().size() == 0) {
            step.getDeclaredArguments().getWorkflowFolders().add("/*");
        }

        step.getDeclaredArguments().setStateTransitionSource(step.getDeclaredArguments().getStateTransitionSource().toUpperCase());
        step.getDeclaredArguments().setStateTransitionTarget(step.getDeclaredArguments().getStateTransitionTarget().toUpperCase());

        if (!OrderStateText.fromValue(step.getDeclaredArguments().getStateTransitionSource()).equals(OrderStateText.FAILED) && !OrderStateText
                .fromValue(step.getDeclaredArguments().getStateTransitionSource()).equals(OrderStateText.PROMPTING) && !OrderStateText.fromValue(step
                        .getDeclaredArguments().getStateTransitionSource()).equals(OrderStateText.SUSPENDED)) {
            throw new Exception("state_transition_source: Illegal value. Not in [FAILED|PROMPTING|SUSPENDED]");
        }

        if (!OrderStateText.fromValue(step.getDeclaredArguments().getStateTransitionTarget()).equals(OrderStateText.CANCELLED) && !step
                .getDeclaredArguments().getStateTransitionTarget().equals(RESUMED)) {
            throw new Exception("state_transition_target: Illegal value. Not in [CANCELLED|RESUMED]");
        }
        orderStateTransition.execute();
    }
}