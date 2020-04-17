package com.sos.joc.keys.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.resource.ISetKey;
import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.joc.model.publish.SetKeyFilter;


@Path("set_key")
public class SetKeyImpl extends JOCResourceImpl implements ISetKey {

    private static final String API_CALL = "./publish/set_key";

    @Override
    public JOCDefaultResponse postSetKey(String xAccessToken, SetKeyFilter setKeyFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, setKeyFilter, xAccessToken, null,
//                    getPermissonsJocCockpit(null, accessToken).getPublish().getView().isSetKey()
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSPGPKeyPair keyPair = new SOSPGPKeyPair();
            // TODO: set the new key 
            return JOCDefaultResponse.responseStatus200(keyPair);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }


}
