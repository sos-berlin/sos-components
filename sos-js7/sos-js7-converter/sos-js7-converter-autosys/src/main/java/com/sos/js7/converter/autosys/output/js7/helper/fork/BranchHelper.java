package com.sos.js7.converter.autosys.output.js7.helper.fork;

import java.util.List;

import com.sos.inventory.model.workflow.Branch;

public class BranchHelper {

    private final List<BOXJobHelper> childrenJobs;
    private final List<BOXJobHelper> closingJobs;

    private Branch branch;

    public BranchHelper(List<BOXJobHelper> childrenJobs, List<BOXJobHelper> closingJobs) {
        this.childrenJobs = childrenJobs;
        this.closingJobs = closingJobs;
    }

    public List<BOXJobHelper> getChildrenJobs() {
        return childrenJobs;
    }

    public List<BOXJobHelper> getClosingJobs() {
        return closingJobs;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch val) {
        branch = val;
    }

}
