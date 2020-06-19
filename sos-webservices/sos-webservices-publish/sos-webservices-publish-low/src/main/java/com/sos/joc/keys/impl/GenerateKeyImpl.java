package com.sos.joc.keys.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.IGenerateKey;
import com.sos.joc.model.pgp.JocPGPKeyType;
import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.joc.model.publish.GenerateKeyFilter;
import com.sos.commons.sign.pgp.key.KeyUtil;


@Path("publish")
public class GenerateKeyImpl extends JOCResourceImpl implements IGenerateKey {

    private static final String API_CALL = "./publish/generate_key";

    @Override
    public JOCDefaultResponse postGenerateKey(String xAccessToken, GenerateKeyFilter filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, xAccessToken, "",
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isShowKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Date validUntil = filter.getValidUntil();
            SOSPGPKeyPair keyPair = null;
            if (validUntil != null) {
                Long secondsToExpire = validUntil.getTime() / 1000;
                keyPair = KeyUtil.createKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername(), null, secondsToExpire);
            } else {
                keyPair = KeyUtil.createKeyPair(jobschedulerUser.getSosShiroCurrentUser().getUsername(), null, null);
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            // store private key to the db
            dbLayerKeys.saveOrUpdateKey(JocPGPKeyType.PRIVATE.ordinal(), 
                    keyPair.getPrivateKey(), 
                    jobschedulerUser.getSosShiroCurrentUser().getUsername());
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
