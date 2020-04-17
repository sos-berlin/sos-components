package com.sos.joc.keys.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.resource.IGenerateKey;
import com.sos.joc.model.pgp.SOSPGPKeyPair;


@Path("generate_key")
public class GenerateKeyImpl extends JOCResourceImpl implements IGenerateKey {

    private static final String API_CALL = "./publish/generate_key";

    @Override
    public JOCDefaultResponse postGenerateKey(String xAccessToken) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, xAccessToken, null,
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isShowKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
            // TODO: generate new keyPair
            // -> show both
            // store private key to the db
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
