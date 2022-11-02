package com.sos.joc.history.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.common.Variables;

import js7.data.value.BooleanValue;
import js7.data.value.ListValue;
import js7.data.value.NumberValue;
import js7.data.value.ObjectValue;
import js7.data.value.StringValue;
import js7.data.value.Value;

public class HistoryUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryUtilTest.class);

    @Ignore
    @Test
    public void testToJsonString() throws Exception {

        Map<String, Value> m = new HashMap<>();
        m.put("string", StringValue.of("string value"));
        m.put("boolean", BooleanValue.True());
        m.put("numeric", NumberValue.of(1));
        m.put("list", getListValue());

        Variables v = HistoryUtil.toVariables(m);
        LOGGER.info(HistoryUtil.toJsonString(v));

    }

    private ListValue getListValue() {
        List<Value> l = new ArrayList<>();

        Map<String, Value> m = new HashMap<>();
        m.put("list_string", StringValue.of("list string value"));
        m.put("list_boolean", BooleanValue.False());
        m.put("list_numeric", NumberValue.of(2));
        m.put("list_list", getSubListValue());
        l.add(ObjectValue.of(m));

        return ListValue.of(l);
    }

    private ListValue getSubListValue() {
        List<Value> l = new ArrayList<>();

        Map<String, Value> m = new HashMap<>();
        m.put("sub_list_string", StringValue.of("sub list string value"));
        m.put("sub_list_boolean", BooleanValue.True());
        m.put("sub_list_numeric", NumberValue.of(3));
        l.add(ObjectValue.of(m));

        return ListValue.of(l);
    }
}
