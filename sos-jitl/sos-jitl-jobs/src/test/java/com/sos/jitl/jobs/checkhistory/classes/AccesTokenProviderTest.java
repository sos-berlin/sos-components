package com.sos.jitl.jobs.checkhistory.classes;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccesTokenProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccesTokenProviderTest.class);

    @Test
    public void testAccessTokenProvider() throws Exception {
        JOCCredentialStoreParameters jobSchedulerCredentialStoreJOCParameters = new JOCCredentialStoreParameters();
        jobSchedulerCredentialStoreJOCParameters.setUser("root");
        jobSchedulerCredentialStoreJOCParameters.setPassword("root");
        jobSchedulerCredentialStoreJOCParameters.setJocUrl("http://localhost:4426");
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(jobSchedulerCredentialStoreJOCParameters);
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken();
        System.out.println(webserviceCredentials.getAccessToken());

    }

}
