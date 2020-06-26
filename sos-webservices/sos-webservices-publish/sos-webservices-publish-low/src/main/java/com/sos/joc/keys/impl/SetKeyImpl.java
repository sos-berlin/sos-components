package com.sos.joc.keys.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocPGPKeyNotValidException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.resource.ISetKey;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.SetKeyFilter;
import com.sos.joc.publish.util.PublishUtils;


@Path("publish")
public class SetKeyImpl extends JOCResourceImpl implements ISetKey {

    private static final String API_CALL = "./publish/set_key";

    @Override
    public JOCDefaultResponse postSetKey(String xAccessToken, SetKeyFilter setKeyFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, setKeyFilter, xAccessToken, "",
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isSetKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            JocKeyPair keyPair = setKeyFilter.getKeys();
            if (keyPairNotEmpty(keyPair)) {
                if (KeyUtil.isKeyPairValid(keyPair)) {
                    hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                    String account = Globals.getDefaultProfileUserAccount();
                    if (keyPair.getPrivateKey() != null && !keyPair.getPrivateKey().isEmpty()) {
                        PublishUtils.storeKey(keyPair, hibernateSession, account);
                    } else if (keyPair.getPublicKey() != null && !keyPair.getPublicKey().isEmpty()) {
                        throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
                    } 
                } else {
                    throw new JocPGPKeyNotValidException("key data is not a PGP key!");
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
        }
    }

    private Boolean keyPairNotEmpty (JocKeyPair keyPair) {
        Boolean checkNotEmpty = false;
        if(keyPair != null) {
            if(keyPair.getPrivateKey() != null && !"".equals(keyPair.getPrivateKey())) {
                checkNotEmpty = true;
            } else if (keyPair.getPrivateKey() != null && "".equals(keyPair.getPrivateKey())) {
                checkNotEmpty = false;
            } else if (keyPair.getPrivateKey() == null) {
                checkNotEmpty = false;
            } 
            if (checkNotEmpty) {
                return checkNotEmpty;
            } else {
                if (keyPair.getPublicKey() == null) {
                    checkNotEmpty = false;
                } else if (!"".equals(keyPair.getPublicKey())) {
                    checkNotEmpty = true;
                } else if ("".equals(keyPair.getPublicKey())) {
                    checkNotEmpty = false;
                }
                return checkNotEmpty;
            }
        } else {
            checkNotEmpty = false;
        }
        return checkNotEmpty;
    }
}
