package com.sos.auth.certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.certificate.classes.SOSCertificateAuthWebserviceCredentials;
import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.exception.SOSHibernateException;

public class SOSCertificateAuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSCertificateAuthHandler.class);

    public SOSCertificateAuthHandler() {
    }

    public SOSAuthAccessToken login(SOSCertificateAuthWebserviceCredentials sosCertificateAuthWebserviceCredentials) throws SOSHibernateException {

        SOSAuthAccessToken sosAuthAccessToken = null;
        if (SOSAuthHelper.checkCertificate(sosCertificateAuthWebserviceCredentials.getHttpRequest(), sosCertificateAuthWebserviceCredentials
                .getAccount())) {
            sosAuthAccessToken = new SOSAuthAccessToken();
            sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
        }
        return sosAuthAccessToken;

    }

}
