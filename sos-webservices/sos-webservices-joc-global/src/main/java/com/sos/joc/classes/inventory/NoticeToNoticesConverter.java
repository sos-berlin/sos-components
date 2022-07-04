package com.sos.joc.classes.inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;

import js7.data_for_java.value.JExpression;

public class NoticeToNoticesConverter {
    
    private final static String noticeInstructions = String.join("|", InstructionType.POST_NOTICE.value(), InstructionType.EXPECT_NOTICE.value());
    private final static Predicate<String> hasNoticeInstruction = Pattern.compile("\"TYPE\"\\s*:\\s*\"(" + noticeInstructions + ")\"")
            .asPredicate();

    public static com.sos.sign.model.instruction.PostNotices postNoticeToSignPostNotices(com.sos.sign.model.instruction.PostNotice pn) {
        com.sos.sign.model.instruction.PostNotices pns = new com.sos.sign.model.instruction.PostNotices();
        pns.setTYPE(InstructionType.POST_NOTICES);
        if (pn.getBoardPath() != null) {
            pns.setBoardPaths(Collections.singletonList(pn.getBoardPath()));
        }
        return pns;
    }

    public static com.sos.sign.model.instruction.ExpectNotices expectNoticeToSignExpectNotices(com.sos.sign.model.instruction.ExpectNotice en) {
        com.sos.sign.model.instruction.ExpectNotices ens = new com.sos.sign.model.instruction.ExpectNotices();
        ens.setTYPE(InstructionType.EXPECT_NOTICES);
        if (en.getBoardPath() != null) {
            ens.setBoardPaths(JExpression.quoteString(en.getBoardPath()));
        }
        return ens;
    }

    public static PostNotices postNoticeToPostNotices(PostNotice pn) {
        PostNotices pns = new PostNotices();
        pns.setTYPE(InstructionType.POST_NOTICES);
        pns.setPosition(pn.getPosition());
        pns.setPositionString(pn.getPositionString());
        if (pn.getNoticeBoardName() != null) {
            pns.setNoticeBoardNames(Collections.singletonList(pn.getNoticeBoardName()));
        }
        return pns;
    }

    public static ExpectNotices expectNoticeToExpectNotices(ExpectNotice en) {
        ExpectNotices ens = new ExpectNotices();
        ens.setTYPE(InstructionType.EXPECT_NOTICES);
        ens.setPosition(en.getPosition());
        ens.setPositionString(en.getPositionString());
        if (en.getNoticeBoardName() != null) {
            ens.setNoticeBoardNames(JExpression.quoteString(en.getNoticeBoardName()));
        }
        return ens;
    }
    
    public static Workflow convertInventoryWorkflow(String content) throws JsonMappingException, JsonProcessingException {
        if (content != null && !content.isEmpty()) {
            // for compatibility ReadNotice -> ExpectNotice
            content = content.replaceAll("(\"TYPE\"\\s*:\\s*)\"ReadNotice\"", "$1\"ExpectNotice\"");
            Workflow workflow = Globals.objectMapper.readValue(content, Workflow.class);
            if (hasNoticeInstruction.test(content)) {
                convertInstructions(workflow.getInstructions());
            }
            return workflow;
        }
        return null;
    }
    
    public static List<String> expectNoticeBoardsToList(String noticeBoardNames) {
        return Arrays.asList(noticeBoardNames.replaceAll("[|&\\(\\)'\"]", " ").replaceAll("  +", " ").trim().split(" ")).stream().distinct().collect(
                Collectors.toList());
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
                    Lock lock = invInstruction.cast();
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
                    invInstructions.set(i, postNoticeToPostNotices(invInstruction.cast()));
                    break;
                case EXPECT_NOTICE:
                    invInstructions.set(i, expectNoticeToExpectNotices(invInstruction.cast()));
                    break;
                default:
                    break;
                }
            }
        }
    }

}
