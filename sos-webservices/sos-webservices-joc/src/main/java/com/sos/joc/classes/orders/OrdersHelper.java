package com.sos.joc.classes.orders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import js7.data.order.Order;

public class OrdersHelper {

    public static final Map<Class<? extends Order.State>, String> groupByStateClasses = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Order.Fresh.class, "pending");
                    put(Order.Awaiting.class, "waiting");
                    put(Order.DelayedAfterError.class, "waiting");
                    put(Order.Forked.class, "waiting");
                    put(Order.Offering.class, "waiting");
                    put(Order.Broken.class, "failed");
                    put(Order.Failed.class, "failed");
                    put(Order.FailedInFork.class, "failed");
                    put(Order.FailedWhileFresh$.class, "failed");
                    put(Order.Ready$.class, "running");
                    put(Order.Processed$.class, "running");
                    put(Order.Processing$.class, "running");
                    put(Order.Finished$.class, "finished");
                    put(Order.Cancelled$.class, "finished");
                    put(Order.ProcessingCancelled$.class, "finished");
                }
            });

    public static final Map<String, String> groupByStates = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("Fresh", "pending");
            put("Awaiting", "waiting");
            put("DelayedAfterError", "waiting");
            put("Forked", "waiting");
            put("Offering", "waiting");
            put("Broken", "failed");
            put("Failed", "failed");
            put("FailedInFork", "failed");
            put("FailedWhileFresh", "failed");
            put("Ready", "running");
            put("Processed", "running");
            put("Processing", "running");
            put("Finished", "finished");
            put("Cancelled", "finished");
            put("ProcessingCancelled", "finished");
        }
    });
    
    public OrdersHelper() {
    }

}
