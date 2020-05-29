package com.sos.webservices.order.initiator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.handler.IJocClusterHandler;

public class OrderInitiatorMain implements IJocClusterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorMain.class);

    private static final String IDENTIFIER = "dailyplan";
    private static final String PROPERTIES_FILE = "joc/dailyplan.properties";

    private final JocConfiguration jocConfig;
    private OrderInitiatorSettings settings;
    private Timer timer;

    public OrderInitiatorMain(JocConfiguration jocConfiguration) {
        jocConfig = jocConfiguration;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getMasterApiUser() {
        return IDENTIFIER;
    }

    @Override
    public String getMasterApiUserPassword() {
        return IDENTIFIER;
    }

    @Override
    public JocClusterAnswer start(List<MasterConfiguration> masters) {
        try {
            LOGGER.info(String.format("[%s]start", getIdentifier()));
            setSettings();
            if (settings.isRunOnStart()) {
                OrderInitiatorRunner o = new OrderInitiatorRunner(settings);
                o.run();
            }
            resetStartPlannedOrderTimer();

            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        }
    }

    @Override
    public JocClusterAnswer stop() {
        LOGGER.info(String.format("[%s]stop", getIdentifier()));

        // TODO

        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private void waitUntilFirstRun() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date firstRun = new Date();
        try {
            firstRun = formatter.parse(settings.getFirstRunAt());
        } catch (ParseException e1) {
            LOGGER.warn("Wrong format for start time " + settings.getFirstRunAt() + " Using default 00:00:00");
            try {
                firstRun = formatter.parse("00:00:00");
            } catch (ParseException e) {
                LOGGER.error("Could not parse date. Using now");
            }
        }
        Calendar first = Calendar.getInstance();
        first.setTime(firstRun);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, first.get(Calendar.HOUR));
        cal.set(Calendar.MINUTE, first.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, first.get(Calendar.SECOND));
        firstRun = cal.getTime();
        Date now = new Date();
        Long wait = 0L;
        Long diff = now.getTime() - firstRun.getTime();
        if (now.before(firstRun)) {
            wait = diff;
        } else {
            wait = 24 * 60 * 1000 - diff;
        }
        LOGGER.debug("Waiting for " + wait / 1000 + " seconds until " + settings.getFirstRunAt() + "(UTC)");
        try {
            java.lang.Thread.sleep(diff);
        } catch (InterruptedException e) {
            LOGGER.warn("Wait time has been interrupted " + e.getCause());
        }

    }

    private void resetStartPlannedOrderTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        waitUntilFirstRun();
        timer = new Timer();
        LOGGER.debug("Plans will be created every " + settings.getRunInterval() + " s");
        timer.schedule(new OrderInitiatorRunner(settings), 0, settings.getRunInterval() * 1000);
    }

    private void setSettings() throws Exception {
        String method = "getSettings";

        settings = new OrderInitiatorSettings();

        Properties conf = JocConfiguration.readConfiguration(jocConfig.getResourceDirectory().resolve(PROPERTIES_FILE).normalize());
        LOGGER.info(String.format("[%s]%s", method, conf));

        settings.setDayOffset(conf.getProperty("day_offset"));
        settings.setJobschedulerUrl(conf.getProperty("jobscheduler_url"));
        settings.setRunOnStart("true".equalsIgnoreCase(conf.getProperty("run_on_start", "true")));
        settings.setRunInterval(conf.getProperty("run_interval", "1440"));
        settings.setFirstRunAt(conf.getProperty("first_run_at", "00:00:00"));
        settings.setOrderTemplatesDirectory(conf.getProperty("order_templates_directory"));
        settings.setHibernateConfigurationFile(jocConfig.getHibernateConfiguration());
    }

}
