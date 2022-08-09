package com.sos.joc.classes.inventory.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.ExecutableType;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.Workflow;

public class WorkflowSearcher {

    private final Workflow workflow;
    private List<WorkflowInstruction<? extends Instruction>> instructions;
    private List<WorkflowInstruction<NamedJob>> jobInstructions;
    private List<WorkflowJob> jobs;
    private Map<String, Parameter> orderPreparationParameters;

    public WorkflowSearcher(final Workflow workflow) {
        this.workflow = workflow;
    }

    public class WorkflowInstruction<T extends Instruction> {

        private final String postition;
        private final T instruction;

        public WorkflowInstruction(final String position, final T instruction) {
            this.postition = position;
            this.instruction = instruction;
        }

        public String getPosition() {
            return postition;
        }

        public T getInstruction() {
            return instruction;
        }
    }

    public class WorkflowJob {

        private final String name;
        private final Job job;

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

    public Map<String, Parameter> getOrderPreparationParameters() {
        setAllOrderPreparationParameters();
        return orderPreparationParameters;
    }

    public Map<String, Parameter> getOrderPreparationParameters(String nameRegex) {
        setAllOrderPreparationParameters();
        if (nameRegex == null || orderPreparationParameters.isEmpty()) {
            return orderPreparationParameters;
        }
        return orderPreparationParameters.entrySet().stream().filter(e -> e.getKey().matches(nameRegex)).collect(Collectors.toMap(x -> x.getKey(),
                x -> x.getValue()));
    }

    private void setAllOrderPreparationParameters() {
        if (orderPreparationParameters == null) {
            if (workflow.getOrderPreparation() != null && workflow.getOrderPreparation().getParameters() != null) {
                orderPreparationParameters = workflow.getOrderPreparation().getParameters().getAdditionalProperties();
            }
            if (orderPreparationParameters == null) {
                orderPreparationParameters = new HashMap<String, Parameter>();
            }
        }
    }

    public List<WorkflowJob> getJobs() {
        return getJobs(null);
    }

    public List<WorkflowJob> getJobs(String nameRegex) {
        setAllJobs();
        if (nameRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getName().matches(nameRegex)).collect(Collectors.toList());
    }

