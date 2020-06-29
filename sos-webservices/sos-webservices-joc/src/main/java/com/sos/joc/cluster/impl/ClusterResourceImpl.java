package com.sos.joc.cluster.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.resource.IClusterResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.model.cluster.ClusterRestart;
import com.sos.joc.model.cluster.ClusterSwitchMember;
import com.sos.joc.model.cluster.common.ClusterHandlerIdentifier;
import com.sos.schema.JsonValidator;

@Path(ClusterResourceImpl.API_PATH)
public class ClusterResourceImpl extends JOCResourceImpl implements IClusterResource {

    public static final String API_PATH = "cluster";
    public static final String IMPL_PATH_RESTART = "restart";
    public static final String IMPL_PATH_SWITCH_MEMBER = "switchMember";

    private static final String API_CALL_RESTART = String.format("./%s/%s", API_PATH, IMPL_PATH_RESTART);
    private static final String API_CALL_SWITCH = String.format("./%s/%s", API_PATH, IMPL_PATH_SWITCH_MEMBER);

    @Override
    public JOCDefaultResponse restart(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, ClusterRestart.class);
            ClusterRestart in = Globals.objectMapper.readValue(filterBytes, ClusterRestart.class);

            JOCDefaultResponse response = checkPermissions(API_CALL_RESTART, accessToken, in);
            if (response == null) {
                JocClusterAnswer answer = new JocClusterAnswer();
                if (in.getType().equals(ClusterHandlerIdentifier.cluster)) {
                    answer = JocClusterService.getInstance().restart();
                } else {
                    answer = JocClusterService.getInstance().restartHandler(in);
                }
                if (answer.getError() != null) {
                    Exception ex = answer.getError().getException();
                    if (ex != null) {
                        throw new JocServiceException(ex);
                    } else {
                        throw new JocServiceException(answer.getError().getMessage());
                    }
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
    public JOCDefaultResponse switchMember(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, ClusterSwitchMember.class);
            ClusterSwitchMember in = Globals.objectMapper.readValue(filterBytes, ClusterSwitchMember.class);

            JOCDefaultResponse response = checkPermissions(API_CALL_SWITCH, accessToken, in);
            if (response == null) {
                JocClusterAnswer answer = JocClusterService.getInstance().switchMember(in.getMemberId());
                if (answer.getError() != null) {
                    Exception ex = answer.getError().getException();
                    if (ex != null) {
                        throw new JocServiceException(ex);
                    } else {
                        throw new JocServiceException(answer.getError().getMessage());
                    }
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

    // TODO permission restart/switchmember
    private JOCDefaultResponse checkPermissions(final String request, final String accessToken, final Object in) throws Exception {
        SOSPermissionJocCockpit permissions = getPermissonsJocCockpit("", accessToken);
        boolean permission = permissions.getJoc().getView().isLog();
        return init(request, in, accessToken, "", permission);
    }

}
