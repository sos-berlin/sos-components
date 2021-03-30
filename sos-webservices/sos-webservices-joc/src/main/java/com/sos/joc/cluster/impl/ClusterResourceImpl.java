package com.sos.joc.cluster.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.resource.IClusterResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.model.cluster.ClusterRestart;
import com.sos.joc.model.cluster.ClusterSwitchMember;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.schema.JsonValidator;

@Path(ClusterResourceImpl.API_PATH)
public class ClusterResourceImpl extends JOCResourceImpl implements IClusterResource {

    public static final String API_PATH = "joc/cluster";
    public static final String IMPL_PATH_RESTART = "restart";
    public static final String IMPL_PATH_SWITCH_MEMBER = "switch_member";

    private static final String API_CALL_RESTART = String.format("./%s/%s", API_PATH, IMPL_PATH_RESTART);
    private static final String API_CALL_SWITCH = String.format("./%s/%s", API_PATH, IMPL_PATH_SWITCH_MEMBER);

    @Override
    public JOCDefaultResponse restart(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RESTART, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ClusterRestart.class);
            ClusterRestart in = Globals.objectMapper.readValue(filterBytes, ClusterRestart.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getCluster().getManage());
            if (response == null) {
                if (in.getType().equals(ClusterServices.cluster)) {
                    processAnswer(JocClusterService.getInstance().restart(StartupMode.manual));
                } else {
                    processAnswer(JocClusterService.getInstance().restartService(in, StartupMode.manual));
                }
                response = JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse switchMember(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_SWITCH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ClusterSwitchMember.class);
            ClusterSwitchMember in = Globals.objectMapper.readValue(filterBytes, ClusterSwitchMember.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getCluster().getManage());
            if (response == null) {
                processAnswer(JocClusterService.getInstance().switchMember(StartupMode.manual_switchover, in.getMemberId()));
                response = JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void processAnswer(JocClusterAnswer answer) throws JocServiceException {
        if (answer.getError() != null) {
            Exception ex = answer.getError().getException();
            if (ex != null) {
                throw new JocServiceException(ex);
            } else {
                throw new JocServiceException(answer.getError().getMessage());
            }
        }
    }

}
