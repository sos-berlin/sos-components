package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IReadAddOrderPositions;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.read.RequestWorkflowFilter;
import com.sos.joc.model.order.OrdersPositions;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class ReadAddOrderPositionsImpl extends JOCResourceImpl implements IReadAddOrderPositions {

    @Override
    public JOCDefaultResponse read(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestWorkflowFilter.class);
            RequestWorkflowFilter in = Globals.objectMapper.readValue(inBytes, RequestWorkflowFilter.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response == null) {
                response = read(in);
            }
            return response;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private JOCDefaultResponse read(RequestWorkflowFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);

            DBItemInventoryConfiguration config = JocInventory.getConfiguration(dbLayer, null, in.getWorkflowPath(), ConfigurationType.WORKFLOW,
                    folderPermissions);

            OrdersPositions entry = new OrdersPositions();
            entry.setSurveyDate(config.getModified());
            WorkflowId wId = new WorkflowId();
            wId.setPath(config.getPath());
            entry.setWorkflowId(wId);
            if (!SOSString.isEmpty(config.getContent())) {
                Workflow w = Globals.objectMapper.readValue(config.getContent(), Workflow.class);
                entry.setPositions(WorkflowsHelper.getWorkflowAddOrderPositions(w));
            } else {
                throw new DBMissingDataException(String.format("Workflow instructions not found: %s", in.getWorkflowPath()));
            }
            entry.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entry));
        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
    
}
