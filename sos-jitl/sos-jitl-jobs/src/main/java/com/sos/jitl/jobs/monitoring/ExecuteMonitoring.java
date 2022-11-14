package com.sos.jitl.jobs.monitoring;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringChecker;
import com.sos.jitl.jobs.monitoring.classes.MonitoringParameters;
import com.sos.jitl.jobs.monitoring.classes.MonitoringWebserviceExecuter;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.jitl.monitoring.MonitoringControllerStatus;
import com.sos.joc.model.jitl.monitoring.MonitoringJocStatus;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;
import com.sos.joc.model.order.OrdersHistoricSummary;
import com.sos.joc.model.order.OrdersSummary;

public class ExecuteMonitoring {

    private MonitoringJobArguments args;
    private JobLogger logger;

    public ExecuteMonitoring(JobLogger logger, MonitoringJobArguments args) {
        this.args = args;
        this.logger = logger;
    }

    public MonitoringStatus getStatusInformations() throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(logger);
        String accessToken = null;
        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();
            
            MonitoringWebserviceExecuter monitoringWebserviceExecuter = new MonitoringWebserviceExecuter(logger, apiExecutor);
            MonitoringControllerStatus monitoringControllerStatus = monitoringWebserviceExecuter.getControllerStatus(accessToken, args
                    .getControllerId());
            MonitoringJocStatus monitoringJocStatus = monitoringWebserviceExecuter.getJS7JOCInstance(accessToken, args.getControllerId());
            List<AgentV> agentStatus = monitoringWebserviceExecuter.getJS7AgentStatus(accessToken, args.getControllerId());
            OrdersSummary ordersSummary = monitoringWebserviceExecuter.getJS7OrderSnapshot(accessToken, args.getControllerId());
            OrdersHistoricSummary ordersHistoricSummary = monitoringWebserviceExecuter.getJS7OrderSummary(accessToken, args.getControllerId());

            MonitoringStatus monitoringStatus = new MonitoringStatus();
            monitoringStatus.setAgentStatus(agentStatus);
            monitoringStatus.setControllerStatus(monitoringControllerStatus);
            monitoringStatus.setFrom(args.getFrom());
            monitoringStatus.setJocStatus(monitoringJocStatus);
            monitoringStatus.setOrderSnapshot(ordersSummary);
            monitoringStatus.setOrderSummary(ordersHistoricSummary);

            return monitoringStatus;
        } catch (Exception e) {
            Globals.error(logger, "", e);
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }

    private List<Path> getSortedDirectory(String dir) throws IOException {
        try (final Stream<Path> fileStream = Files.list(Paths.get(dir))) {
            return fileStream.map(Path::toFile).sorted(Comparator.comparing(File::lastModified)).map(File::toPath).collect(Collectors.toList());
        }
    }

    public MonitoringParameters result2File(MonitoringStatus monitoringStatus, MonitoringParameters monitoringParameters, Integer resultCount)
            throws IOException {
        Files.createDirectories(Paths.get(args.getMonitorReportDir()));

        String filename;
        if (resultCount == 0) {
            filename = args.getMonitorReportDir() + "/monitor." + monitoringParameters.getMonitorFileReportDate() + ".notice.json";
        } else {
            filename = args.getMonitorReportDir() + "/monitor." + monitoringParameters.getMonitorFileReportDate() + ".alert.json";
        }
        monitoringParameters.setMonitorReportFile(filename);
        Globals.debug(logger, "Report Filename: " + filename);

        String output = Globals.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(monitoringStatus);
        try (PrintWriter outWriter = new PrintWriter(filename)) {
            Globals.log(logger, output);
            outWriter.println(output);
        } catch (Exception e) {
            Globals.error(logger, "", e);
        }

        long count = 0;
        try (Stream<Path> files = Files.list(Paths.get(args.getMonitorReportDir()))) {
            count = files.count();
        }
        if (count > args.getMonitorReportMaxFiles()) {
            List<Path> reportfiles = getSortedDirectory(args.getMonitorReportDir());
            for (int i = 0; i < args.getMonitorReportMaxFiles() - 1; i++) {
                if (count > args.getMonitorReportMaxFiles()) {
                    Files.delete(reportfiles.get(i));
                    count -= 1;
                } else {
                    break;
                }
            }
        }

        return monitoringParameters;
    }

    public MonitoringCheckReturn checkStatusInformation(MonitoringStatus monitoringStatus, MonitoringParameters monitoringParameters)
            throws SOSException {
        MonitoringChecker montitoringChecker = new MonitoringChecker(this.logger);
        return montitoringChecker.doCheck(monitoringStatus, monitoringParameters, args.getFrom());
    }

}