package com.sos.joc.joc.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.ILicense;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.joc.model.joc.LicenseType;
import com.sos.js7.license.joc.ClusterLicenseCheck;

@Path("joc")
public class LicenseImpl extends JOCResourceImpl implements ILicense {

    private static final String API_CALL = "./joc/license";

    @Override
    public JOCDefaultResponse postLicense(String accessToken) {
        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Js7LicenseInfo info = new Js7LicenseInfo();
            info.setValid(ClusterLicenseCheck.hasLicense());
            info.setValidFrom(ClusterLicenseCheck.getValidFrom());
            info.setValidUntil(ClusterLicenseCheck.getValidUntil());
            if(info.getValidFrom() == null && info.getValidUntil() == null && !info.getValid()) {
                info.setType(LicenseType.OPENSOURCE);
            } else {
                info.setType(LicenseType.COMMERCIAL);
            }
            return JOCDefaultResponse.responseStatus200(info);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
