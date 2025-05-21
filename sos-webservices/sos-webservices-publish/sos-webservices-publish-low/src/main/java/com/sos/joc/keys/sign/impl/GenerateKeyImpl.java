package com.sos.joc.keys.sign.impl;

import java.security.cert.X509Certificate;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.ca.CAUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.sign.resource.IGenerateKey;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.GenerateKeyFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.util.SigningCertificateUtil;
import com.sos.schema.JsonValidator;

@Path("profile/key")
public class GenerateKeyImpl extends JOCResourceImpl implements IGenerateKey {

    private static final String API_CALL = "./profile/key/generate";

    @Override
    public JOCDefaultResponse postGenerateKey(String xAccessToken, byte[] generateKeyFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            generateKeyFilter = initLogging(API_CALL, generateKeyFilter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(generateKeyFilter, GenerateKeyFilter.class);
            GenerateKeyFilter filter = Globals.objectMapper.readValue(generateKeyFilter, GenerateKeyFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(filter.getAuditLog());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            
            Date validUntil = filter.getValidUntil();
            if (filter.getKeyAlgorithm() == null) {
                // default
                filter.setKeyAlgorithm(SOSKeyConstants.RSA_ALGORITHM_NAME);
            }
            JocKeyPair keyPair = null;
            String accountName = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                if (validUntil != null) {
                    Long secondsToExpire = validUntil.getTime() / 1000;
                    keyPair = KeyUtil.createKeyPair(accountName, null, secondsToExpire);
                } else {
                    keyPair = KeyUtil.createKeyPair(accountName, null, null);
                }
            } else {
                boolean rootCaAvailable = false;
                JocKeyPair rootKeyPair = null;
                X509Certificate rootCert = null;
                if(filter.getUseSslCa()) {
                    rootKeyPair = dbLayer.getAuthRootCaKeyPair();
                    if (rootKeyPair != null) {
                        rootCaAvailable = true;
                        rootCert = KeyUtil.getX509Certificate(rootKeyPair.getCertificate());
                        if(rootCert != null) {
                            // first: get new PK and X509 certificate from stored CA
                            String newDN = CAUtils.createUserSubjectDN(filter.getDn(), rootCert, accountName);
                            String san = accountName;
                            CreateCSRFilter csrFilter = new CreateCSRFilter();
                            csrFilter.setDn(newDN);
                            csrFilter.setSan(san);
                            keyPair = SigningCertificateUtil.createSigningKeyPair(hibernateSession, csrFilter, filter.getKeyAlgorithm(), validUntil);
                        }
                    } else {
                        throw new JocConfigurationException("No CA configured in SSL Key Management. Please configure the CA for SSL Key Management first.");
                    }
                    // update CA info for signing key
                    if(rootCaAvailable) {
                        dbLayer.saveOrUpdateSigningRootCaCertificate(rootKeyPair, accountName, Globals.getJocSecurityLevel().intValue());
                    }
                } else {
                    if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                        // default
                        keyPair = KeyUtil.createRSAJocKeyPair(accountName, filter.getDn());
                    } else {
                        keyPair = KeyUtil.createECDSAJOCKeyPair(accountName, filter.getDn());
                    }
                }
            }
            // store private key to the db
            dbLayer.saveOrUpdateGeneratedKey(keyPair, accountName, JocSecurityLevel.LOW, filter.getDn());
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
