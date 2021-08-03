package com.sos.joc.classes.inventory;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;

public class JsonConverter {
    
    private final static Predicate<String> hasForkListInstruction = Pattern.compile("\"TYPE\"\\s*:\\s*\"" + InstructionType.FORKLIST.value() + "\"").asPredicate();
    
    public static com.sos.sign.model.workflow.Workflow readConvertedWorkflow(String json) throws JsonParseException, JsonMappingException, IOException {
        
        com.sos.sign.model.workflow.Workflow signWorkflow = Globals.objectMapper.readValue(json, com.sos.sign.model.workflow.Workflow.class);
        
        if (signWorkflow.getInstructions() != null) {
            // at the moment the converter is only necessary for ForkList instructions
            if (hasForkListInstruction.test(json)) {
                convertInstructions(Globals.objectMapper.readValue(json, Workflow.class).getInstructions(), signWorkflow.getInstructions());
            }
        }
        
        return signWorkflow;
    }
    
    private static void convertInstructions(List<Instruction> invInstructions, List<com.sos.sign.model.instruction.Instruction> signInstructions) {
        if (invInstructions != null) {
            for (int i = 0; i < invInstructions.size(); i++) {
                Instruction invInstruction = invInstructions.get(i);
                com.sos.sign.model.instruction.Instruction signInstruction = signInstructions.get(i);
                switch (invInstruction.getTYPE()) {
                case FORKLIST:
                    ForkList fl = invInstruction.cast();
                    com.sos.sign.model.instruction.ForkList sfl = signInstruction.cast();
                    convertForkList(fl, sfl);
                    if (fl.getWorkflow() != null) {
                        convertInstructions(fl.getWorkflow().getInstructions(), sfl.getWorkflow().getInstructions());
                    }
                    break;
                case FORK:
                    ForkJoin fj = invInstruction.cast();
                    com.sos.sign.model.instruction.ForkJoin sfj = signInstruction.cast();
                    for (int j = 0; j < fj.getBranches().size(); j++) {
                        Branch invBranch = fj.getBranches().get(j);
                        if (invBranch.getWorkflow() != null) {
                            convertInstructions(invBranch.getWorkflow().getInstructions(), sfj.getBranches().get(j).getWorkflow().getInstructions());
                        }
                    }
                    break;
                case IF:
                    IfElse ifElse = invInstruction.cast();
                    com.sos.sign.model.instruction.IfElse sIfElse = signInstruction.cast();
                    if (ifElse.getThen() != null) {
                        convertInstructions(ifElse.getThen().getInstructions(), sIfElse.getThen().getInstructions());
                    }
                    if (ifElse.getElse() != null) {
                        convertInstructions(ifElse.getElse().getInstructions(), sIfElse.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tryCatch = invInstruction.cast();
                    com.sos.sign.model.instruction.TryCatch sTryCatch = signInstruction.cast();
                    if (tryCatch.getTry() != null) {
                        convertInstructions(tryCatch.getTry().getInstructions(), sTryCatch.getTry().getInstructions());
                    }
                    if (tryCatch.getCatch() != null) {
                        convertInstructions(tryCatch.getCatch().getInstructions(), sTryCatch.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock lock = invInstruction.cast();
                    if (lock.getLockedWorkflow() != null) {
                        com.sos.sign.model.instruction.Lock sLock = signInstruction.cast();
                        convertInstructions(lock.getLockedWorkflow().getInstructions(), sLock.getLockedWorkflow().getInstructions());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static void convertForkList(ForkList fl, com.sos.sign.model.instruction.ForkList sfl) {
        sfl.setChildren("$" + fl.getChildren());
        sfl.setChildToArguments("(x) => $x");
        sfl.setChildToId("(x, i) => $i + '-' + $x." + fl.getChildToId());
    }

}
