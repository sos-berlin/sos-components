package com.sos.joc.keys.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.GenerateKeyAudit;
import com.sos.joc.classes.audit.SetKeyAudit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocKeyNotValidException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.resource.ISetKey;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.SetKeyFilter;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;


@Path("publish")
public class SetKeyImpl extends JOCResourceImpl implements ISetKey {

    private static final String API_CALL = "./publish/set_key";

    @Override
    public JOCDefaultResponse postSetKey(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, SetKeyFilter.class);
            SetKeyFilter setKeyFilter = Globals.objectMapper.readValue(filter, SetKeyFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("",
//                    getPermissonsJocCockpit(null, xAccessToken).getInventory().getConfigurations().getPublish().isSetKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            JocKeyPair keyPair = setKeyFilter.getKeys();
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            String reason = null;
            if (PublishUtils.jocKeyPairNotEmpty(keyPair)) {
                if (KeyUtil.isKeyPairValid(keyPair)) {
                    hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                    if (keyPair.getPublicKey() != null && !keyPair.getPublicKey().isEmpty()) {
                        PublishUtils.storeKey(keyPair, hibernateSession, account, JocSecurityLevel.HIGH);
                        reason = String.format("new Public Key stored for profile - %1$s -", account);
                    } else if (keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                        PublishUtils.storeKey(keyPair, hibernateSession, account, JocSecurityLevel.HIGH);
                        reason = String.format("new X.509 Certificate stored for profile - %1$s -", account);
                    } else if (keyPair.getPrivateKey() != null && !keyPair.getPrivateKey().isEmpty()) {
                        throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public or certificate | received: private");
                    }
                } else {
                    throw new JocKeyNotValidException("key data is not a known key type!");
                }
            } else {
              throw new JocMissingRequiredParameterException("No key was provided");
            }
            SetKeyAudit audit = new SetKeyAudit(setKeyFilter, reason);
            logAuditMessage(audit);
            storeAuditLogEntry(audit);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
