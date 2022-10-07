package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.commons.util.SOSString;
import com.sos.joc.history.controller.proxy.HistoryEventEntry;

import js7.base.time.Timestamp;
import js7.data.board.BoardPath;
import js7.data.board.Notice;
import js7.data.board.NoticeId;

public class FatPostNotice {

    private String noticeId;
    private String boardPath;
    private Date endOfLife;

    public FatPostNotice(Notice notice) {
        if (notice != null) {
            setNoticeId(notice.id());
            setBoardPath(notice.boardPath());
            setEndOfLife(notice.endOfLife());
        }
    }

    private void setNoticeId(NoticeId nId) {
        if (nId != null) {
            noticeId = nId.string();
        }
    }

    private void setBoardPath(BoardPath bp) {
        if (bp != null) {
            boardPath = bp.string();
        }
    }

    private void setEndOfLife(Timestamp t) {
        endOfLife = HistoryEventEntry.getDate(t);
    }

    public String getNoticeId() {
        return noticeId;
    }

    public String getBoardPath() {
        return boardPath;
    }

    public Date getEndOfLife() {
        return endOfLife;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }

}
