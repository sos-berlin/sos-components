package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.exception.SOSJsonSchemaException;

public class ReferenceValidator {

    public static void validate(String name, ConfigurationType type, byte[] configBytes, InventoryDBLayer dbLayer, String accessToken)
            throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        validate(name, type, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)), dbLayer,
                accessToken);
    }

    public static void validate(String name, ConfigurationType type, byte[] configBytes, String accessToken) throws SOSJsonSchemaException,
            IOException, SOSHibernateException, JocConfigurationException {
        validate(name, type, (IConfigurationObject) Globals.objectMapper.readValue(configBytes, JocInventory.CLASS_MAPPING.get(type)), null,
                accessToken);
    }

    public static void validate(String name, ConfigurationType type, IConfigurationObject config, String accessToken) throws SOSJsonSchemaException,
            IOException, SOSHibernateException, JocConfigurationException {
        validate(name, type, config, null, accessToken);
    }

    public static void validate(String name, ConfigurationType type, IConfigurationObject config, InventoryDBLayer dbLayer, String accessToken)
            throws SOSJsonSchemaException, IOException, SOSHibernateException, JocConfigurationException {
        if (ConfigurationType.WORKFLOW.equals(type)) {
            SOSHibernateSession session = null;
            try {
                if (dbLayer == null) {
                    session = Globals.createSosHibernateStatelessConnection("referenceValidate");
                    dbLayer = new InventoryDBLayer(session);
                }
                Workflow workflow = (Workflow) config;
                validateAddOrderInstructionArguments(name, workflow.getOrderPreparation(), dbLayer, accessToken);
            } finally {
                Globals.disconnect(session);
            }
        }
    }

    private static void validateAddOrderInstructionArguments(String workflowName, Requirements orderPreparation, InventoryDBLayer dbLayer,
            String accessToken) {
        List<DBItemInventoryConfiguration> dbWorkflows = dbLayer.getAddOrderWorkflowsByWorkflowName(workflowName);
        Predicate<DBItemInventoryConfiguration> addOrderInstructionArgumentAreValid = dbWorkflow -> {
            try {
                Workflow w = WorkflowConverter.convertInventoryWorkflow(dbWorkflow.getContent());
                validateAddOrderInstructionArguments(w.getInstructions(), orderPreparation);
                return true;
            } catch (Exception e) {
                return false;
            }
        };
        if (dbWorkflows != null) {
            String invalidWorkflows = dbWorkflows.stream().filter(addOrderInstructionArgumentAreValid.negate()).map(dbWorkflow -> {
                if (dbWorkflow.getValid() || dbWorkflow.getDeployed()) {
                    dbWorkflow.setValid(false);
                    dbWorkflow.setDeployed(false);
                    try {
                        JocInventory.updateConfiguration(dbLayer, dbWorkflow);
                    } catch (Exception e) {
                        //
                    }
                }
                return dbWorkflow.getPath();
            }).collect(Collectors.joining(", "));

            if (!invalidWorkflows.isEmpty() && accessToken != null) {
                if (invalidWorkflows.contains(", ")) {
                    ProblemHelper.postMessageAsHintIfExist("The workflows " + invalidWorkflows + " reference " + workflowName
                            + " and have invalid arguments in an AddOrder instruction", accessToken, null, null);
                } else {
                    ProblemHelper.postMessageAsHintIfExist("The workflow " + invalidWorkflows + " references " + workflowName
                            + " and has invalid arguments in an AddOrder instruction", accessToken, null, null);
                }
            }
        }
    }

    private static void validateAddOrderInstructionArguments(List<Instruction> instructions, Requirements orderPreparation) {
        if (instructions != null) {
            for (Instruction inst : instructions) {
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin fj = inst.cast();
                    for (Branch branch : fj.getBranches()) {
                        if (branch.getWorkflow() != null) {
                            validateAddOrderInstructionArguments(branch.getWorkflow().getInstructions(), orderPreparation);
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        validateAddOrderInstructionArguments(fl.getWorkflow().getInstructions(), orderPreparation);
                    }
                    break;
                case IF:
                    IfElse ifElse = inst.cast();
                    if (ifElse.getThen() != null) {
                        validateAddOrderInstructionArguments(ifElse.getThen().getInstructions(), orderPreparation);
                    }
                    if (ifElse.getElse() != null) {
                        validateAddOrderInstructionArguments(ifElse.getElse().getInstructions(), orderPreparation);
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = inst.cast();
                    if (tryCatch.getTry() != null) {
                        validateAddOrderInstructionArguments(tryCatch.getTry().getInstructions(), orderPreparation);
                    }
                    if (tryCatch.getCatch() != null) {
                        validateAddOrderInstructionArguments(tryCatch.getCatch().getInstructions(), orderPreparation);
                    }
                    break;
                case LOCK:
                    Lock lock = inst.cast();
                    if (lock.getLockedWorkflow() != null) {
                        validateAddOrderInstructionArguments(lock.getLockedWorkflow().getInstructions(), orderPreparation);
                    }
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    OrdersHelper.checkArguments(ao.getArguments(), orderPreparation);
                    // TODO check also Start-/Endpositions
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    if (cns.getSubworkflow() != null) {
                        validateAddOrderInstructionArguments(cns.getSubworkflow().getInstructions(), orderPreparation);
                    }
                    break;
                case CYCLE:
                    Cycle cycle = inst.cast();
                    if (cycle.getCycleWorkflow() != null) {
                        validateAddOrderInstructionArguments(cycle.getCycleWorkflow().getInstructions(), orderPreparation);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = inst.cast();
                    if (sticky.getSubworkflow() != null) {
                        validateAddOrderInstructionArguments(sticky.getSubworkflow().getInstructions(), orderPreparation);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        validateAddOrderInstructionArguments(opts.getBlock().getInstructions(), orderPreparation);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
}
