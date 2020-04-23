package com.sos.joc.keys.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.Path;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.KeyNotExistException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.keys.resource.IShowKey;
import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.pgp.util.key.KeyUtil;


@Path("publish")
public class ShowKeyImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./publish/show_key";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowKeyImpl.class);

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, xAccessToken, "",
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isShowKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            SOSPGPKeyPair keyPair = dbLayerKeys.getKeys(jobschedulerUser.getSosShiroCurrentUser().getUsername());
            if (keyPair == null) {
                throw new KeyNotExistException("No key found in the database for this user!");
            } else {
                if (keyPair.getPublicKey() == null) {
                    // restore public key from private key
                    keyPair.setPublicKey(KeyUtil.extractPublicKey(keyPair.getPrivateKey()));
                    // calculate validity period
                    InputStream privateKeyStream = IOUtils.toInputStream(keyPair.getPrivateKey());
                    PGPPublicKey publicPGPKey = KeyUtil.extractPGPPublicKey(privateKeyStream);
                    Date creationDate = publicPGPKey.getCreationTime();
                    Long validSeconds = publicPGPKey.getValidSeconds();
                    Date validUntil = null;
                    if (validSeconds == 0) {
                        LOGGER.debug("Key does not expire!");
                    } else {
                        validUntil = new Date(creationDate.getTime() + (validSeconds * 1000));
                        LOGGER.debug("Key is valid until: " + validUntil.toString()); 
                    }
                    keyPair.setValidUntil(validUntil);
                }
            }
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
