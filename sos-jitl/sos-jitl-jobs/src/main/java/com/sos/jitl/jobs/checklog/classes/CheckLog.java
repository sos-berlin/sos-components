package com.sos.jitl.jobs.checklog.classes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.Break;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.ImplicitEnd;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.Prompt;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.inventory.model.instruction.RetryInCatch;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.jitl.jobs.checklog.CheckLogJobArguments;
import com.sos.joc.model.job.RunningTaskLogFilter;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.OrderHistoryItemChildItem;
import com.sos.joc.model.order.OrderHistoryItemChildren;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class CheckLog {

    private OrderProcessStepLogger logger;
    private CheckLogJobArguments args;
    OrderProcessStep<CheckLogJobArguments> step;
    private long checkLogMatchCount = 0;
    private long checkLogGroupCount = 0;
    private long checkLogGroupsMatchesCount = 0;

    private String checkLogMatches = "";
    private String checkLogMatchedGroups = "";
    private boolean matchFound = false;

    public CheckLog(OrderProcessStep<CheckLogJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.logger = step.getLogger();
        this.step = step;
    }

    private String handleInstruction(List<Instruction> instructions, Map<String, Integer> jobCount, Map<String, String> label2Job) {
        String returnValue = "";
        for (Instruction instruction : instructions) {

            switch (instruction.getTYPE()) {
            case EXECUTE_NAMED:
                com.sos.inventory.model.instruction.NamedJob namedJob = (com.sos.inventory.model.instruction.NamedJob) instruction;
                int count = jobCount.getOrDefault(namedJob.getJobName(), 0);
                jobCount.put(namedJob.getJobName(), count + 1);
                label2Job.put(instruction.getLabel(), namedJob.getJobName());
                if (namedJob.getJobName().equals(args.getJob())) {
                    returnValue = instruction.getLabel();
                }
                break;

            case TRY:
                if (!instruction.isRetry()) {
                    com.sos.inventory.model.instruction.TryCatch tryCatch = (com.sos.inventory.model.instruction.TryCatch) instruction;
                    returnValue = handleInstruction(tryCatch.getTry().getInstructions(), jobCount, label2Job);
                } else {
                    com.sos.inventory.model.instruction.RetryCatch retryCatch = (com.sos.inventory.model.instruction.RetryCatch) instruction;
                    returnValue = handleInstruction(retryCatch.getTry().getInstructions(), jobCount, label2Job);
                }
                break;
            case IF:
                com.sos.inventory.model.instruction.IfElse ifElse = (com.sos.inventory.model.instruction.IfElse) instruction;
                returnValue = handleInstruction(ifElse.getThen().getInstructions(), jobCount, label2Job);
                returnValue = handleInstruction(ifElse.getElse().getInstructions(), jobCount, label2Job);
                break;
            case FORK:
                com.sos.inventory.model.instruction.ForkJoin forkJoin = (com.sos.inventory.model.instruction.ForkJoin) instruction;
                for (Branch branch : forkJoin.getBranches())
                    returnValue = handleInstruction(branch.getWorkflow().getInstructions(), jobCount, label2Job);
                break;
            case FORKLIST:
                com.sos.inventory.model.instruction.ForkList forkList = (com.sos.inventory.model.instruction.ForkList) instruction;
                returnValue = handleInstruction(forkList.getWorkflow().getInstructions(), jobCount, label2Job);
                break;
            case LOCK:
                com.sos.inventory.model.instruction.Lock lock = (com.sos.inventory.model.instruction.Lock) instruction;
                returnValue = handleInstruction(lock.getLockedWorkflow().getInstructions(), jobCount, label2Job);
                break;
            case STICKY_SUBAGENT:
                com.sos.inventory.model.instruction.StickySubagent stickySubagent = (com.sos.inventory.model.instruction.StickySubagent) instruction;
                returnValue = handleInstruction(stickySubagent.getSubworkflow().getInstructions(), jobCount, label2Job);
                break;
            case CYCLE:
                com.sos.inventory.model.instruction.Cycle cycle = (com.sos.inventory.model.instruction.Cycle) instruction;
                returnValue = handleInstruction(cycle.getCycleWorkflow().getInstructions(), jobCount, label2Job);
                break;
            case OPTIONS:
                com.sos.inventory.model.instruction.Options options = (com.sos.inventory.model.instruction.Options) instruction;
                returnValue = handleInstruction(options.getBlock().getInstructions(), jobCount, label2Job);
                break;
            default:
                break;
            }
        }
        return returnValue;

    }

    private Long checkJob2LabelAssignment(String accessToken, CheckLogWebserviceExecuter orderStateWebserviceExecuter) throws Exception {
        OrderFilter orderFilter = new OrderFilter();
        orderFilter.setControllerId(step.getControllerId());
        orderFilter.setOrderId(step.getOrderId());
        orderFilter.setOrderId("#2024-09-09#T89463254302-root");
        orderFilter.setControllerId("controller");

        OrderV order = orderStateWebserviceExecuter.getOrder(orderFilter, accessToken);

        WorkflowFilter workflowFilter = new WorkflowFilter();
        workflowFilter.setCompact(false);
        workflowFilter.setControllerId(step.getControllerId());
        workflowFilter.setControllerId("controller");
        workflowFilter.setWorkflowId(order.getWorkflowId());
        Workflow workflow = orderStateWebserviceExecuter.getWorkflow(workflowFilter, accessToken);

        OrderHistoryFilter orderHistoryFilter = new OrderHistoryFilter();
        orderHistoryFilter.setControllerId(step.getControllerId());
        orderHistoryFilter.setControllerId("controller");
        orderHistoryFilter.setOrderId(step.getOrderId());
        orderHistoryFilter.setOrderId("#2024-09-09#T89463254302-root");

        String label = args.getLabel();
        String defaultLabel = "";
        Long taskId = 0L;
        if (workflow != null) {

            Map<String, Integer> jobCount = new HashMap<String, Integer>();
            Map<String, String> label2Job = new HashMap<String, String>();

            defaultLabel = handleInstruction(workflow.getWorkflow().getInstructions(), jobCount, label2Job);

            if (jobCount.get(args.getJob()) == null) {
                throw new Exception("could not find job " + "'" + args.getJob() + " in workflow " + "'" + workflowFilter.getWorkflowId().getPath()
                        + "'" + " version " + "'" + workflowFilter.getWorkflowId().getVersionId() + "'" + " on Controller " + "'" + workflowFilter
                                .getControllerId() + "'");

            }

            if (jobCount.get(args.getJob()) > 1) {
                if (args.getLabel() == null) {

                    throw new Exception("value for <label> not specified, job " + "'" + args.getJob() + " occurs " + jobCount.get(args.getJob())
                            + " times in workflow " + "'" + workflowFilter.getWorkflowId().getPath() + "'" + " version " + "'" + workflowFilter
                                    .getWorkflowId().getVersionId() + "'" + " on Controller " + "'" + workflowFilter.getControllerId() + "'");
                }
                if (label2Job.get(args.getLabel()) == null) {
                    throw new Exception("could not find label " + "'" + args.getLabel() + "'" + ", job " + "'" + args.getJob() + " occurs " + jobCount
                            .get(args.getJob()) + " times in workflow " + "'" + workflowFilter.getWorkflowId().getPath() + "'" + " version " + "'"
                            + workflowFilter.getWorkflowId().getVersionId() + "'" + " on Controller " + "'" + workflowFilter.getControllerId() + "'");
                }
            } else {
                label = defaultLabel;
            }

            if (args.getLabel() != null) {
                if (label2Job.get(args.getLabel()) == null) {
                    throw new Exception("could not find job configured with label " + "'" + args.getLabel() + " in workflow " + "'" + workflowFilter
                            .getWorkflowId().getPath() + "'" + " version " + "'" + workflowFilter.getWorkflowId().getVersionId() + "'"
                            + " on Controller " + "'" + workflowFilter.getControllerId() + "'");
                }
                if (!label2Job.get(args.getLabel()).equals(args.getJob())) {
                    throw new Exception("the label " + "'" + args.getLabel() + "'" + " is assigned to the job " + "'" + label2Job.get(args.getLabel())
                            + "'" + " and not to the job " + "'" + args.getJob() + "'" + " in workflow " + "'" + workflowFilter.getWorkflowId()
                                    .getPath() + "'" + " version " + "'" + workflowFilter.getWorkflowId().getVersionId() + "'" + " on Controller "
                            + "'" + workflowFilter.getControllerId() + "'");
                }
            }

            OrderHistoryItemChildren orderHistoryItemChildren = orderStateWebserviceExecuter.getOrderHistory(orderHistoryFilter, accessToken);
            jobCount.clear();
            label2Job.clear();
            Map<String, Long> label2TaskId = new HashMap<String, Long>();

            for (OrderHistoryItemChildItem orderHistoryItemChildItem : orderHistoryItemChildren.getChildren()) {
                int count = jobCount.getOrDefault(orderHistoryItemChildItem.getTask().getJob(), 0);
                jobCount.put(orderHistoryItemChildItem.getTask().getJob(), count + 1);
                label2Job.put(orderHistoryItemChildItem.getTask().getLabel(), orderHistoryItemChildItem.getTask().getJob());
                label2TaskId.put(orderHistoryItemChildItem.getTask().getLabel(), orderHistoryItemChildItem.getTask().getTaskId());
            }

            if (jobCount.get(args.getJob()) == null) {
                throw new Exception("job " + "'" + args.getJob() + "'" + " was not executed in workflow " + "'" + workflowFilter.getWorkflowId()
                        .getPath() + "'" + " version " + "'" + workflowFilter.getWorkflowId().getVersionId() + "'" + " on Controller "
                        + workflowFilter.getControllerId() + "'");
            }

            if (jobCount.get(args.getJob()) > 1) {
                if (args.getLabel() == null) {
                    throw new Exception("value for <label> not specified, job " + "'" + args.getJob() + "'" + " occurs " + jobCount.get(args.getJob())
                            + " times in workflow " + "'" + workflowFilter.getWorkflowId().getPath() + "'" + " version " + "'" + workflowFilter
                                    .getWorkflowId().getVersionId() + "'" + " on Controller " + "'" + workflowFilter.getControllerId() + "'");
                }
                if (label2Job.get(args.getLabel()) == null) {
                    throw new Exception("job with label " + "'" + args.getLabel() + "'" + " was not executed" + ", job " + "'" + args.getJob() + "'"
                            + " occurs " + jobCount.get(args.getJob()) + " times in the history for the workflow " + "'" + workflowFilter
                                    .getWorkflowId().getPath() + "'" + " version " + "'" + workflowFilter.getWorkflowId().getVersionId() + "'"
                            + " on Controller " + workflowFilter.getControllerId() + "'");
                }
            }

            taskId = label2TaskId.get(label);
        }
        return taskId;
    }

    private void matchLog(String log) {

        int options = 0;
        if (args.getCaseInsensitive()) {
            logger.debug("CASE_INSITIVE");
            options = options + Pattern.CASE_INSENSITIVE;
        }
        if (args.getMultiline()) {
            logger.debug("MULTILINE");
            options = options + Pattern.MULTILINE;
        }
        if (args.getUnixLines()) {
            logger.debug("UNIX_LINES");
            options = options + Pattern.UNIX_LINES;
        }
        Pattern pattern = Pattern.compile(args.getPattern(), options);
        Matcher matcher = pattern.matcher(log);

        checkLogMatchCount = matcher.results().count();
        matchFound = checkLogMatchCount > 0;

        matcher.reset();
        while (matcher.find()) {
            checkLogMatches = checkLogMatches + args.getSeparator() + matcher.group(0);
            logger.debug("match: " + matcher.group(0));
        }
        matcher.reset();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                logger.debug("Group" + i + ":" + matcher.group(i));
                checkLogGroupsMatchesCount = checkLogGroupsMatchesCount + 1;
                checkLogMatchedGroups = checkLogMatchedGroups + args.getSeparator() + matcher.group(i);
            }
        }

        if (checkLogMatchedGroups.startsWith(args.getSeparator())) {
            checkLogMatchedGroups = checkLogMatchedGroups.substring(1);
        }
        if (checkLogMatches.startsWith(args.getSeparator())) {
            checkLogMatches = checkLogMatches.substring(1);
        }
        checkLogGroupCount = matcher.groupCount();
        logger.debug(checkLogMatchCount + " matches");
        logger.debug("matched:" + matchFound);
        logger.debug(checkLogMatches);
        logger.debug("groupCount:" + matcher.groupCount());
        logger.debug(checkLogMatchedGroups);

    }

    public void execute() throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(logger);
        String accessToken = null;
        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();

            CheckLogWebserviceExecuter orderStateWebserviceExecuter = new CheckLogWebserviceExecuter(logger, apiExecutor);
            Long taskId = checkJob2LabelAssignment(accessToken, orderStateWebserviceExecuter);

            RunningTaskLogFilter runningTaskLogFilter = new RunningTaskLogFilter();
            runningTaskLogFilter.setControllerId(step.getControllerId());
            runningTaskLogFilter.setControllerId("controller");
            runningTaskLogFilter.setTaskId(taskId);

            String taskLog = orderStateWebserviceExecuter.getTaskLog(runningTaskLogFilter, accessToken);
            matchLog(taskLog);

        } catch (Exception e) {
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }

    public long getCheckLogMatchCount() {
        return checkLogMatchCount;
    }

    public String getCheckLogMatches() {
        return checkLogMatches;
    }

    public String getCheckLogMatchedGroups() {
        return checkLogMatchedGroups;
    }

    public boolean isMatchFound() {
        return matchFound;
    }

    public long getCheckLogGroupCount() {
        return checkLogGroupCount;
    }

    public long getCheckLogGroupsMatchesCount() {
        return checkLogGroupsMatchesCount;
    }

}
