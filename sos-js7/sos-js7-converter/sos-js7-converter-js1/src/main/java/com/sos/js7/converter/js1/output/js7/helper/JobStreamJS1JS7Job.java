package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobstreams.condition.Condition;
import com.sos.js7.converter.js1.common.jobstreams.condition.Conditions;
import com.sos.js7.converter.js1.common.json.jobstreams.InCondition;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;
import com.sos.js7.converter.js1.common.json.jobstreams.OutCondition;
import com.sos.js7.converter.js1.common.json.jobstreams.OutConditionEvent;

public class JobStreamJS1JS7Job {

    private final StandaloneJob js1Job;
    private final JobStreamJob js1JobStreamJob;
    private final Job js7Job;
    private final String js7JobName;

    private Path js7WorkflowPath;
    private String js7WorkflowName;
    private String js7BranchPath;

    private Set<String> js1OutEventNames = new HashSet<>();
    private Set<String> js1InEventNames = new HashSet<>();

    public JobStreamJS1JS7Job(StandaloneJob js1Job, JobStreamJob js1JobStreamJob, Job js7Job, String js7JobName) {
        this.js1Job = js1Job;
        this.js1JobStreamJob = js1JobStreamJob;
        this.js7Job = js7Job;
        this.js7JobName = js7JobName;

        setProperties();
    }

    private void setProperties() {
        setJS1InEventsProperties();
        setJS1OutEventsProperties();

    }

    private void setJS1InEventsProperties() {
        if (js1JobStreamJob.getInconditions() != null) {
            for (InCondition oc : js1JobStreamJob.getInconditions()) {
                if (oc.getConditionExpression() != null) {
                    try {
                        Conditions c = new Conditions();
                        List<Object> o = c.parse(oc.getConditionExpression().getExpression());
                        List<Condition> co = Conditions.getConditions(o);
                        for (Condition v : co) {
                            switch (v.getType()) {
                            case EVENT:
                                js1InEventNames.add(v.getName());
                                break;
                            default:
                                break;
                            }
                        }

                    } catch (Exception e) {
                        ConverterReport.INSTANCE.addErrorRecord(String.format("[JOBSTREAM][JOB=%s][inCondition=%s]%s", js1JobStreamJob.getJob(), oc
                                .getConditionExpression(), e.toString()));
                    }
                }
            }
        }
    }

    private void setJS1OutEventsProperties() {
        if (js1JobStreamJob.getOutconditions() != null) {
            List<String> returnCodes = new ArrayList<>();

            for (OutCondition oc : js1JobStreamJob.getOutconditions()) {
                if (oc.getConditionExpression() != null) {
                    try {
                        Conditions c = new Conditions();
                        List<Object> o = c.parse(oc.getConditionExpression().getExpression());
                        List<Condition> co = Conditions.getConditions(o);
                        for (Condition v : co) {
                            switch (v.getType()) {
                            case RC:
                                String value = v.getTrimmedValue();
                                if (value != null && !value.equals("0")) {
                                    String[] arr = value.split("-");
                                    switch (arr.length) {
                                    case 0:
                                        break;
                                    case 1:
                                        if (value.endsWith("-")) {
                                            returnCodes.add(arr[0] + "..255");
                                        } else {
                                            returnCodes.add(arr[0]);
                                        }
                                        break;
                                    default:
                                        returnCodes.add(arr[0] + ".." + arr[1]);
                                        break;
                                    }
                                }
                                break;
                            default:
                                break;
                            }
                        }

                    } catch (Exception e) {
                        ConverterReport.INSTANCE.addErrorRecord(String.format("[JOBSTREAM][JOB=%s][outCondition=%s]%s", js1JobStreamJob.getJob(), oc
                                .getConditionExpression(), e.toString()));
                    }
                }
                if (oc.getOutconditionEvents() != null) {
                    for (OutConditionEvent oe : oc.getOutconditionEvents()) {
                        if (!SOSString.isEmpty(oe.getEvent())) {
                            js1OutEventNames.add(oe.getEvent());
                        }
                    }
                }

            }

            if (returnCodes.size() > 0) {
                if (js7Job.getReturnCodeMeaning() == null) {
                    JobReturnCode rc = new JobReturnCode();
                    rc.setSuccess(String.join(",", returnCodes));
                    js7Job.setReturnCodeMeaning(rc);
                }
            }
        }
    }

    public StandaloneJob getJS1Job() {
        return js1Job;
    }

    public JobStreamJob getJS1JobStreamJob() {
        return js1JobStreamJob;
    }

    public Job getJS7Job() {
        return js7Job;
    }

    public String getJS7JobName() {
        return js7JobName;
    }

    public Path getJS7WorkflowPath() {
        return js7WorkflowPath;
    }

    public void setJS7WorkflowPath(Path val) {
        js7WorkflowPath = val;
    }

    public String getJS7WorkflowName() {
        return js7WorkflowName;
    }

    public void setJS7WorkflowName(String val) {
        js7WorkflowName = val;
    }

    public String getJS7BranchPath() {
        return js7BranchPath;
    }

    public void setJS7BranchPath(String val) {
        js7BranchPath = val;
    }

    public Set<String> getJS1OutEventNames() {
        return js1OutEventNames;
    }

    public Set<String> getJS1InEventNames() {
        return js1InEventNames;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("js7JobName=" + js7JobName);
        if (js7BranchPath != null) {
            sb.append(",js7BranchPath=" + js7BranchPath);
        }

        return sb.toString();
    }

}