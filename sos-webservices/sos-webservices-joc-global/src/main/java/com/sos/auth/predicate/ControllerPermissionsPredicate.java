
package com.sos.auth.predicate;

import java.util.function.Predicate;

import com.sos.auth.predicate.controller.Agents;
import com.sos.auth.predicate.controller.Deployments;
import com.sos.auth.predicate.controller.Locks;
import com.sos.auth.predicate.controller.NoticeBoards;
import com.sos.auth.predicate.controller.Orders;
import com.sos.auth.predicate.controller.Workflows;

public class ControllerPermissionsPredicate {

    private String prefix = "sos:products:controller";
    private Deployments deployments;
    private Orders orders;
    private Agents agents;
    private NoticeBoards noticeBoards;
    private Locks locks;
    private Workflows workflows;

    public ControllerPermissionsPredicate() {
        agents = new Agents(prefix);
        deployments = new Deployments(prefix);
        locks = new Locks(prefix);
        workflows = new Workflows(prefix);
        noticeBoards = new NoticeBoards(prefix);
        orders = new Orders(prefix);
    }

    public Predicate<String> getView() {
        return JocPermissionsPredicate.createPredicate(prefix, "view");
    }

    public Predicate<String> getRestart() {
        return JocPermissionsPredicate.createPredicate(prefix, "restart");
    }

    public Predicate<String> getTerminate() {
        return JocPermissionsPredicate.createPredicate(prefix, "terminate");
    }

    public Predicate<String> getGetLog() {
        return JocPermissionsPredicate.createPredicate(prefix, "get_log");
    }

    public Predicate<String> getSwitchOver() {
        return JocPermissionsPredicate.createPredicate(prefix, "switch_over");
    }

    public Deployments getDeployments() {
        return deployments;
    }

    public Orders getOrders() {
        return orders;
    }

    public Agents getAgents() {
        return agents;
    }

    public NoticeBoards getNoticeBoards() {
        return noticeBoards;
    }

    public Locks getLocks() {
        return locks;
    }

    public Workflows getWorkflows() {
        return workflows;
    }

}
