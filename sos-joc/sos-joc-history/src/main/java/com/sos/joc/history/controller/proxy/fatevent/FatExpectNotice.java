package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.board.BoardHelper;

import js7.data.board.NoticeId;

public class FatExpectNotice {

    private final NoticeId noticeId;

    public FatExpectNotice(NoticeId noticeId) {
        this.noticeId = noticeId;
    }

    public String getNoticeId() {
        if (noticeId == null) {
            return "";
        }
        return BoardHelper.getNoticeKeyShortString(noticeId);
    }

    public String getBoardPath() {
        if (noticeId == null) {
            return "";
        }
        return noticeId.boardPath().string();
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
