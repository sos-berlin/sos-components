package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Date;

import com.sos.commons.util.SOSString;
import com.sos.joc.history.controller.proxy.HistoryEventEntry;

import js7.base.time.Timestamp;
import js7.data.board.NoticeId;
import scala.Option;

public class FatPostNotice {

    private NoticeId noticeId;
    private Date endOfLife;

    public FatPostNotice(NoticeId notice, Option<Timestamp> endOfLife) {
        if (notice != null) {
            noticeId = notice;
            setEndOfLife(endOfLife);
        }
    }

    private void setEndOfLife(Option<Timestamp> t) {
        if (t.isDefined()) {
            endOfLife = HistoryEventEntry.getDate(t.get());
        }
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

    public Date getEndOfLife() {
        return endOfLife;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }

}
