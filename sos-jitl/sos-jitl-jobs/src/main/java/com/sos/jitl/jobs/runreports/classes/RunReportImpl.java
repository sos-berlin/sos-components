package com.sos.jitl.jobs.runreports.classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.jitl.jobs.runreports.RunReportJobArguments;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.reporting.Report;
import com.sos.joc.model.reporting.Reports;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class RunReportImpl {

    private OrderProcessStepLogger logger;
    private RunReportJobArguments args;
    private Map<String, DetailValue> jobResources;

    public RunReportImpl(OrderProcessStep<RunReportJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.jobResources = step.getJobResourcesArgumentsAsNameDetailValueMap();
        this.logger = step.getLogger();
    }

    public void execute() throws Exception {
        ApiExecutor apiExecutor = new ApiExecutor(logger);
        apiExecutor.setJobResources(jobResources);

        String accessToken = null;

        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();
            RunReportsWebserviceExecuter runReportsWebserviceExecuter = new RunReportsWebserviceExecuter(logger, apiExecutor);
            Set<String> reportPaths = new HashSet<String>();
            if (logger.isDebugEnabled() && args.getReportPaths() != null  && args.getReportPaths().getValue() != null) {
                for (String report : args.getReportPaths().getValue()) {
                    logger.debug("Add report from reportPaths: " + report);
                }
            }
            if (args.getReportPaths().getValue() != null) {
                reportPaths.addAll(args.getReportPaths().getValue());
            }

            List<Folder> folders = new ArrayList<Folder>();

            if ((args.getReportFolders().getValue() == null || args.getReportFolders().getValue().size() == 0) && (args.getReportPaths()
                    .getValue() == null || args.getReportPaths().getValue().size() == 0)) {
                Folder folder = new Folder();
                folder.setFolder("/");
                folder.setRecursive(true);
                folders.add(folder);
            } else {
                if (args.getReportFolders().getValue() != null) {
                    for (String inFolder : args.getReportFolders().getValue()) {
                        Folder folder = new Folder();
                        boolean recursive = false;
                        logger.debug("Add reports /*: ");

                        if (inFolder.endsWith("/*")) {
                            recursive = true;
                            inFolder = inFolder.substring(0, inFolder.length() - 2);
                        }
                        folder.setFolder(inFolder);
                        folder.setRecursive(recursive);
                        folders.add(folder);
                    }
                }
            }

            if (folders.size() > 0) {
                Reports reports = runReportsWebserviceExecuter.getReports(accessToken, folders);
                for (Report report : reports.getReports()) {
                    logger.debug("Add report from folder: " + report.getPath());
                    reportPaths.add(report.getPath());
                }
            }

            runReportsWebserviceExecuter.generateReports(accessToken, reportPaths);

        } catch (

        Exception e) {
            logger.error(e);
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }

}
