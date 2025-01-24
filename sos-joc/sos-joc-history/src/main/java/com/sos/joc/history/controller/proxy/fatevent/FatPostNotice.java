package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.commons.util.SOSString;
import com.sos.joc.history.controller.proxy.HistoryEventEntry;

import js7.base.time.Timestamp;
import js7.data.board.BoardPath;
import js7.data.board.Notice;
import js7.data.board.NoticeId;
import scala.Option;

public class FatPostNotice {

    private String noticeId;
    private String boardPath;
    private Date endOfLife;

    public FatPostNotice(NoticeId notice, Option<Timestamp> endOfLife) {
        if (notice != null) {
            setNoticeId(notice);
            setBoardPath(notice.boardPath());
            setEndOfLife(endOfLife);
        }
    }

    private void setNoticeId(NoticeId nId) {
        if (nId != null) {
            noticeId = nId.noticeKey().string();
        }
    }

    private void setBoardPath(BoardPath bp) {
        if (bp != null) {
            boardPath = bp.string();
        }
    }

    private void setEndOfLife(Option<Timestamp> t) {
        if (t.isDefined()) {
            endOfLife = HistoryEventEntry.getDate(t.get());
        }
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
