package com.sos.jitl.jobs.runreports.classes;

import java.util.List;
import java.util.Set;

import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.joc.model.agent.DeployAgents;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.joc.model.reporting.Reports;
import com.sos.joc.model.reporting.ReportsFilter;
import com.sos.joc.model.reporting.RunReports;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class RunReportsWebserviceExecuter {

    private ApiExecutor apiExecutor;
    private OrderProcessStepLogger logger;

    public RunReportsWebserviceExecuter(OrderProcessStepLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    public void generateReports(String accessToken, Set<String> reportPaths) throws Exception {
        RunReports runReports = new RunReports();
        runReports.setReportPaths(reportPaths);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(runReports);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/reporting/reports/run", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        logger.debug("answer=" + answer);

    }

    public Reports getReports(String accessToken, List<Folder> folders) throws Exception {
        ReportsFilter reportsFilter = new ReportsFilter();
        reportsFilter.setFolders(folders);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(reportsFilter);
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/reporting/reports", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            if (apiResponse.getException() != null) {
                throw apiResponse.getException();
            } else {
                throw new Exception(apiResponse.getResponseBody());
            }
        }
        logger.debug("answer=" + answer);
        Reports reports = new Reports();
        reports = Globals.objectMapper.readValue(answer, Reports.class);

        return reports;
    }

}
