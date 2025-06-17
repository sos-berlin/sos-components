package com.sos.joc.utilities.impl;

import java.nio.charset.StandardCharsets;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.utilities.resource.IHashResource;

import jakarta.ws.rs.core.MediaType;

@jakarta.ws.rs.Path(WebservicePaths.UTILITIES)
public class HashImpl extends JOCResourceImpl implements IHashResource {

    @Override
    public JOCDefaultResponse postHash(String accessToken, byte[] body) {
        try {
            body = initLogging(IMPL_PATH, body, accessToken, CategoryType.OTHERS);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getSettings()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String s = (body == null) ? "" : new String(body, StandardCharsets.UTF_8);
            s = s.trim();
            if (s.isEmpty()) {
                return responseStatus200("plain:".getBytes(), MediaType.TEXT_PLAIN + "; charset=UTF-8");
            }
            return responseStatus200(("sha512:" + SOSString.hash512(s).toUpperCase()).getBytes(), MediaType.TEXT_PLAIN + "; charset=UTF-8");
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
