package com.sos.joc.keys.ca.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocKeyNotValidException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.keys.ca.resource.ISetRootCa;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.SetRootCaFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;


@Path("profile/ca")
public class SetRootCaImpl extends JOCResourceImpl implements ISetRootCa {

    private static final String API_CALL = "./profile/ca/store";

    @Override
    public JOCDefaultResponse postSetRootCa(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, SetRootCaFilter.class);
            SetRootCaFilter setRootCaFilter = Globals.objectMapper.readValue(filter, SetRootCaFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(setRootCaFilter.getAuditLog(), CategoryType.CERTIFICATES);
            
            JocKeyPair keyPair = new JocKeyPair();
            keyPair.setPrivateKey(setRootCaFilter.getPrivateKey());
            keyPair.setCertificate(setRootCaFilter.getCertificate());
            keyPair.setKeyAlgorithm(SOSKeyConstants.ECDSA_ALGORITHM_NAME);
            keyPair.setKeyType(JocKeyType.CA.name());
            if (PublishUtils.jocKeyPairNotEmpty(keyPair)) {
                if (KeyUtil.isECDSARootKeyPairValid(keyPair)) {
                    hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                    if (keyPair.getPrivateKey() != null && !keyPair.getPrivateKey().isEmpty() &&
                            keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                        PublishUtils.storeCA(keyPair, hibernateSession);
                    } 
                } else {
                    throw new JocKeyNotValidException("key data is not a known key type!");
                }
            } else {
              throw new JocMissingRequiredParameterException("No key was provided");
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
