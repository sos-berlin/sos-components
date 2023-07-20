package com.sos.jitl.jobs.orderstatustransition;

import com.sos.jitl.jobs.orderstatustransition.classes.OrderStateTransition;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class OrderStateTransitionJob extends Job<OrderStateTransitionJobArguments> {

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

        step.getDeclaredArguments().setStateTransitionSource(step.getDeclaredArguments().getStateTransitionSource().toUpperCase());
        step.getDeclaredArguments().setStateTransitionTarget(step.getDeclaredArguments().getStateTransitionTarget().toUpperCase());

        if (!OrderStateText.fromValue(step.getDeclaredArguments().getStateTransitionSource()).equals(OrderStateText.FAILED) && !OrderStateText
                .fromValue(step.getDeclaredArguments().getStateTransitionSource()).equals(OrderStateText.PROMPTING) && !OrderStateText.fromValue(step
                        .getDeclaredArguments().getStateTransitionSource()).equals(OrderStateText.SUSPENDED)) {
            throw new Exception("state_transition_source: Illegal value. Not in [FAILED|PROMPTING|INPROGRESS]");
        }

        if (!OrderStateText.fromValue(step.getDeclaredArguments().getStateTransitionTarget()).equals(OrderStateText.CANCELLED) && !OrderStateText
                .fromValue(step.getDeclaredArguments().getStateTransitionTarget()).equals(OrderStateText.INPROGRESS)) {
            throw new Exception("state_transition_target: Illegal value. Not in [CANCELLED|INPROGRESS]");
        }
        orderStateTransition.execute();
    }
}