    public List<WorkflowJob> getUnusedJobs() {
        List<WorkflowJob> jobs = getJobs();
        if (jobs.isEmpty()) {
            return jobs;
        }
        List<WorkflowInstruction<NamedJob>> namedJobs = getJobInstructions();
        if (namedJobs.isEmpty()) {
            return jobs;
        }
        List<String> namedJobNames = namedJobs.stream().map(j -> j.getInstruction().getJobName()).distinct().collect(Collectors.toList());
        return jobs.stream().filter(j -> !namedJobNames.contains(j.getName())).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByAgentId(String regex) {
        setAllJobs();
        if (regex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getAgentName() != null).filter(j -> j.getJob().getAgentName().matches(regex)).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByJobClass(String regex) {
        setAllJobs();
        if (regex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getJobClassName() != null).filter(j -> j.getJob().getJobClassName().matches(regex)).collect(Collectors.toList());
    }
    
    public List<WorkflowJob> getJobsByJobTemplate(String regex) {
        setAllJobs();
        if (regex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getJobTemplate() != null).filter(j -> j.getJob().getJobTemplate().getName() != null).filter(j -> j.getJob()
                .getJobTemplate().getName().matches(regex)).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByScript(String regex) {
        setAllJobs();
        if (regex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> j.getJob().getExecutable() != null).filter(j -> ExecutableType.ScriptExecutable.equals(j.getJob().getExecutable()
                .getTYPE())).filter(j -> ((ExecutableScript) j.getJob().getExecutable()).getScript().matches(regex)).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByArgument(String nameRegex) {
        setAllJobs();
        if (nameRegex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> getJobArgumentsStream(j.getJob()).anyMatch(a -> a.getKey().matches(nameRegex))).collect(Collectors.toList());
    }

    public List<WorkflowJob> getJobsByArgumentValue(String regex) {
        setAllJobs();
        if (regex == null || jobs.isEmpty()) {
            return jobs;
        }
        return jobs.stream().filter(j -> getJobArgumentsStream(j.getJob()).anyMatch(a -> a.getValue().toString().matches(regex))).collect(Collectors
                .toList());
    }

    public List<WorkflowJob> getJobsByArgumentAndValue(String nameRegex, String valueRegex) {
        setAllJobs();
        if (nameRegex == null || valueRegex == null) {
            if (nameRegex == null && valueRegex == null) {
                return jobs;
            } else if (valueRegex == null) {
                return getJobsByArgument(nameRegex);
            } else {
                return getJobsByArgumentValue(valueRegex);
            }
        }

        return jobs.stream().filter(j -> getJobArgumentsStream(j.getJob()).anyMatch(a -> a.getKey().matches(nameRegex) && a.getValue().toString()
                .matches(valueRegex))).collect(Collectors.toList());
    }

    public WorkflowJob getJob(String nameRegex) {
        setAllJobs();
        if (nameRegex == null || jobs.isEmpty()) {
            return null;
        }
        return jobs.stream().filter(j -> j.getName().matches(nameRegex)).findFirst().orElse(null);
    }

    public Object getJobArgument(String jobNameRegex, String argName) {
        WorkflowJob job = getJob(jobNameRegex);
        if (job == null) {
            return null;
        }
        return getJobArgument(job.getJob(), argName);
    }

    public Object getJobArgument(Job job, String argName) {
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
    
    public List<WorkflowInstruction<? extends Instruction>> getInstructions(InstructionType... types) {
       return getInstructionsStream(types).collect(Collectors.toList());
    }

    public Stream<WorkflowInstruction<? extends Instruction>> getInstructionsStream(InstructionType... types) {
        setAllInstructions();
        if (types.length == 0 || instructions.isEmpty()) {
            return Stream.empty();
        }
        List<InstructionType> typesList = Arrays.stream(types).collect(Collectors.toList());
        return instructions.stream().filter(i -> typesList.contains(i.getInstruction().getTYPE()));
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowInstruction<Lock>> getLockInstructions() {
        return getInstructionsStream(InstructionType.LOCK).map(l -> (WorkflowInstruction<Lock>) l).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowInstruction<PostNotice>> getPostNoticeInstructions() {
        return getInstructionsStream(InstructionType.POST_NOTICE).map(l -> (WorkflowInstruction<PostNotice>) l).collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    public List<WorkflowInstruction<PostNotices>> getPostNoticesInstructions() {
        return getInstructionsStream(InstructionType.POST_NOTICES).map(l -> (WorkflowInstruction<PostNotices>) l).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowInstruction<ExpectNotice>> getExpectNoticeInstructions() {
        return getInstructionsStream(InstructionType.EXPECT_NOTICE).map(l -> (WorkflowInstruction<ExpectNotice>) l).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowInstruction<ExpectNotices>> getExpectNoticesInstructions() {
        return getInstructionsStream(InstructionType.EXPECT_NOTICES).map(l -> (WorkflowInstruction<ExpectNotices>) l).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowInstruction<AddOrder>> getAddOrderInstructions() {
        return getInstructionsStream(InstructionType.ADD_ORDER).map(l -> (WorkflowInstruction<AddOrder>) l).collect(Collectors.toList());
    }

    public List<WorkflowInstruction<Lock>> getLockInstructions(String lockIdRegex) {
        List<WorkflowInstruction<Lock>> r = getLockInstructions();
        if (r.isEmpty() || lockIdRegex == null) {
            return r;
        }
        return r.stream().filter(l -> l.getInstruction().getLockName().matches(lockIdRegex)).collect(Collectors.toList());
    }

    /** filter e.g.:
     * 
     * 1) l -> l.getInstruction().getLockId().equals("myLockId") && l -> l.getInstruction().getCount() != null && l.getInstruction().getCount() > 0
     * 
     * 2) l -> { Lock lock = l.getInstruction(); return lock.getCount() != null && lock.getCount() > 0; }
     * 
     * 3) Predicate<WorkflowInstruction<Lock>> filter = MyLockFilterFunction(1); */
    public List<WorkflowInstruction<Lock>> getLockInstructions(Predicate<? super WorkflowInstruction<Lock>> filter) {
        List<WorkflowInstruction<Lock>> r = getLockInstructions();
        if (r.isEmpty() || filter == null) {
            return r;
        }
        return r.stream().filter(filter).collect(Collectors.toList());
    }

    public List<WorkflowInstruction<NamedJob>> getJobInstructions() {
        setAllJobInstructions();
        return jobInstructions;
    }

    public List<WorkflowInstruction<NamedJob>> getJobInstructions(String jobNameRegex) {
        List<WorkflowInstruction<NamedJob>> r = getJobInstructions();
        if (r.isEmpty() || jobNameRegex == null) {
            return r;
        }
        return r.stream().filter(j -> j.getInstruction().getJobName().matches(jobNameRegex)).collect(Collectors.toList());
    }

    public List<WorkflowInstruction<NamedJob>> getJobInstructionsByLabel(String labelRegex) {
        List<WorkflowInstruction<NamedJob>> r = getJobInstructions();
        if (r.isEmpty() || labelRegex == null) {
            return r;
        }
        return r.stream().filter(j -> j.getInstruction().getLabel().matches(labelRegex)).collect(Collectors.toList());
    }

    public List<WorkflowInstruction<NamedJob>> getJobInstructionsByArgument(String argNameRegex) {
        List<WorkflowInstruction<NamedJob>> r = getJobInstructions();
        if (r.isEmpty() || argNameRegex == null) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j.getInstruction()).anyMatch(en -> en.getKey().matches(argNameRegex))).collect(
                Collectors.toList());
    }

    public List<WorkflowInstruction<NamedJob>> getJobInstructionsByArgumentValue(String argValueRegex) {
        List<WorkflowInstruction<NamedJob>> r = getJobInstructions();
        if (r.isEmpty() || argValueRegex == null) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j.getInstruction()).anyMatch(en -> en.getValue().toString().matches(argValueRegex)))
                .collect(Collectors.toList());
    }

    public List<WorkflowInstruction<NamedJob>> getJobInstructionsByArgumentAndValue(String argNameRegex, String argValueRegex) {
        if (argNameRegex == null || argValueRegex == null) {
            if (argNameRegex == null && argValueRegex == null) {
                return getJobInstructions();
            } else if (argValueRegex == null) {
                return getJobInstructionsByArgument(argNameRegex);
            } else {
                return getJobInstructionsByArgumentValue(argValueRegex);
            }
        }

        List<WorkflowInstruction<NamedJob>> r = getJobInstructions();
        if (r.isEmpty()) {
            return r;
        }
        return r.stream().filter(j -> getNamedJobArgumentsStream(j.getInstruction()).anyMatch(en -> en.getKey().matches(argNameRegex) && en.getValue()
                .toString().matches(argValueRegex))).collect(Collectors.toList());
    }

    private void setAllInstructions() {
        if (instructions == null) {
            instructions = getAllInstructions(workflow.getInstructions());
        }
    }

    private List<WorkflowInstruction<? extends Instruction>> getAllInstructions(List<Instruction> list) {
        List<WorkflowInstruction<? extends Instruction>> result = new ArrayList<WorkflowInstruction<? extends Instruction>>();
        handleInstructions(result, list, "");
        return result == null ? new ArrayList<WorkflowInstruction<? extends Instruction>>() : result;
    }

    @SuppressWarnings("unchecked")
    private void setAllJobInstructions() {
        if (jobInstructions == null) {
            jobInstructions = getInstructionsStream(InstructionType.EXECUTE_NAMED).map(i -> (WorkflowInstruction<NamedJob>) i).collect(Collectors.toList());
        }
    }

    private void setAllJobs() {
        if (jobs == null) {
            Stream<Entry<String, Job>> stream = getJobsStream();
            jobs = stream == null ? new ArrayList<WorkflowJob>() : stream.map(this::map2WorkflowJob).collect(Collectors.toList());
        }
    }

    private void handleInstructions(List<WorkflowInstruction<? extends Instruction>> result, List<Instruction> instructions, String parentPosition) {
        if (instructions == null) {
            return;
        }

        int index = 0;
        for (Instruction in : instructions) {
            InstructionType it = in.getTYPE();
            if (it == null) {
                continue;
            }

            switch (it) {
            case IF:
                IfElse ie = in.cast();
                if (ie.getThen() != null) {
                    String position = getPosition(parentPosition, index, "ifthen");
                    result.add(new WorkflowInstruction<IfElse>(position, ie));

                    handleInstructions(result, ie.getThen().getInstructions(), position);
                }
                if (ie.getElse() != null) {
                    String position = getPosition(parentPosition, index, "ifelse");
                    result.add(new WorkflowInstruction<IfElse>(position, ie));

                    handleInstructions(result, ie.getElse().getInstructions(), position);
                }
                break;
            case LOCK:
                Lock l = in.cast();
                if (l.getLockedWorkflow() != null) {
                    String position = getPosition(parentPosition, index, "lock");
                    result.add(new WorkflowInstruction<Lock>(position, l));

                    handleInstructions(result, l.getLockedWorkflow().getInstructions(), position);
                }
                break;
            case TRY:
                TryCatch tc = in.cast();
                if (tc.getTry() != null) {
                    String position = getPosition(parentPosition, index, "try");
                    result.add(new WorkflowInstruction<TryCatch>(position, tc));

                    handleInstructions(result, tc.getTry().getInstructions(), position);
                }
                if (tc.getCatch() != null) {
                    String position = getPosition(parentPosition, index, "catch");
                    result.add(new WorkflowInstruction<TryCatch>(position, tc));

                    handleInstructions(result, tc.getCatch().getInstructions(), position);
                }
                break;
            case FORK:
                ForkJoin fj = in.cast();
                if (fj.getBranches() != null) {
                    for (Branch branch : fj.getBranches()) {
                        if (branch.getWorkflow() != null) {
                            String position = getPosition(parentPosition, index, "fork+" + branch.getId());
                            result.add(new WorkflowInstruction<ForkJoin>(position, fj));

                            handleInstructions(result, branch.getWorkflow().getInstructions(), position);
                        }
                    }
                }
                break;
            case FORKLIST:
                ForkList fl = in.cast();
                if (fl.getWorkflow() != null) {
                    String position = getPosition(parentPosition, index, "fork");
                    result.add(new WorkflowInstruction<ForkList>(position, fl));

                    handleInstructions(result, fl.getWorkflow().getInstructions(), position);
                }
                break;
            case EXECUTE_NAMED:
                result.add(new WorkflowInstruction<NamedJob>(getPosition(parentPosition, index, null), in.cast()));
                break;
            case POST_NOTICE:
                result.add(new WorkflowInstruction<PostNotice>(getPosition(parentPosition, index, null), in.cast()));
                break;
            case EXPECT_NOTICE:
                result.add(new WorkflowInstruction<ExpectNotice>(getPosition(parentPosition, index, null), in.cast()));
                break;
            case POST_NOTICES:
                result.add(new WorkflowInstruction<PostNotices>(getPosition(parentPosition, index, null), in.cast()));
                break;
            case EXPECT_NOTICES:
                result.add(new WorkflowInstruction<ExpectNotices>(getPosition(parentPosition, index, null), in.cast()));
                break;
            case ADD_ORDER:
                result.add(new WorkflowInstruction<AddOrder>(getPosition(parentPosition, index, null), in.cast()));
                break;
            case CYCLE:
                Cycle c = in.cast();
                if (c.getCycleWorkflow() != null) {
                    String position = getPosition(parentPosition, index, "cycle");
                    result.add(new WorkflowInstruction<Cycle>(position, c));

                    handleInstructions(result, c.getCycleWorkflow().getInstructions(), position);
                }
                break;
            
            default:

                break;
            }
            index += 1;
        }
    }

    private String getPosition(String parentPosition, int index, String position) {
        StringBuilder sb = new StringBuilder();
        if (!SOSString.isEmpty(parentPosition)) {
            sb.append(parentPosition).append("/");
        }
        sb.append(String.valueOf(index));
        if (!SOSString.isEmpty(position)) {
            sb.append("/").append(position);
        }
        return sb.toString();
    }

    private Stream<Entry<String, Job>> getJobsStream() {
        return workflow.getJobs() == null ? null : workflow.getJobs().getAdditionalProperties().entrySet().stream();
    }

    private Stream<Entry<String, String>> getJobArgumentsStream(Job job) {
        if (job == null || job.getDefaultArguments() == null) {
            return Stream.empty(); // new HashSet<Entry<String, String>>().stream();
        }
        return job.getDefaultArguments().getAdditionalProperties().entrySet().stream();
    }

    private Stream<Entry<String, String>> getNamedJobArgumentsStream(NamedJob job) {
        if (job == null || job.getDefaultArguments() == null) {
            return Stream.empty(); // new HashSet<Entry<String, String>>().stream();
        }
        return job.getDefaultArguments().getAdditionalProperties().entrySet().stream();
    }

    private WorkflowJob map2WorkflowJob(Entry<String, Job> entry) {
        return new WorkflowJob(entry.getKey(), entry.getValue());
    }

}
