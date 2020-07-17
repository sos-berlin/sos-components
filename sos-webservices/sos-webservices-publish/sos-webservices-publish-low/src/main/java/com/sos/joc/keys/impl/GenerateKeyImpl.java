package com.sos.joc.keys.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.IGenerateKey;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.model.publish.GenerateKeyFilter;


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
            Boolean usePGP = filter.getUsePGP();
            if(usePGP == null) {
                usePGP = false;
            }
            JocKeyPair keyPair = null;
            if (usePGP) {
                if (validUntil != null) {
                    Long secondsToExpire = validUntil.getTime() / 1000;
                    keyPair = KeyUtil.createKeyPair(Globals.getDefaultProfileUserAccount(), null, secondsToExpire);
                } else {
                    keyPair = KeyUtil.createKeyPair(Globals.getDefaultProfileUserAccount(), null, null);
                }                
            } else {
                keyPair = KeyUtil.createRSAKeyPair();
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            // store private key to the db
            dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.ordinal(), 
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
