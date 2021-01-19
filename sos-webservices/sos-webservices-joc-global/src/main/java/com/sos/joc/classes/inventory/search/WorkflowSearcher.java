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
        if (jobNameRegex == null) {
            return getJobsStream().map(this::map2WorkflowJob).collect(Collectors.toList());
        }
        return toWorkflowJobList(getJobsStream().filter(e -> e.getKey().matches(jobNameRegex)));
    }

    public List<WorkflowJob> getUnusedJobs() {
        List<WorkflowJob> jobs = getJobs();
        if (jobs == null || jobs.isEmpty()) {
            return jobs;
        }
        List<NamedJob> namedJobs = getJobInstructions();
        if (namedJobs == null || namedJobs.isEmpty()) {
            return jobs;
        }
        List<String> namedJobNames = namedJobs.stream().map(j -> j.getJobName()).distinct().collect(Collectors.toList());
        return jobs.stream().filter(j -> !namedJobNames.contains(j.getName())).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByAgentId(String agentIdRegex) {
        if (agentIdRegex == null) {
            return getJobs();
        }
        return toWorkflowJobList(getJobsStream().filter(e -> e.getValue().getAgentId() != null && e.getValue().getAgentId().matches(agentIdRegex)));
    }

    public List<WorkflowJob> getJobsByJobClass(String jobClassRegex) {
        if (jobClassRegex == null) {
            return getJobs();
        }
        return toWorkflowJobList(getJobsStream().filter(e -> e.getValue().getJobClass() != null && e.getValue().getJobClass().matches(
                jobClassRegex)));
    }

    public List<WorkflowJob> getJobsByScript(String scriptRegex) {
        if (scriptRegex == null) {
            return getJobs();
        }
        return toWorkflowJobList(getJobsStream().filter(e -> e.getValue().getExecutable() != null && e.getValue().getExecutable().getScript().matches(
                scriptRegex)));
    }

    public List<WorkflowJob> getJobsByArgument(String argNameRegex) {
        if (argNameRegex == null) {
            return getJobs();
        }
        return toWorkflowJobList(getJobsStream().filter(e -> getJobArgumentsStream(e.getValue()).anyMatch(en -> en.getKey().matches(argNameRegex))));
    }

    public List<WorkflowJob> getJobsByArgumentValue(String argValueRegex) {
        if (argValueRegex == null) {
            return getJobs();
        }
        return toWorkflowJobList(getJobsStream().filter(e -> getJobArgumentsStream(e.getValue()).anyMatch(en -> en.getValue().matches(
                argValueRegex))));
    }

    public List<WorkflowJob> getJobsByArgumentAndValue(String argNameRegex, String argValueRegex) {
        if (argNameRegex == null || argValueRegex == null) {
            if (argNameRegex == null && argValueRegex == null) {
                return getJobs();
            } else if (argValueRegex == null) {
                return getJobsByArgument(argNameRegex);
            } else {
                return getJobsByArgumentValue(argValueRegex);
            }
        }
        return toWorkflowJobList(getJobsStream().filter(e -> getJobArgumentsStream(e.getValue()).anyMatch(en -> en.getKey().matches(argNameRegex)
                && en.getValue().matches(argValueRegex))));
    }

    public WorkflowJob getJob(String jobName) {
        return getJobsStream().filter(e -> e.getKey().equals(jobName)).map(this::map2WorkflowJob).findFirst().orElse(null);
    }

    public String getJobArgument(String jobName, String argName) {
        WorkflowJob job = getJob(jobName);
        if (job != null) {
            return getJobArgument(job.getJob(), argName);
        }
        return null;
    }

    public String getJobArgument(Job job, String argName) {
        return getJobArgumentsStream(job).filter(e -> e.getKey().equals(argName)).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    public Map<String, String> getJobArguments(String jobName) {
        return getJobArguments(jobName, null);
    }

    public Map<String, String> getJobArguments(String jobName, String argNameRegex) {
        WorkflowJob job = getJob(jobName);
        if (job != null) {
            return getJobArguments(job.getJob(), argNameRegex);
        }
        return null;
    }

    public Map<String, String> getJobArguments(Job job) {
        return getJobArguments(job, null);
    }

    public Map<String, String> getJobArguments(Job job, String argNameRegex) {
        if (argNameRegex == null) {
            return job.getDefaultArguments().getAdditionalProperties();
        }
        return getJobArgumentsStream(job).filter(e -> e.getKey().matches(argNameRegex)).collect(Collectors.toMap(map -> map.getKey(), map -> map
                .getValue()));
    }

    public List<Instruction> getInstructions(InstructionType... types) {
        return getInstructions(workflow.getInstructions(), types);
    }

    public List<Instruction> getInstructions(List<Instruction> list, InstructionType... types) {
        List<Instruction> result = new ArrayList<Instruction>();
        List<InstructionType> typesList = Arrays.stream(types).collect(Collectors.toList());
        handleInstructions(result, list, typesList.size() == 0 ? null : typesList);
        return result;
    }

    public List<Lock> getLockInstructions() {
        List<Instruction> r = getInstructions(InstructionType.LOCK);
        if (r == null) {
            return null;
        }
        return r.stream().map(i -> (Lock) i).collect(Collectors.toList());
    }

    public List<Lock> getLockInstructions(String lockIdRegex) {
        List<Lock> r = getLockInstructions();
        if (r == null || r.isEmpty() || lockIdRegex == null) {
            return r;
        }
        return r.stream().filter(l -> l.getLockId().matches(lockIdRegex)).collect(Collectors.toList());
    }

    public List<Lock> getLockInstructions(Predicate<? super Lock> filter) {
        List<Lock> r = getLockInstructions();
        if (r == null || r.isEmpty() || filter == null) {
            return r;
        }
        return r.stream().filter(filter).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructions() {
        List<Instruction> r = getInstructions(InstructionType.EXECUTE_NAMED);
        if (r == null) {
            return null;
        }
        return r.stream().map(i -> (NamedJob) i).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructions(String jobNameRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r == null || r.isEmpty() || jobNameRegex == null) {
            return r;
        }
        return r.stream().filter(j -> j.getJobName().matches(jobNameRegex)).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructionsByLabel(String labelRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r == null || r.isEmpty() || labelRegex == null) {
            return r;
        }
        return r.stream().filter(j -> j.getLabel().matches(labelRegex)).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructionsByArgument(String argNameRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r == null || r.isEmpty() || argNameRegex == null) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j).anyMatch(en -> en.getKey().matches(argNameRegex))).collect(Collectors.toList());
    }

    public List<NamedJob> getJobInstructionsByArgumentValue(String argValueRegex) {
        List<NamedJob> r = getJobInstructions();
        if (r == null || r.isEmpty() || argValueRegex == null) {
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
        if (r == null || r.isEmpty()) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j).anyMatch(en -> en.getKey().matches(argNameRegex) && en.getValue().matches(
                argValueRegex))).collect(Collectors.toList());
    }

    private void handleInstructions(List<Instruction> result, List<Instruction> instructions, List<InstructionType> types) {
        if (instructions == null) {
            return;
        }

        for (Instruction in : instructions) {
            InstructionType it = in.getTYPE();
            if (it == null) {
                continue;
            }
            if (types == null) {
                result.add(in.cast());
            } else if (types.contains(it)) {
                result.add(in.cast());
            }
            switch (it) {
            case IF:
                IfElse ie = in.cast();
                if (ie.getThen() != null) {
                    handleInstructions(result, ie.getThen().getInstructions(), types);
                }
                if (ie.getElse() != null) {
                    handleInstructions(result, ie.getElse().getInstructions(), types);
                }
                break;
            case LOCK:
                Lock l = in.cast();
                if (l.getLockedWorkflow() != null) {
                    handleInstructions(result, l.getLockedWorkflow().getInstructions(), types);
                }
                break;
            case TRY:
                TryCatch tc = in.cast();
                if (tc.getTry() != null) {
                    handleInstructions(result, tc.getTry().getInstructions(), types);
                }
                if (tc.getCatch() != null) {
                    handleInstructions(result, tc.getCatch().getInstructions(), types);
                }
                break;
            case FORK:
                ForkJoin fj = in.cast();
                if (fj.getBranches() != null) {
                    for (Branch branch : fj.getBranches()) {
                        if (branch.getWorkflow() != null) {
                            handleInstructions(result, branch.getWorkflow().getInstructions(), types);
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
        return workflow.getJobs().getAdditionalProperties().entrySet().stream();
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

    private List<WorkflowJob> toWorkflowJobList(Stream<Entry<String, Job>> stream) {
        return stream.map(this::map2WorkflowJob).collect(Collectors.toList());
    }

    private WorkflowJob map2WorkflowJob(Entry<String, Job> entry) {
        return new WorkflowJob(entry.getKey(), entry.getValue());
    }

}
