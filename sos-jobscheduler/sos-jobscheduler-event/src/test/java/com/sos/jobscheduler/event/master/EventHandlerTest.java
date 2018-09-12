package com.sos.jobscheduler.event.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.EventMeta.EventSeq;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.master.handler.EventHandler;
import com.sos.jobscheduler.event.master.handler.ISender;

public class EventHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerTest.class);

    public static void main(String[] args) throws Exception {
        ISender sender = null;
        EventHandler eh = new EventHandler(sender, EventPath.fatEvent, Entry.class);
        try {
            eh.setIdentifier("test");
            eh.setBaseUri("localhost", "4444");
            eh.createRestApiClient();

            Long eventId = new Long(0);
            eh.useLogin(true);
            String token = eh.login("test", "12345");
            Event event = eh.getAfterEvent(eventId, token);

            LOGGER.info("TYPE=" + event.getType());
            if (event.getType().equals(EventSeq.NonEmpty)) {
                LOGGER.info("size=" + event.getStamped().size());
            } else if (event.getType().equals(EventSeq.Empty)) {
                LOGGER.info("lastEventId=" + event.getLastEventId());
            } else if (event.getType().equals(EventSeq.Torn)) {
                throw new Exception(String.format("Torn event occured. Try to retry events ..."));
            } else {
                throw new Exception(String.format("unknown event seq type=%s", event.getType()));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            eh.logout();
            eh.closeRestApiClient();
        }
    }

}
