package com.sos.jobscheduler.event.master;

import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventPath;
import com.sos.jobscheduler.event.master.JobSchedulerEvent.EventSeq;
import com.sos.jobscheduler.event.master.handler.EventHandler;

public class EventHandlerTest {

    public static void main(String[] args) throws Exception {
        EventHandler eh = new EventHandler();
        try {
            eh.setIdentifier("test");
            eh.setBaseUrl("localhost", "4444");
            eh.createRestApiClient();

            Long eventId = new Long(0);
            JobSchedulerEvent em = eh.getEvents(EventPath.fatEvent, eventId);

            System.out.println(em.getEventSeq());
            if (em.getEventSeq().equals(EventSeq.NonEmpty)) {
                System.out.println(em.getStampeds());
                System.out.println(em.getLastStampedsEntry());
            } else if (em.getEventSeq().equals(EventSeq.Empty)) {

            } else if (em.getEventSeq().equals(EventSeq.Torn)) {
                throw new Exception(String.format("Torn event occured. Try to retry events ..."));
            } else {
                throw new Exception(String.format("unknown event seq type=%s", em.getEventSeq()));
            }
            System.out.println(em.getLastEventId());

        } catch (Exception e) {
            throw e;
        } finally {
            eh.closeRestApiClient();
        }
    }

}
