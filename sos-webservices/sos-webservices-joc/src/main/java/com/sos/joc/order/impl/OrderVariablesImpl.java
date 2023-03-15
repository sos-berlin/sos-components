package com.sos.joc.order.impl;

import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.CheckedResumeOrdersPositions;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.order.OrderVariablesFilter;
import com.sos.joc.order.resource.IOrderVariablesResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.position.JPosition;

@Path("order")
public class OrderVariablesImpl extends JOCResourceImpl implements IOrderVariablesResource {

    private static final String API_CALL = "./order/variables";

    @SuppressWarnings("unchecked")
    @Override
    public JOCDefaultResponse postVariables(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, OrderVariablesFilter.class);
            OrderVariablesFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderVariablesFilter.class);
            String controllerId = orderFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            JPosition position = null;
            JControllerState currentState = Proxy.of(controllerId).currentState();
            
            if (orderFilter.getPosition() != null) {
                if (orderFilter.getPosition() instanceof String) {
                    JOrder jOrder = currentState.idToOrder().get(OrderId.of(orderFilter.getOrderId()));
                    if (jOrder == null) {
                        throw new JocObjectNotExistException(String.format("Unknown OrderId: %s", orderFilter.getOrderId()));
                    }
                    connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                    DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                    DeployedContent dbWorkflow = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), jOrder.workflowId().path()
                            .string());
                    Globals.disconnect(connection);
                    connection = null;
                    if (dbWorkflow != null) {
                        Workflow w = JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
                        if (w != null) {
                            position = getPosition(OrdersHelper.getPosition(orderFilter.getPosition(), WorkflowsHelper.getLabelToPositionsMap(w)));
                        }
                    }
                } else if (orderFilter.getPosition() instanceof List<?>) {
                    position = getPosition((List<Object>) orderFilter.getPosition());
                }
            }
            
            CheckedResumeOrdersPositions cop = new CheckedResumeOrdersPositions();
            cop.get(orderFilter.getOrderId(), currentState, folderPermissions.getListOfFolders(), position, false);
            cop.setOrderIds(null);
            cop.setDisabledPositionChange(null);
            cop.setPositions(null);
            cop.setWithCyclePosition(null);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(cop));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private JPosition getPosition(List<Object> pos) {
        if (pos != null && !pos.isEmpty()) {
            Either<Problem, JPosition> jPos = JPosition.fromList(pos);
            ProblemHelper.throwProblemIfExist(jPos);
            return jPos.get();
        }
        return null;
    }

}
