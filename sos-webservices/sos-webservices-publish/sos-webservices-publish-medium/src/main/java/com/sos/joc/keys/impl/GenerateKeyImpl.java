package com.sos.joc.keys.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.GenerateKeyAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.IGenerateKey;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.GenerateKeyFilter;
import com.sos.schema.JsonValidator;


@Path("publish")
public class GenerateKeyImpl extends JOCResourceImpl implements IGenerateKey {

    private static final String API_CALL = "./publish/generate_key";

    @Override
    public JOCDefaultResponse postGenerateKey(String xAccessToken, byte[] generateKeyFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, generateKeyFilter, xAccessToken);
            JsonValidator.validateFailFast(generateKeyFilter, GenerateKeyFilter.class);
            GenerateKeyFilter filter = Globals.objectMapper.readValue(generateKeyFilter, GenerateKeyFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isGenerateKey());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Date validUntil = filter.getValidUntil();
            if(filter.getKeyAlgorithm() == null) {
                filter.setKeyAlgorithm(SOSKeyConstants.RSA_ALGORITHM_NAME);
            }
            JocKeyPair keyPair = null;
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                if (validUntil != null) {
                    Long secondsToExpire = validUntil.getTime() / 1000;
                    keyPair = KeyUtil.createKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername(), null, secondsToExpire);
                } else {
                    keyPair = KeyUtil.createKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername(), null, null);
                }                
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                keyPair = KeyUtil.createRSAJocKeyPair();
                //default
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                keyPair = KeyUtil.createECDSAJOCKeyPair();
            }
            keyPair.setKeyAlgorithm(filter.getKeyAlgorithm());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            // store private key to the db
            dbLayerKeys.saveOrUpdateGeneratedKey(keyPair, 
                    jobschedulerUser.getSosShiroCurrentUser().getUsername(),
                    JocSecurityLevel.MEDIUM);
            GenerateKeyAudit audit = new GenerateKeyAudit(filter, 
                    String.format("new Private Key generated for profile - %1$s -", jobschedulerUser.getSosShiroCurrentUser().getUsername()));
            logAuditMessage(audit);
            storeAuditLogEntry(audit);
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
