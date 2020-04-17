package com.sos.joc.keys.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.resource.IShowKey;
import com.sos.joc.model.pgp.SOSPGPKeyPair;


@Path("show_key")
public class ShowKeyImpl extends JOCResourceImpl implements IShowKey {

    private static final String API_CALL = "./publish/show_key";

    @Override
    public JOCDefaultResponse postShowKey(String xAccessToken) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, xAccessToken, null,
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isShowKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
            // TODO: read key from database,
            // if public -> show
            // if private -> restore public -> show both
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
