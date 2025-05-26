package com.sos.jitl.jobs.checklicense.classes;

import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class CheckLicenseWebserviceExecuter {

    private final ApiExecutor apiExecutor;

    public CheckLicenseWebserviceExecuter(ApiExecutor apiExecutor) {
        this.apiExecutor = apiExecutor;
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
        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug("answer=%s", answer);
        }

        Js7LicenseInfo info = new Js7LicenseInfo();
        info = Globals.objectMapper.readValue(answer, Js7LicenseInfo.class);

        return info;

    }

}
