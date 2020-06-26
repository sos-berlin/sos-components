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
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.request.switchmember.JocClusterSwitchMemberRequest;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocServiceException;

@Path("/cluster")
public class JocClusterService extends JOCResourceImpl {

    private static final String API_CALL_RESTART = "./cluster/restart";
    private static final String API_CALL_SWITCH = "./cluster/switchMember";
    private JocCluster cluster;

    @POST
    @Path("restart")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    public JOCDefaultResponse restart(@HeaderParam("X-Access-Token") String accessToken) {
        try {
            // JsonValidator.validateFailFast(filterBytes, xxx.class);
            // xxx body = Globals.objectMapper.readValue(filterBytes, xxx.class);

//            JOCDefaultResponse jocDefaultResponse = init(API_CALL_RESTART, null, accessToken, "", getPermissonsJocCockpit("", accessToken)
//                    .getJoc().getView().isLog());
            
            // TODO permission for restart
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_RESTART, null, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJoc().getView().isLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // TODO doStop(), doStart(), ....

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
            // JsonValidator.validateFailFast(filterBytes, JocClusterSwitchMemberRequest.class);
            JocClusterSwitchMemberRequest body = Globals.objectMapper.readValue(filterBytes, JocClusterSwitchMemberRequest.class);

            // TODO permission for switch
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_SWITCH, body, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJoc().getView().isLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // TODO get cluster from ClusterServlet
            JocClusterAnswer entity = new JocClusterAnswer();
            if (cluster != null) {
                entity = cluster.switchMember(body.getMemberId());
            } else {
                throw new JocServiceException("cluster not running");
            }
            
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
