package com.sos.joc.monitoring.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSSerializer;
import com.sos.controller.model.event.EventType;
import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.SerializedResult;

public class HistoryMonitoringModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitoringModelTest.class);

    @Ignore
    @Test
    public void testSerializing() throws Exception {
        HistoryOrderStepBean bean = new HistoryOrderStepBean(EventType.OrderProcessingStarted, 123L, "myControllerId", 1L);
        // bean = null;

        CopyOnWriteArraySet<AHistoryBean> payloads = new CopyOnWriteArraySet<>();
        HashMap<Long, HistoryOrderStepBean> longerThan = new HashMap<>();

        if (bean != null) {
            payloads.add(bean);
            longerThan.put(1L, bean);
        }

        HistoryMonitoringModel model = new HistoryMonitoringModel();
        byte[] result = new SOSSerializer<SerializedResult>().serializeCompressed2bytes(model.new SerializedResult(payloads, longerThan));
        LOGGER.info("---bytes---:" + result);
    }

    @Ignore
    @Test
    public void testRound() throws Exception {
        BigDecimal bg = new BigDecimal(1.1);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);
        bg = bg.setScale(0, RoundingMode.HALF_UP);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);

        LOGGER.info("----------------");

        bg = new BigDecimal(1.8);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);
        bg = bg.setScale(0, RoundingMode.HALF_UP);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);

    }

}
