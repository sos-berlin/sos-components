package com.sos.jitl.jobs.checklicense.classes;

import java.util.Date;
import java.util.Map;

import com.sos.jitl.jobs.checklicense.CheckLicenseJobArguments;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class CheckLicense {

    private OrderProcessStepLogger logger;
    private CheckLicenseJobArguments args;
    private int exit;
    private String body;
    private String subject;
    private Map<String, DetailValue> jobResources;


    public CheckLicense(OrderProcessStep<CheckLicenseJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.jobResources = step.getJobResourcesArgumentsAsNameDetailValueMap();
        this.logger = step.getLogger();
    }

    private void log(String s) {
        this.logger.info(s);
        body += s + "\r\n";
    }

    public void execute() throws Exception {
        ApiExecutor apiExecutor = new ApiExecutor(logger);
        apiExecutor.setJobResources(jobResources);
        String accessToken = null;

        exit = 0;
        body = "";

        subject = "";
        try {
            ApiResponse apiResponse = apiExecutor.login();
            accessToken = apiResponse.getAccessToken();
            CheckLicenseWebserviceExecuter checkLicenceWebserviceExecuter = new CheckLicenseWebserviceExecuter(logger, apiExecutor);
            Js7LicenseInfo js7LicenseInfo = checkLicenceWebserviceExecuter.getLicence(accessToken);
            log(".. Check License for validity period of " + args.getValidityDays().shortValue() + " days");
            log(".. Licence tpye: " + js7LicenseInfo.getType());

            if (js7LicenseInfo.getValid() != null && js7LicenseInfo.getValid()) {
                subject = "JS7 JobScheduler License Check: " + js7LicenseInfo.getType();
                log(".. License valid: " + js7LicenseInfo.getValid());
                if (js7LicenseInfo.getValidFrom() != null && js7LicenseInfo.getValidUntil() != null) {
                    Date now = new Date();
                    log(".. License valid from: " + js7LicenseInfo.getValidFrom());
                    log(".. License valid to: " + js7LicenseInfo.getValidUntil());

                    if (js7LicenseInfo.getValidUntil().before(now)) {
                        log("License Check failed: license expired on " + js7LicenseInfo.getValidUntil());
                        subject = "JS7 JobScheduler Notification: license expired";
                        exit = 2;
                    }

                    long timeUntilExpiration = (js7LicenseInfo.getValidUntil().getTime() - now.getTime()) / (1000 * 24 * 60 * 60);
                    long daysMs = args.getValidityDays();

                    logger.debug("now: " + now.getTime());
                    logger.debug("daysMs: " + daysMs);
                    logger.debug("timeUntilExpiration: " + timeUntilExpiration);
                    if (timeUntilExpiration < daysMs) {
                        log("License Check warning: license will expire on " + js7LicenseInfo.getValidUntil());
                        subject = "JS7 JobScheduler Notification: license expiration warning";
                        exit = 3;
                    }
                }
            } else {
                log(".. License valid not applicable");
                log("License Check failed: license check not applicable for open source license");
                subject = "JS7 JobScheduler License Check failed";
                exit = 2;
            }

        } catch (Exception e) {
            logger.error(e);
            exit = 4;
            throw e;
        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();
        }
    }

    public int getExit() {
        return exit;
    }

    public String getBody() {
        return body;
    }

    public String getSubject() {
        return subject;
    }

}
