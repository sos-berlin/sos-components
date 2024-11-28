package com.sos.joc.history.controller.proxy.fatevent;

import com.sos.commons.util.SOSString;

import js7.data.board.NoticeId;

public class FatExpectNotice {

    private final String noticeId;
    private final String boardPath;

    public FatExpectNotice(NoticeId noticeId, String boardPath) {
        this.noticeId = noticeId.noticeKey().string();
        this.boardPath = boardPath;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public String getBoardPath() {
        return boardPath;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
