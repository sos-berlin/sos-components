package com.sos.joc.workflow.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.workflow.resource.IWorkflowResource;
import com.sos.schema.JsonValidator;

@Path("workflow")
public class WorkflowResourceImpl extends JOCResourceImpl implements IWorkflowResource {

    private static final String API_CALL = "./workflow";
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
    
    
    @Override
    public JOCDefaultResponse postWorkflow(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowFilter.getControllerId(), getPermissonsJocCockpit(workflowFilter
                    .getControllerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            com.sos.inventory.model.workflow.Workflow item = addWorkflowPositions(dbLayer.getDeployedInventory(workflowFilter));
            if (item == null) {
                throw new DBMissingDataException(String.format("Workflow '%s' doesn't exist", workflowFilter.getWorkflowId().getPath()));
            }
            
//            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
//            filter.setControllerId(workflowFilter.getJobschedulerId());
//            filter.setWorkflow(workflowFilter.getWorkflowId().getPath());
//            filter.setDailyPlanDate(formatter.format(Instant.now()));

            
            Workflow workflow = new Workflow();
            workflow.setDeliveryDate(Date.from(Instant.now()));
            workflow.setWorkflow(item);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(workflow));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

//    public JOCDefaultResponse postWorkflowVolatile(String accessToken, byte[] filterBytes) {
//        try {
//            initLogging(API_CALL, filterBytes, accessToken);
//            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
//            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
//            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowFilter.getJobschedulerId(),
//                    getPermissonsJocCockpit(workflowFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//
//            JControllerState currentState = Proxy.of(workflowFilter.getJobschedulerId()).currentState();
//            // Long surveyDateMillis = currentState.eventId() / 1000;
//            Either<Problem, JWorkflow> response = currentState.idToWorkflow(JWorkflowId.of(workflowFilter.getWorkflowId().getPath(), workflowFilter
//                    .getWorkflowId().getVersionId()));
//            ProblemHelper.throwProblemIfExist(response);
//
//            return JOCDefaultResponse.responseStatus200(response.get().toJson());
//
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        }
//    }
    
    private com.sos.inventory.model.workflow.Workflow addWorkflowPositions(com.sos.inventory.model.workflow.Workflow w) {
        if (w == null) {
            return null;
        }
        Object[] o = {};
        setWorkflowPositions(o, w.getInstructions());
        return w;
    }
    
    private void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for(Branch b : f.getBranches()) {
                        setWorkflowPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositions(extendArray(pos, "then"), ie.getThen().getInstructions());
                    if (ie.getElse() != null) {
                        setWorkflowPositions(extendArray(pos, "else"), ie.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositions(extendArray(pos, "try+0"), tc.getTry().getInstructions());
                    if (tc.getCatch() != null) {
                        setWorkflowPositions(extendArray(pos, "catch+0"), tc.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    setWorkflowPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions());
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private Object[] extendArray(Object[] position, Object extValue) {
        Object[] pos = Arrays.copyOf(position, position.length + 1);
        pos[position.length] = extValue;
        return pos;
    }

}
