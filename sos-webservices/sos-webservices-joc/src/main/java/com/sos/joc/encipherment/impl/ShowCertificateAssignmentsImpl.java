package com.sos.joc.encipherment.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.encipherment.DBItemEncAgentCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IShowCertificateAssgnments;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.AgentAssignments;
import com.sos.joc.model.encipherment.ShowAgentAssignmentsRequestFilter;
import com.sos.joc.model.encipherment.ShowAgentAssignmentsResponse;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/assignment")
public class ShowCertificateAssignmentsImpl extends JOCResourceImpl implements IShowCertificateAssgnments {

    private static final String API_CALL = "./encipherment/assignment";

    @Override
    public JOCDefaultResponse postShowCertificateAssignment(String xAccessToken, byte[] showAssignmentFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            showAssignmentFilter = initLogging(API_CALL, showAssignmentFilter, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validate(showAssignmentFilter, ShowAgentAssignmentsRequestFilter.class);
            ShowAgentAssignmentsRequestFilter filter = Globals.objectMapper.readValue(showAssignmentFilter, ShowAgentAssignmentsRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            List<DBItemEncAgentCertificate> mappings = null;
            if(filter.getAgentIds() != null) {
                mappings = dbLayer.getEnciphermentCertificateMappingsByAgents(filter.getAgentIds());
            } else if (filter.getCertAliases() != null){
                mappings = dbLayer.getEnciphermentCertificateMappingsByCertAliases(filter.getCertAliases());
            }
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(mapDbItems(mappings)));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private ShowAgentAssignmentsResponse mapDbItems (List<DBItemEncAgentCertificate> dbItems) {
        ShowAgentAssignmentsResponse response = new ShowAgentAssignmentsResponse();
        response.setMappings(dbItems.stream()
                .collect(Collectors.groupingBy(DBItemEncAgentCertificate::getCertAlias, 
                        Collectors.mapping(DBItemEncAgentCertificate::getAgentId, Collectors.toList())))
                .entrySet().stream().map(entry -> {
                    AgentAssignments assignment = new AgentAssignments();
                    assignment.setCertAlias(entry.getKey());
                    assignment.setAgentId(entry.getValue());
                    return assignment;
                }).collect(Collectors.toList()));
        return response;
    }
}
