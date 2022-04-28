package com.sos.js7.converter.commons;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.workflow.Workflow;

public class JS7ExportObjectsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ExportObjectsTest.class);

    @Ignore
    @Test
    public void test() {
        JS7ExportObjects<Workflow> w = new JS7ExportObjects<>();
        w.addItem(Paths.get("/w1.json"), new Workflow());
        w.addItem(Paths.get("/w2.json"), new Workflow());

        for (JS7ExportObjects<Workflow>.JS7ExportObject item : w.getItems()) {
            LOGGER.info(String.format("[%s]%s", Workflow.class.getSimpleName(), item.getUniquePath().getPath()));
        }
    }
}
