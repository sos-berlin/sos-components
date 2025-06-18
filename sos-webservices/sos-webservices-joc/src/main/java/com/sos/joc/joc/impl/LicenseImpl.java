package com.sos.joc.joc.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.joc.resource.ILicense;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.joc.Js7LicenseInfo;
import com.sos.joc.model.joc.LicenseType;

import jakarta.ws.rs.Path;

@Path("joc")
public class LicenseImpl extends JOCResourceImpl implements ILicense {

    private static final String API_CALL = "./joc/license";

    @Override
    public JOCDefaultResponse postLicense(String accessToken) {
        try {
            initLogging(API_CALL, "{}".getBytes(), accessToken, CategoryType.OTHERS);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            Js7LicenseInfo info = new Js7LicenseInfo();
            if(JocClusterService.getInstance().getCluster() != null) {
                JocClusterService.getInstance().getCluster().getConfig().rereadClusterMode();
                if(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().isJarFound()) {
                    info.setValid(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().getUse());
                    info.setValidFrom(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().getValidFrom());
                    info.setValidUntil(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().getValidUntil());
                    if (info.getValid()) {
                        info.setType(LicenseType.COMMERCIAL_VALID);
                    } else {
                        info.setType(LicenseType.COMMERCIAL_INVALID);
                    }
                } else {
                    info.setType(LicenseType.OPENSOURCE);
                    info.setValid(false);
                }
            } else {
                throw new JocServiceException("cluster service not available, retry in a few seconds.");
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(info));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
