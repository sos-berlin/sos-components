package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IAddCertificateAssgnment;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.encipherment.AgentAssignmentRequestFilter;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/assignment")
public class AddCertificateAssignmentImpl extends JOCResourceImpl implements IAddCertificateAssgnment{

    private static final String API_CALL = "./encipherment/assignment/add";
    
    @Override
    public JOCDefaultResponse postAddCertificateAssgnment(String xAccessToken, byte[] agentAssignmentFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            agentAssignmentFilter = initLogging(API_CALL, agentAssignmentFilter, xAccessToken);
            JsonValidator.validateFailFast(agentAssignmentFilter, AgentAssignmentRequestFilter.class);
            AgentAssignmentRequestFilter filter = Globals.objectMapper.readValue(agentAssignmentFilter, AgentAssignmentRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.addEnciphermentCertificateMapping(filter.getCertAlias(), filter.getAgentId());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
