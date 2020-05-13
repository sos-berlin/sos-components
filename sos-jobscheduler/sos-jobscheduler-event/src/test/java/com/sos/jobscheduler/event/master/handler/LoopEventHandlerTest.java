package com.sos.jobscheduler.event.master.handler;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.EventMeta.EventPath;
import com.sos.jobscheduler.event.master.bean.Event;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;
import com.sos.jobscheduler.event.notifier.DefaultNotifier;
import com.sos.jobscheduler.event.notifier.INotifier;

public class LoopEventHandlerTest extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopEventHandlerTest.class);

    public static void closeAfter(ILoopEventHandler eh, int seconds) {
        Thread thread = new Thread() {

            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s][start][closeAfter]%ss...", name, seconds));
                try {
                    Thread.sleep(seconds * 1_000);
                } catch (InterruptedException e) {
                    LOGGER.info(String.format("[%s][exception][closeAfter]%s", name, e.toString()), e);
                }
                eh.close();
                LOGGER.info(String.format("[%s][end][closeAfter]%ss", name, seconds));
            }
        };
        thread.start();
    }

    public LoopEventHandlerTest(Configuration config, EventPath path, Class<? extends IEntry> clazz, INotifier n) {
        super(config, path, clazz, n);
    }

    @Override
    public void onProcessingStart(Long eventId) {
        LOGGER.info("onProcessingStart: eventId=" + eventId);
    }

    @Override
    public void onProcessingEnd(Long eventId) {
        LOGGER.info("onProcessingEnd: eventId=" + eventId);
    }

    @Override
    public Long onEmptyEvent(Long eventId, Event event) {
        Long newEventId = super.onEmptyEvent(eventId, event);

        wait(1);
        return newEventId;
    }

    @Override
    public Long onNonEmptyEvent(Long eventId, Event event) {
        Long newEventId = super.onNonEmptyEvent(eventId, event);

        wait(1);
        return newEventId;
    }

    @Override
    public Long onTornEvent(Long eventId, Event event) {
        Long newEventId = super.onTornEvent(eventId, event);

        wait(1);
        return newEventId;
    }

    @Override
    public void onRestart(Long eventId, Event event) {
        super.onRestart(eventId, event);
        wait(1);
    }

    public static Configuration getTestConfig() throws Exception {
        Configuration config = new Configuration();

        Properties conf = new Properties();
        conf.put("jobscheduler_id", "jobscheduler2");
        conf.put("primary_master_uri", "http://localhost:4444");
        conf.put("primary_master_user", "test");
        conf.put("primary_master_user_password", "12345");
        // config.getMailer().load(conf);
        // config.getHandler().load(conf);
        // config.getHttpClient().load(conf);
        // config.getWebservice().load(conf);

        MasterConfiguration mc = new MasterConfiguration();
        mc.load(conf);
        config.addMaster(mc);
        return config;
    }

    public static void main(String[] args) throws Exception {

        INotifier notifier = new DefaultNotifier();
        // notifier = new Mailer(conf);

        LoopEventHandlerTest handler = new LoopEventHandlerTest(LoopEventHandlerTest.getTestConfig(), EventPath.fatEvent, Entry.class, notifier);
        handler.init(handler.getConfig().getMasters().get(0));

        LoopEventHandlerTest.closeAfter(handler, 40);// close after n seconds

        handler.start(new Long(0));
    }

}
