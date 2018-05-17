package com.sos.jobscheduler.event.master;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.EventMeta.EventSeq;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandler;

public class EventHandlerTest {

    public static void main(String[] args) throws Exception {
        EventHandler eh = new EventHandler(EventPath.fatEvent, Entry.class);
        try {
            eh.setIdentifier("test");
            eh.setBaseUri("localhost", "4444");
            eh.createRestApiClient();

            Long eventId = new Long(0);
            Event event = eh.getEvent(eventId);

            System.out.println(event.getType());
            if (event.getType().equals(EventSeq.NonEmpty)) {
                System.out.println(event.getStampeds().size());
            } else if (event.getType().equals(EventSeq.Empty)) {

            } else if (event.getType().equals(EventSeq.Torn)) {
                throw new Exception(String.format("Torn event occured. Try to retry events ..."));
            } else {
                throw new Exception(String.format("unknown event seq type=%s", event.getType()));
            }
            System.out.println(event.getLastEventId());

        } catch (Exception e) {
            throw e;
        } finally {
            eh.closeRestApiClient();
        }
    }

}
