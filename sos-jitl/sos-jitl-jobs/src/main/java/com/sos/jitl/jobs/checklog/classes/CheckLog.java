package com.sos.jitl.jobs.checklog.classes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
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

    private Long checkJob2LabelAssignment(String accessToken, CheckLogWebserviceExecuter orderStateWebserviceExecuter) throws Exception {
        OrderFilter orderFilter = new OrderFilter();
        orderFilter.setControllerId(step.getControllerId());
        orderFilter.setOrderId(step.getOrderId());
        OrderV order = orderStateWebserviceExecuter.getOrder(orderFilter, accessToken);

        WorkflowFilter workflowFilter = new WorkflowFilter();
        workflowFilter.setCompact(false);
        workflowFilter.setControllerId(step.getControllerId());
        workflowFilter.setWorkflowId(order.getWorkflowId());
        Workflow workflow = orderStateWebserviceExecuter.getWorkflow(workflowFilter, accessToken);

        OrderHistoryFilter orderHistoryFilter = new OrderHistoryFilter();
        orderHistoryFilter.setControllerId(step.getControllerId());
        orderHistoryFilter.setOrderId(step.getOrderId());

        String label = args.getLabel();
        String defaultLabel = "";
        Long taskId = 0L;
        if (workflow != null) {
            Map<String, Integer> jobCount = new HashMap<String, Integer>();
            Map<String, String> label2Job = new HashMap<String, String>();
            for (Instruction instruction : workflow.getWorkflow().getInstructions()) {
                if (instruction.getTYPE().equals(InstructionType.EXECUTE_NAMED)) {
                    com.sos.inventory.model.instruction.NamedJob namedJob = (com.sos.inventory.model.instruction.NamedJob) instruction;
                    int count = jobCount.getOrDefault(namedJob.getJobName(), 0);
                    jobCount.put(namedJob.getJobName(), count + 1);
                    label2Job.put(instruction.getLabel(), namedJob.getJobName());
                    if (namedJob.getJobName().equals(args.getJob())) {
                        defaultLabel = instruction.getLabel();
                    }
                }
            }

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
