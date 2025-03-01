package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.ProxiesEdit;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IRestartProxiesResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.cluster.ClusterServiceRun;

import jakarta.ws.rs.Path;

@Path("joc")
public class RestartProxies extends JOCResourceImpl implements IRestartProxiesResource {

    private static final String API_CALL = "./joc/proxies/restart";

    @Override
    public JOCDefaultResponse restart(String accessToken, byte[] filterBytes) {

        try {
            initLogging(API_CALL, filterBytes, accessToken);
            boolean perm = getJocPermissions(accessToken).getCluster().getManage() || getJocPermissions(accessToken).getAdministration()
                    .getControllers().getManage();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            ClusterServiceRun in = Globals.objectMapper.readValue(filterBytes, ClusterServiceRun.class);
            storeAuditLog(in.getAuditLog(), CategoryType.CONTROLLER);
            ProxiesEdit.forcedRestartForJOC();
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
