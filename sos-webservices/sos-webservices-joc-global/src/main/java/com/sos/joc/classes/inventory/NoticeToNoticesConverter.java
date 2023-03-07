package com.sos.joc.classes.inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;

import js7.data_for_java.value.JExpression;

public class NoticeToNoticesConverter {
    
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
    
    public static List<String> expectNoticeBoardsToList(String noticeBoardNames) {
        if (noticeBoardNames == null) {
            return Collections.emptyList();
        }
        return expectNoticeBoardsToStream(noticeBoardNames).collect(Collectors.toList());
    }
    
    public static Stream<String> expectNoticeBoardsToStream(String noticeBoardNames) {
        if (noticeBoardNames == null) {
            return Stream.empty();
        }
        return Arrays.asList(noticeBoardNames.replaceAll("[|&\\(\\)'\"]", " ").replaceAll("  +", " ").trim().split(" ")).stream().filter(
                n -> !SOSString.isEmpty(n)).distinct();
    }

}
