package com.sos.joc.cluster.api;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.api.JocClusterMeta.HandlerIdentifier;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerType;
import com.sos.joc.cluster.api.bean.request.restart.JocClusterRestartRequest;
import com.sos.joc.cluster.api.bean.request.switchmember.JocClusterSwitchMemberRequest;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.schema.JsonValidator;

@Path("/cluster")
public class JocClusterService extends JOCResourceImpl {

    private static final String API_CALL_RESTART = "./cluster/restart";
    private static final String API_CALL_SWITCH = "./cluster/switchMember";

    @POST
    @Path("restart")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse restart(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, JocClusterRestartRequest.class);
            JocClusterRestartRequest body = Globals.objectMapper.readValue(filterBytes, JocClusterRestartRequest.class);

            // TODO permission for restart
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_RESTART, body, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJoc().getView().isLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JocClusterAnswer entity = new JocClusterAnswer();
            if (body.getType().equals(HandlerIdentifier.cluster)) {
                entity = JocClusterServiceHelper.getInstance().restartCluster();
            } else {
                entity = JocClusterServiceHelper.getInstance().restartHandler(body);
            }
            if (entity.getError() != null) {
                Exception ex = entity.getError().getException();
                if (ex != null) {
                    throw new JocServiceException(ex);
                } else {
                    throw new JocServiceException(entity.getError().getMessage());
                }
            } 
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @POST
    @Path("switchMember")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse switchMember(@HeaderParam("X-Access-Token") String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, JocClusterSwitchMemberRequest.class);
            JocClusterSwitchMemberRequest body = Globals.objectMapper.readValue(filterBytes, JocClusterSwitchMemberRequest.class);

            // TODO permission for switch
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_SWITCH, body, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJoc().getView().isLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JocClusterAnswer entity = new JocClusterAnswer();
            entity.setType(JocClusterAnswerType.SUCCESS);
            JocCluster cluster = JocClusterServiceHelper.getInstance().getCluster();
            if (cluster != null) {
                entity = cluster.switchMember(body.getMemberId());
            } else {
                throw new JocServiceException("cluster not running");
            }
            if (entity.getError() != null) {
                Exception ex = entity.getError().getException();
                if (ex != null) {
                    throw new JocServiceException(ex);
                } else {
                    throw new JocServiceException(entity.getError().getMessage());
                }
            } 
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
