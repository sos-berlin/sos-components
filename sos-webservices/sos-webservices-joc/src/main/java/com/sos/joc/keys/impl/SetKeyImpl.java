package com.sos.joc.keys.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.ISetKey;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocPGPKeyType;
import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.joc.model.publish.SetKeyFilter;


@Path("set_key")
public class SetKeyImpl extends JOCResourceImpl implements ISetKey {

    private static final String API_CALL = "./publish/set_key";

    @Override
    public JOCDefaultResponse postSetKey(String xAccessToken, SetKeyFilter setKeyFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, setKeyFilter, xAccessToken, null,
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isSetKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSPGPKeyPair keyPair = setKeyFilter.getKeys();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            if (keyPair.getPrivateKey() != null) {
                dbLayerKeys.saveOrUpdateKey(
                        JocPGPKeyType.PRIVATE.ordinal(), 
                        keyPair.getPrivateKey(), 
                        jobschedulerUser.getSosShiroCurrentUser().getUsername());
            } else if (Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                dbLayerKeys.saveOrUpdateKey(
                        JocPGPKeyType.PRIVATE.ordinal(), 
                        keyPair.getPublicKey(), 
                        jobschedulerUser.getSosShiroCurrentUser().getUsername());
            } else if (keyPair.getPublicKey() != null && !Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }


}
