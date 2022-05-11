package com.sos.js7.converter.autosys.common.v12.job.attr.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;

public class ConditionsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionsTest.class);

    @Ignore
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        String c = "(v(app.varA) = \"X\") and f(app.Job1) & s(app.Job2)";
        // c = "v(app.varA) = \"X\" & (f(app.Job1) or s(app.Job2))";
        c = "v(gatl.dbgatwwp) = \"up\" & s(gatl.ga_week_roll_ww)=\"x\" & d(gatl.ga_actualize_amr) & t(gatl.ga_actualize_apac) & s(gatl.ga_actualize_euro)";

        List<Object> conditions = Conditions.parse(c);
        LOGGER.info("--- RESULT WITH OPERATORS ----");
        for (Object o : conditions) {
            if (o instanceof ArrayList) {
                LOGGER.info("GROUP: ");
                List<Object> l = (ArrayList<Object>) o;
                for (Object oo : l) {
                    LOGGER.info("    " + SOSString.toString(oo));
                }

            } else {
                LOGGER.info(SOSString.toString(o));
            }
        }

        LOGGER.info("--- RESULT BY TYPES ----");
        Map<ConditionType, List<Condition>> map = Conditions.getByType(conditions);
        map.entrySet().forEach(e -> {
            LOGGER.info(e.getKey().toString());
            for (Condition con : e.getValue()) {
                LOGGER.info("    " + SOSString.toString(con));
            }
        });
    }

}
