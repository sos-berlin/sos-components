package com.sos.joc.utilities.impl;

import java.nio.charset.StandardCharsets;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.utilities.resource.IHashResource;

@javax.ws.rs.Path("utilities")
public class HashImpl extends JOCResourceImpl implements IHashResource {
    
    private static final String API_CALL = "./utilities/hash";
    
    @Override
    public JOCDefaultResponse postHash(String accessToken, byte[] body) {
        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getSettings().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String s = (body == null) ? "" : new String(body, StandardCharsets.UTF_8);
            s = s.trim();
            if (s.isEmpty()) {
                return JOCDefaultResponse.responsePlainStatus200("plain:");
            }
            return JOCDefaultResponse.responsePlainStatus200("sha512:" + SOSString.hash512(s).toUpperCase());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
