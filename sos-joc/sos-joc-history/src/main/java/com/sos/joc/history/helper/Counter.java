package com.sos.joc.history.helper;

import java.lang.reflect.Field;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Counter {

    private final Controller controller;
    private final Agent agent;
    private final Order order;
    private final OrderStep orderStep;

    private int total = 0;
    private int processed = 0;
    @SuppressWarnings("unused")
    private int skipped;
    @SuppressWarnings("unused")
    private int failed;

    public Counter() {
        controller = new Controller();
        agent = new Agent();
        order = new Order();
        orderStep = new OrderStep();
    }

    public void setTotal(int val) {
        total = val;
    }

    public int getTotal() {
        return total;
    }

    public void addProcessed() {
        processed += 1;
    }

    public int getProcessed() {
        return processed;
    }

    public void addSkipped() {
        skipped += 1;
    }

    public void addFailed() {
        failed += 1;
    }

    public Controller getController() {
        return controller;
    }

    public Agent getAgent() {
        return agent;
    }

    public Order getOrder() {
        return order;
    }

    public OrderStep getOrderStep() {
        return orderStep;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(toString(this)).append(",");
        if (controller.ready > 0 || controller.shutdown > 0 || controller.clusterCoupled > 0) {
            sb.append(toString(controller)).append(",");
        }
        if (agent.ready > 0 || agent.couplingFailed > 0 || agent.shutdown > 0 || agent.subagentDedicated > 0) {
            sb.append(toString(agent)).append(",");
        }
        sb.append(toString(order)).append(",").append(toString(orderStep));
        return sb.toString();
    }

    private String toString(Object o) {
        return (new ReflectionToStringBuilder(o, ToStringStyle.SHORT_PREFIX_STYLE) {

            protected boolean accept(Field f) {
                try {
                    return super.accept(f) && f.getInt(o) > 0;
                } catch (Throwable e) {
                    return false;
                }
            }
        }).toString();
    }

    public class Controller {

        private int ready = 0;
        private int clusterCoupled = 0;
        private int shutdown = 0;

        public void addReady() {
            ready += 1;
        }

        public void addClusterCoupled() {
            clusterCoupled += 1;
        }

        public void addShutdown() {
            shutdown += 1;
        }
    }

    public class Agent {

        private int ready = 0;
        private int couplingFailed = 0;
        private int shutdown = 0;
        private int subagentDedicated = 0;

        public void addReady() {
            ready += 1;
        }

        public void addCouplingFailed() {
            couplingFailed += 1;
        }

        public void addShutdown() {
            shutdown += 1;
        }

        public void addSubagentDedicated() {
            subagentDedicated += 1;
        }
    }

    public class Order {

        @SuppressWarnings("unused")
        private int started = 0;
        @SuppressWarnings("unused")
        private int resumed = 0;
        @SuppressWarnings("unused")
        private int resumeMarked = 0;
        @SuppressWarnings("unused")
        private int forked = 0;
        @SuppressWarnings("unused")
        private int joined = 0;
        @SuppressWarnings("unused")
        private int failed = 0;
        @SuppressWarnings("unused")
        private int suspended = 0;
        @SuppressWarnings("unused")
        private int suspendMarked = 0;
        @SuppressWarnings("unused")
        private int cancelled = 0;
        @SuppressWarnings("unused")
        private int cancelledNotStarted = 0;
        @SuppressWarnings("unused")
        private int broken = 0;
        @SuppressWarnings("unused")
        private int finished = 0;
        @SuppressWarnings("unused")
        private int locksAcquired = 0;
        @SuppressWarnings("unused")
        private int locksQueued = 0;
        @SuppressWarnings("unused")
        private int locksReleased = 0;
        @SuppressWarnings("unused")
        private int noticesExpected = 0;
        @SuppressWarnings("unused")
        private int noticesRead = 0;
        @SuppressWarnings("unused")
        private int noticePosted = 0;
        @SuppressWarnings("unused")
        private int caught = 0;
        @SuppressWarnings("unused")
        private int retrying = 0;

        public void addStarted() {
            started += 1;
        }

        public void addResumed() {
            resumed += 1;
        }

        public void addResumeMarked() {
            resumeMarked += 1;
        }

        public void addForked() {
            forked += 1;
        }

        public void addJoined() {
            joined += 1;
        }

        public void addFailed() {
            failed += 1;
        }

        public void addSuspended() {
            suspended += 1;
        }

        public void addSuspendMarked() {
            suspendMarked += 1;
        }

        public void addCancelled() {
            cancelled += 1;
        }

        public void addCancelledNotStarted() {
            cancelledNotStarted += 1;
        }

        public void addBroken() {
            broken += 1;
        }

        public void addFinished() {
            finished += 1;
        }

        public void addLocksAcquired() {
            locksAcquired += 1;
        }

        public void addLocksQueued() {
            locksQueued += 1;
        }

        public void addLocksReleased() {
            locksReleased += 1;
        }

        public void addNoticesExpected() {
            noticesExpected += 1;
        }

        public void addNoticesRead() {
            noticesRead += 1;
        }

        public void addNoticePosted() {
            noticePosted += 1;
        }

        public void addCaught() {
            caught += 1;
        }

        public void addRetrying() {
            retrying += 1;
        }
    }

    public class OrderStep {

        @SuppressWarnings("unused")
        private int started = 0;
        @SuppressWarnings("unused")
        private int stdWritten = 0;
        @SuppressWarnings("unused")
        private int processed = 0;

        public void addStarted() {
            started += 1;
        }

        public void addStdWritten() {
            stdWritten += 1;
        }

        public void addProcessed() {
            processed += 1;
        }

    }

    public static void main(String[] args) {
        Counter c = new Counter();
        c.setTotal(100);

        c.getAgent().addReady();

        c.getOrder().addStarted();
        c.getOrder().addStarted();
        c.getOrder().addBroken();

        c.getOrderStep().addStdWritten();
        System.out.println(c.toString());
    }

}
