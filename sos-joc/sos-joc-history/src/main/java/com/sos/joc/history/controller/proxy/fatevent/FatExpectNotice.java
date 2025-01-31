package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.commons.util.SOSString;

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
        return noticeId.plannedNoticeKey().toShortString();
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
