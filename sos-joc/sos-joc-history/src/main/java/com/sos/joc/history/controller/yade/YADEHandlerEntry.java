package com.sos.joc.history.controller.yade;

import java.io.Serializable;
import java.util.Base64;

import com.sos.joc.history.helper.CachedOrder;
import com.sos.joc.history.helper.CachedOrderStep;

public class YADEHandlerEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String workflowPath;
    private final String orderId;

    private final Long stepHistoryId;
    private final String stepJobName;
    private final String stepWorkflowPosition;

    // Convert the String to byte[] immediately → reduces memory usage
    // Reason:
    // 1. Java Strings use UTF-16 internally (2 bytes per character), so large strings take more memory.
    // 2. Storing the raw bytes (GZIP compressed) is much more memory-efficient.
    // 3. By keeping only byte[] in the queue, we avoid keeping both the original String and deserialized objects in memory simultaneously.
    // 4. Deserialization happens later in the worker, minimizing peak heap usage.
    private final byte[] data;

    // Due to asynchronous processing, create a copy from byte[] instead of using by reference
    // all parameters (String, Long etc) are immutable - do not need to be copied for asynchronous processing
    public YADEHandlerEntry(CachedOrder co, CachedOrderStep cos, String yadeReturnValues) {
        workflowPath = co.getWorkflowPath();
        orderId = co.getOrderId();

        stepHistoryId = cos.getId();
        stepJobName = cos.getJobName();
        stepWorkflowPosition = cos.getWorkflowPosition();

        // yadeReturnValues is not null/empty - see OrderStepProcessedResult
        data = Base64.getDecoder().decode(yadeReturnValues);
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getStepHistoryId() {
        return stepHistoryId;
    }

    public String getStepJobName() {
        return stepJobName;
    }

    public String getStepWorkflowPosition() {
        return stepWorkflowPosition;
    }

    public byte[] getData() {
        return data;
    }

}
