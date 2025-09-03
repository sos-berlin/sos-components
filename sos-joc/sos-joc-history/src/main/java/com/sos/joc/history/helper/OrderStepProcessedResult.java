package com.sos.joc.history.helper;

import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.joc.history.controller.model.HistoryModel;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.joc.history.controller.yade.YADEHandler;
import com.sos.joc.history.controller.yade.YADEHandlerEntry;
import com.sos.yade.commons.Yade;

import js7.data.value.Value;

public class OrderStepProcessedResult {

    private Map<String, Value> returnValues;

    public OrderStepProcessedResult(FatEventOrderStepProcessed eos, String controllerId, CachedOrder co, CachedOrderStep cos,
            YADEHandler yadeHandler) {
        returnValues = eos.getOutcome() == null ? null : eos.getOutcome().getNamedValues();
        if (returnValues != null) {
            Value yadeReturnValues = returnValues.get(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES);
            if (yadeReturnValues != null) {
                // copy without YADE serialized value
                returnValues = returnValues.entrySet().stream().filter(e -> !e.getKey().equals(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES)).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                String v = yadeReturnValues.convertToString();
                if (!SOSString.isEmpty(v)) {
                    yadeHandler.add(new YADEHandlerEntry(co, cos, v));
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

}
