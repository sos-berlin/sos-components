package com.sos.jitl.jobs.checklicense.classes;

import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersV;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class CheckLicenseWebserviceExecuter {

    private ApiExecutor apiExecutor;
    private OrderProcessStepLogger logger;

    public CheckLicenseWebserviceExecuter(OrderProcessStepLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    public Js7LicenseInfo getLicence(String accessToken) throws Exception {

        String body = "{}";
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/joc/license", body);
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

        Js7LicenseInfo info = new Js7LicenseInfo();
        info = Globals.objectMapper.readValue(answer, Js7LicenseInfo.class);

        return info;

    }

}
