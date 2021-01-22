package com.sos.joc.classes.inventory.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.jobscheduler.model.instruction.ForkJoin;
import com.sos.jobscheduler.model.instruction.IfElse;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.InstructionType;
import com.sos.jobscheduler.model.instruction.Lock;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.instruction.TryCatch;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.workflow.Branch;
import com.sos.jobscheduler.model.workflow.Workflow;

public class WorkflowSearcher {

    private final Workflow workflow;
    private List<Instruction> instructions;
    private List<NamedJob> jobInstructions;
    private List<WorkflowJob> jobs;

    public WorkflowSearcher(final Workflow workflow) {
        this.workflow = workflow;
    }

    public class WorkflowJob {

        private String name;
        private Job job;

        public WorkflowJob(final String name, final Job job) {
            this.name = name;
            this.job = job;
        }

        public String getName() {
            return name;
        }

        public Job getJob() {
            return job;
        }
    }

    public List<WorkflowJob> getJobs() {
        return getJobs(null);
    }

    public List<WorkflowJob> getJobs(String jobNameRegex) {
        setAllJobs();
        if (jobNameRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getName().matches(jobNameRegex)).collect(Collectors.toList());
    }

    public List<WorkflowJob> getUnusedJobs() {
        List<WorkflowJob> jobs = getJobs();
        if (jobs.isEmpty()) {
            return jobs;
        }
        List<NamedJob> namedJobs = getJobInstructions();
        if (namedJobs.isEmpty()) {
            return jobs;
        }
        List<String> namedJobNames = namedJobs.stream().map(j -> j.getJobName()).distinct().collect(Collectors.toList());
        return jobs.stream().filter(j -> !namedJobNames.contains(j.getName())).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByAgentId(String agentIdRegex) {
        setAllJobs();
        if (agentIdRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getAgentId() != null && j.getJob().getAgentId().matches(agentIdRegex)).collect(Collectors
                .toList());
    }

    public List<WorkflowJob> getJobsByJobClass(String jobClassRegex) {
        setAllJobs();
        if (jobClassRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getJobClass() != null && j.getJob().getJobClass().matches(jobClassRegex)).collect(Collectors
                .toList());
    }

    public List<WorkflowJob> getJobsByScript(String scriptRegex) {
        setAllJobs();
        if (scriptRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getExecutable() != null && j.getJob().getExecutable().getScript() != null && j.getJob()
                .getExecutable().getScript().matches(scriptRegex)).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByArgument(String argNameRegex) {
        setAllJobs();
        if (argNameRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> getJobArgumentsStream(j.getJob()).anyMatch(a -> a.getKey().matches(argNameRegex))).collect(Collectors
                .toList());
    }

    public List<WorkflowJob> getJobsByArgumentValue(String argValueRegex) {
        setAllJobs();
        if (argValueRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> getJobArgumentsStream(j.getJob()).anyMatch(a -> a.getValue().matches(argValueRegex))).collect(Collectors
                .toList());
    }

    public List<WorkflowJob> getJobsByArgumentAndValue(String argNameRegex, String argValueRegex) {
        setAllJobs();
        if (argNameRegex == null || argValueRegex == null) {
            if (argNameRegex == null && argValueRegex == null) {
                return jobs;
            } else if (argValueRegex == null) {
                return getJobsByArgument(argNameRegex);
            } else {
                return getJobsByArgumentValue(argValueRegex);
            }
        }

        return jobs.stream().filter(j -> getJobArgumentsStream(j.getJob()).anyMatch(a -> a.getKey().matches(argNameRegex) && a.getValue().matches(
                argValueRegex))).collect(Collectors.toList());
    }

    public WorkflowJob getJob(String jobName) {
        setAllJobs();
        if (jobName == null || jobs.isEmpty()) {
            return null;
        }
        return jobs.stream().filter(j -> j.getName().matches(jobName)).findFirst().orElse(null);
    }

    public String getJobArgument(String jobName, String argName) {
        WorkflowJob job = getJob(jobName);
        if (job == null) {
            return null;
        }
        return getJobArgument(job.getJob(), argName);
    }

    public String getJobArgument(Job job, String argName) {
        if (job == null || argName == null) {
            return null;
        }
        return getJobArgumentsStream(job).filter(e -> e.getKey().equals(argName)).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    public Map<String, String> getJobArguments(String jobName) {
        return getJobArguments(jobName, null);
    }

    public Map<String, String> getJobArguments(String jobName, String argNameRegex) {
        WorkflowJob job = getJob(jobName);
        if (job == null) {
            return null;
        }
        return getJobArguments(job.getJob(), argNameRegex);
    }

    public Map<String, String> getJobArguments(Job job) {
        return getJobArguments(job, null);
    }

    public Map<String, String> getJobArguments(Job job, String argNameRegex) {
        if (job == null) {
            return null;
        }
        if (argNameRegex == null) {
            return job.getDefaultArguments().getAdditionalProperties();
        }
        return getJobArgumentsStream(job).filter(e -> e.getKey().matches(argNameRegex)).collect(Collectors.toMap(map -> map.getKey(), map -> map
                .getValue()));
    }

    public List<Instruction> getInstructions(InstructionType... types) {
        setAllInstructions();
        if (types.length == 0 || instructions.isEmpty()) {
            return instructions;
        }
        List<InstructionType> typesList = Arrays.stream(types).collect(Collectors.toList());
        return instructions.stream().filter(i -> typesList.contains(i.getTYPE())).collect(Collectors.toList());
    }

    public List<Lock> getLockInstructions() {
        List<Instruction> r = getInstructions(InstructionType.LOCK);
        if (r.isEmpty()) {
            return new ArrayList<Lock>();
        }
        return r.stream().map(i -> (Lock) i).collect(Collectors.toList());
    }

    public List<Lock> getLockInstructions(String lockIdRegex) {
        List<Lock> r = getLockInstructions();
        if (r.isEmpty() || lockIdRegex == null) {
            return r;
        }
        return r.stream().filter(l -> l.getLockId().matches(lockIdRegex)).collect(Collectors.toList());
    }

    /** filter e.g.: l -> l.getLockId().equals("myLockId") && l.getCount() != null && l.getCount() > 10 */
    public List<Lock> getLockInstructions(Predicate<? super Lock> filter) {
        List<Lock> r = getLockInstructions();
        if (r.isEmpty() || filter == null) {
            return r;
        }
        return r.stream().filter(filter).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructions() {
        setAllJobInstructions();
        return jobInstructions;
    }

    public List<NamedJob> getJobInstructions(String jobNameRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r.isEmpty() || jobNameRegex == null) {
            return r;
        }
        return r.stream().filter(j -> j.getJobName().matches(jobNameRegex)).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructionsByLabel(String labelRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r.isEmpty() || labelRegex == null) {
            return r;
        }
        return r.stream().filter(j -> j.getLabel().matches(labelRegex)).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructionsByArgument(String argNameRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r.isEmpty() || argNameRegex == null) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j).anyMatch(en -> en.getKey().matches(argNameRegex))).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructionsByArgumentValue(String argValueRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r.isEmpty() || argValueRegex == null) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j).anyMatch(en -> en.getValue().matches(argValueRegex))).collect(Collectors
                .toList());
    }

    public List<NamedJob> getJobInstructionsByArgumentAndValue(String argNameRegex, String argValueRegex) {
        if (argNameRegex == null || argValueRegex == null) {
            if (argNameRegex == null && argValueRegex == null) {
                return getJobInstructions();
            } else if (argValueRegex == null) {
                return getJobInstructionsByArgument(argNameRegex);
            } else {
                return getJobInstructionsByArgumentValue(argValueRegex);
            }
        }

        List<NamedJob> r = getJobInstructions();
        if (r.isEmpty()) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j).anyMatch(en -> en.getKey().matches(argNameRegex) && en.getValue().matches(
                argValueRegex))).collect(Collectors.toList());
    }

    private void setAllInstructions() {
        if (instructions == null) {
            instructions = getAllInstructions(workflow.getInstructions());
        }
    }

    private List<Instruction> getAllInstructions(List<Instruction> list) {
        List<Instruction> result = new ArrayList<Instruction>();
        handleInstructions(result, list);
        return result == null ? new ArrayList<Instruction>() : result;
    }

    private void setAllJobInstructions() {
        if (jobInstructions == null) {
            List<Instruction> r = getInstructions(InstructionType.EXECUTE_NAMED);
            if (r.size() == 0) {
                jobInstructions = new ArrayList<NamedJob>();
            } else {
                jobInstructions = r.stream().map(i -> (NamedJob) i).collect(Collectors.toList());
            }
        }
    }

    public void setAllJobs() {
        if (jobs == null) {
            Stream<Entry<String, Job>> stream = getJobsStream();
            jobs = stream == null ? new ArrayList<WorkflowJob>() : stream.map(this::map2WorkflowJob).collect(Collectors.toList());
        }
    }

    private void handleInstructions(List<Instruction> result, List<Instruction> instructions) {
        if (instructions == null) {
            return;
        }

        for (Instruction in : instructions) {
            InstructionType it = in.getTYPE();
            if (it == null) {
                continue;
            }
            result.add(in.cast());
            switch (it) {
            case IF:
                IfElse ie = in.cast();
                if (ie.getThen() != null) {
                    handleInstructions(result, ie.getThen().getInstructions());
                }
                if (ie.getElse() != null) {
                    handleInstructions(result, ie.getElse().getInstructions());
                }
                break;
            case LOCK:
                Lock l = in.cast();
                if (l.getLockedWorkflow() != null) {
                    handleInstructions(result, l.getLockedWorkflow().getInstructions());
                }
                break;
            case TRY:
                TryCatch tc = in.cast();
                if (tc.getTry() != null) {
                    handleInstructions(result, tc.getTry().getInstructions());
                }
                if (tc.getCatch() != null) {
                    handleInstructions(result, tc.getCatch().getInstructions());
                }
                break;
            case FORK:
                ForkJoin fj = in.cast();
                if (fj.getBranches() != null) {
                    for (Branch branch : fj.getBranches()) {
                        if (branch.getWorkflow() != null) {
                            handleInstructions(result, branch.getWorkflow().getInstructions());
                        }
                    }
                }
                break;
            default:
                break;
            }
        }
    }

    private Stream<Entry<String, Job>> getJobsStream() {
        return workflow.getJobs() == null ? null : workflow.getJobs().getAdditionalProperties().entrySet().stream();
    }

    private Stream<Entry<String, String>> getJobArgumentsStream(Job job) {
        if (job == null || job.getDefaultArguments() == null) {
            return new HashSet<Entry<String, String>>().stream();
        }
        return job.getDefaultArguments().getAdditionalProperties().entrySet().stream();
    }

    private Stream<Entry<String, String>> getNamedJobArgumentsStream(NamedJob job) {
        if (job == null || job.getDefaultArguments() == null) {
            return new HashSet<Entry<String, String>>().stream();
        }
        return job.getDefaultArguments().getAdditionalProperties().entrySet().stream();
    }

    private WorkflowJob map2WorkflowJob(Entry<String, Job> entry) {
        return new WorkflowJob(entry.getKey(), entry.getValue());
    }

}
