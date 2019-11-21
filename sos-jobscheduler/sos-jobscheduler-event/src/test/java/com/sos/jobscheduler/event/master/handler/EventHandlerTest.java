package com.sos.jobscheduler.event.master.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.EventMeta.EventSeq;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.event.master.configuration.handler.HttpClientConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;

public class EventHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerTest.class);

    public static void main(String[] args) throws Exception {
        EventHandler handler = new EventHandler(new Configuration(), EventPath.fatEvent, Entry.class);
        try {
            handler.setIdentifier("test");
            handler.setUri("http://localhost:4444");
            handler.useLogin(true);

            handler.getHttpClient().create(new HttpClientConfiguration());
            String token = handler.login("test", "12345");
            Event event = handler.getAfterEvent(new Long(0), token);

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
            handler.logout();
            handler.getHttpClient().close();
        }
    }

}
