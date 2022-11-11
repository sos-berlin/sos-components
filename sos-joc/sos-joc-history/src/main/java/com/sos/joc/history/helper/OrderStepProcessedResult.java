package com.sos.joc.history.helper;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.history.controller.model.HistoryModel;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.yade.commons.Yade;
import com.sos.yade.commons.result.YadeTransferResult;
import com.sos.yade.commons.result.YadeTransferResultSerializer;

import js7.data.value.Value;

public class OrderStepProcessedResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStepProcessedResult.class);

    private Map<String, Value> returnValues;
    private YadeTransferResult yadeTransferResult;

    public OrderStepProcessedResult(FatEventOrderStepProcessed eos, String controllerId, CachedOrder co, CachedOrderStep cos) {
        returnValues = eos.getOutcome() == null ? null : eos.getOutcome().getNamedValues();
        if (returnValues != null) {
            Value yadeTransfer = returnValues.get(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES);
            if (yadeTransfer != null) {
                // copy without yade serialized value
                returnValues = returnValues.entrySet().stream().filter(e -> !e.getKey().equals(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES)).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                String serialized = yadeTransfer.convertToString();
                if (!SOSString.isEmpty(serialized)) {
                    YadeTransferResultSerializer<YadeTransferResult> serializer = new YadeTransferResultSerializer<YadeTransferResult>();
                    try {
                        yadeTransferResult = serializer.deserialize(serialized);
                    } catch (Throwable e) {
                        LOGGER.warn(String.format("[%s][%s][%s][job name=%s,pos=%s][cannot deserialize]%s", controllerId, co.getWorkflowPath(), co
                                .getOrderId(), cos.getJobName(), cos.getWorkflowPosition(), e.toString()), e);
                    }
                }
            }
            // copy without returnCode
            returnValues = returnValues.entrySet().stream().filter(e -> !e.getKey().equals(HistoryModel.RETURN_CODE_KEY)).collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    public Map<String, Value> getReturnValues() {
        return returnValues;
    }

    public YadeTransferResult getYadeTransferResult() {
        return yadeTransferResult;
    }
}
