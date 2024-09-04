package com.sos.joc.cluster.impl;

import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.ProxiesEdit;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.resource.IClusterResource;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.model.cluster.ClusterRestart;
import com.sos.joc.model.cluster.ClusterSwitchMember;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.schema.JsonValidator;

@Path(ClusterResourceImpl.API_PATH)
public class ClusterResourceImpl extends JOCResourceImpl implements IClusterResource {

    public static final String API_PATH = "joc/cluster";
    public static final String IMPL_PATH_FORCE_START = "force_start";
    public static final String IMPL_PATH_RESTART = "restart";
    public static final String IMPL_PATH_SWITCH_MEMBER = "switch_member";
    public static final String IMPL_PATH_DELETE_MEMBER = "delete_member";

    private static final String API_CALL_FORCE_START = String.format("./%s/%s", API_PATH, IMPL_PATH_FORCE_START);
    private static final String API_CALL_RESTART = String.format("./%s/%s", API_PATH, IMPL_PATH_RESTART);
    private static final String API_CALL_SWITCH = String.format("./%s/%s", API_PATH, IMPL_PATH_SWITCH_MEMBER);
    private static final String API_CALL_DELETE = String.format("./%s/%s", API_PATH, IMPL_PATH_DELETE_MEMBER);

    @Override
    public JOCDefaultResponse forceServiceStart(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_FORCE_START, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ClusterRestart.class);
            ClusterRestart in = Globals.objectMapper.readValue(filterBytes, ClusterRestart.class);
            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).getCluster().getManage());
            if (response == null) {
                processAnswer(JocClusterService.getInstance().forceServiceStart(in, StartupMode.manual_restart));
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
    public JOCDefaultResponse restart(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RESTART, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ClusterRestart.class);
            ClusterRestart in = Globals.objectMapper.readValue(filterBytes, ClusterRestart.class);
            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).getCluster().getManage());
            if (response == null) {
                if (in.getType().equals(ClusterServices.cluster)) { // all services
                    processAnswer(JocClusterService.getInstance().restart(StartupMode.manual_restart));
                    // proxy restart in addition
                    ProxiesEdit.forcedRestartForJOC();
                } else {
                    processAnswer(JocClusterService.getInstance().restartService(in, StartupMode.manual_restart));
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

    @Override
    public JOCDefaultResponse deleteMember(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ClusterSwitchMember.class);
            ClusterSwitchMember in = Globals.objectMapper.readValue(filterBytes, ClusterSwitchMember.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getCluster().getManage());
            if (response == null) {
                session = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);

                JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(session);
                DBItemJocInstance member = dbLayer.getInstance(in.getMemberId());
                if (member == null) {
                    throw new DBMissingDataException(String.format("Couldn't find cluster member: %s", in.getMemberId()));
                }
                if (member.getApiServer()) {
                    if (member.getHeartBeat() == null || JocCluster.isHeartBeatExceeded(session.getCurrentUTCDateTime(), member.getHeartBeat())) {
                        // Long osId = member.getOsId();
                        // TODO delete obsolete row in INV_OPERATING_SYSTEMS if not used with other controller or cluster instances
                        session.delete(member);
                        EventBus.getInstance().post(new ActiveClusterChangedEvent());
                    } else {
                        throw new JocBadRequestException(String.format("The last heartbeat of '%s#%d' is younger than one minute.", member
                                .getClusterId(), member.getOrdering()));
                    }
                } else {
                    DBItemJocCluster activeMember = dbLayer.getCluster();
                    boolean isInactive = activeMember == null || !activeMember.getMemberId().equals(in.getMemberId());
                    if (isInactive && (member.getHeartBeat() == null || JocCluster.isHeartBeatExceeded(session.getCurrentUTCDateTime(), member
                            .getHeartBeat()))) {
                        // Long osId = member.getOsId();
                        // TODO delete obsolete row in INV_OPERATING_SYSTEMS if not used with other controller or cluster instances
                        session.delete(member);
                        EventBus.getInstance().post(new ActiveClusterChangedEvent());
                    } else {
                        throw new JocBadRequestException(String.format(
                                "The cluster member '%s#%d' is either active or its last heartbeat is younger than one minute.", member
                                        .getClusterId(), member.getOrdering()));
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
        } finally {
            Globals.disconnect(session);
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
