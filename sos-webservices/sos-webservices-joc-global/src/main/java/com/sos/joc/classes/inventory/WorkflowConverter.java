package com.sos.joc.classes.inventory;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.StickySubagent;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.InternalExecutableType;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;

public class WorkflowConverter {
    
    private final static String convertInstructions = String.join("|", InstructionType.POST_NOTICE.value(), InstructionType.EXPECT_NOTICE.value(),
            InstructionType.CONSUME_NOTICES.value(), InstructionType.LOCK.value());
    private final static Predicate<String> hasConvertInstruction = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + convertInstructions + ")\"")
            .asPredicate();

    public static Workflow convertInventoryWorkflow(String content) throws JsonMappingException, JsonProcessingException {
        return convertInventoryWorkflow(content, Workflow.class);
    }
    
    public static <T extends Workflow> T convertInventoryWorkflow(String content, Class<T> clazz) throws JsonMappingException, JsonProcessingException {
        if (content != null && !content.isEmpty()) {
            // for compatibility ReadNotice -> ExpectNotice
            content = content.replaceAll("(\"TYPE\"\\s*:\\s*)\"ReadNotice\"", "$1\"ExpectNotice\"");
            T workflow = Globals.objectMapper.readValue(content, clazz);
            if (hasConvertInstruction.test(content)) {
                convertInstructions(workflow.getInstructions());
            }
            addInternalExecutableType(workflow.getJobs());
            workflow.setVersion(Globals.getStrippedInventoryVersion());
            return workflow;
        }
        return null;
    }

    private static void convertInstructions(List<Instruction> invInstructions) {
        if (invInstructions != null) {
            for (int i = 0; i < invInstructions.size(); i++) {
                Instruction invInstruction = invInstructions.get(i);
                switch (invInstruction.getTYPE()) {
                case FORKLIST:
                    ForkList fl = invInstruction.cast();
                    if (fl.getWorkflow() != null) {
                        convertInstructions(fl.getWorkflow().getInstructions());
                    }
                    break;
                case FORK:
                    ForkJoin fj = invInstruction.cast();
                    if (fj.getBranches() != null) {
                        for (int j = 0; j < fj.getBranches().size(); j++) {
                            Branch invBranch = fj.getBranches().get(j);
                            if (invBranch.getWorkflow() != null) {
                                convertInstructions(invBranch.getWorkflow().getInstructions());
                            }
                        }
                    }
                    break;
                case IF:
                    IfElse ifElse = invInstruction.cast();
                    if (ifElse.getThen() != null) {
                        convertInstructions(ifElse.getThen().getInstructions());
                    }
                    if (ifElse.getElse() != null) {
                        convertInstructions(ifElse.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = invInstruction.cast();
                    if (tryCatch.getTry() != null) {
                        convertInstructions(tryCatch.getTry().getInstructions());
                    }
                    if (tryCatch.getCatch() != null) {
                        convertInstructions(tryCatch.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock lock = LockToLockDemandsConverter.lockToInventoryLockDemands(invInstruction.cast());
                    if (lock.getLockedWorkflow() != null) {
                        convertInstructions(lock.getLockedWorkflow().getInstructions());
                    }
                    break;
                case CYCLE:
                    Cycle cycle = invInstruction.cast();
                    if (cycle.getCycleWorkflow() != null) {
                        convertInstructions(cycle.getCycleWorkflow().getInstructions());
                    }
                    break;
                case POST_NOTICE:
                    invInstructions.set(i, NoticeToNoticesConverter.postNoticeToPostNotices(invInstruction.cast()));
                    break;
                case EXPECT_NOTICE:
                    invInstructions.set(i, NoticeToNoticesConverter.expectNoticeToExpectNotices(invInstruction.cast()));
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = invInstruction.cast();
                    if (cn.getSubworkflow() == null || cn.getSubworkflow().getInstructions() == null) {
                        cn.setSubworkflow(new Instructions(Collections.emptyList())); 
                    } else {
                        convertInstructions(cn.getSubworkflow().getInstructions());
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = invInstruction.cast();
                    if (sticky.getSubworkflow() != null) {
                        convertInstructions(sticky.getSubworkflow().getInstructions());
                    }
                    break;
                case OPTIONS:
                    Options opts = invInstruction.cast();
                    if (opts.getBlock() != null) {
                        convertInstructions(opts.getBlock().getInstructions());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static void addInternalExecutableType(Jobs jobs) {
        if (jobs != null && jobs.getAdditionalProperties() != null) {
            jobs.getAdditionalProperties().forEach((jobName, job) -> {
                addInternalExecutableType(job.getExecutable());
            });
        };
    }
    
    public static void addInternalExecutableType(Executable executable) {
        if (executable != null && executable.getTYPE() != null) {
            switch (executable.getTYPE()) {
            case ShellScriptExecutable:
            case ScriptExecutable:
                break;
            case InternalExecutable:
                ExecutableJava ej = executable.cast();
                if (ej.getInternalType() == null) {
                    if (ej.getScript() != null) {
                        ej.setInternalType(InternalExecutableType.JavaScript);
                    } else if (ej.getClassName() != null) {
                        if (ej.getClassName().startsWith("com.sos.jitl.jobs.")) {
                            ej.setInternalType(InternalExecutableType.JITL);
                        } else {
                            ej.setInternalType(InternalExecutableType.Java);
                        }
                    } else {
                        //default
                        ej.setInternalType(InternalExecutableType.JITL);
                    }
                }
                break;
            }
        }
    }

}
