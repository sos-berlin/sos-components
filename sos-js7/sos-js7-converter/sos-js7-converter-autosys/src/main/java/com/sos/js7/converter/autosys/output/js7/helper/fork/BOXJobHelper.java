package com.sos.js7.converter.autosys.output.js7.helper.fork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.output.js7.helper.ConverterBOXJobs;

public class BOXJobHelper {

    private final JobBOX box;
    private final ACommonJob job;
    private final Map<Condition, List<ACommonJob>> thisBoxConditions;

    private boolean used = false;

    public BOXJobHelper(JobBOX box, ACommonJob job, Map<Condition, List<ACommonJob>> thisBoxConditions) {
        this.box = box;
        this.job = job;
        this.thisBoxConditions = thisBoxConditions;
    }

    public boolean isUsed() {
        return used;
    }

    public void isUsed(boolean val) {
        used = val;
        if (used) {
            String key = box.getName();
            List<BOXJobHelper> l = ConverterBOXJobs.USED_JOBS_PER_BOX.get(key);
            if (l == null) {
                l = new ArrayList<>();
            }
            if (!l.contains(this)) {
                l.add(this);
            }
            ConverterBOXJobs.USED_JOBS_PER_BOX.put(key, l);
        }
    }

    public ACommonJob getJob() {
        return job;
    }

    public Map<Condition, List<ACommonJob>> getThisBoxConditions() {
        return thisBoxConditions;
    }

    public boolean allThisBoxConditionsCompleted() {
        if (thisBoxConditions == null || thisBoxConditions.size() == 0) {
            return true;
        }

        List<BOXJobHelper> lh = ConverterBOXJobs.USED_JOBS_PER_BOX.get(box.getName());
        if (lh == null) {
            return false;
        }
        for (Map.Entry<Condition, List<ACommonJob>> entry : thisBoxConditions.entrySet()) {
            for (ACommonJob j : entry.getValue()) {
                if (!lh.stream().anyMatch(jh -> jh.job.equals(j) && jh.isUsed())) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof BOXJobHelper) {
            return job.getName().equals(((BOXJobHelper) o).job.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.job.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("[");
        sb.append("job=[").append(job).append("]");
        sb.append(",isUsed=").append(used);
        sb.append("]");
        return sb.toString();
    }
}
