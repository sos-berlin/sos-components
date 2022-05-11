package com.sos.js7.converter.commons.output;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.controller.model.workflow.Workflow;
import com.sos.js7.converter.commons.JS7ExportObjects;

public class OutputWriterTest {

    @Ignore
    @Test
    public void test() throws IOException {
        JS7ExportObjects<Workflow> wl = new JS7ExportObjects<>();
        wl.addItem(Paths.get("/1/w.json"), new Workflow());
        wl.addItem(Paths.get("/2/w.json"), new Workflow());

        wl.addItem(Paths.get("3/w-001.json"), new Workflow());
        wl.addItem(Paths.get("4/w-001.json"), new Workflow());
        wl.addItem(Paths.get("5/w-001.json"), new Workflow());

        OutputWriter.write(Paths.get("src/test/resources/output"), wl);
    }
}
