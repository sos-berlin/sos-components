package com.sos.js7.converter.autosys.output.js7;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.commons.JS7ConverterMain;

public class JS7ConverterTest {

    @Ignore
    @Test
    public void test() throws Exception {
        Path input = Paths.get("src/test/resources/input/xml");
        Path outputDir = Paths.get("src/test/resources/output");
        Path reportDir = Paths.get("src/test/resources/output/report");
        Path archive = Paths.get("src/test/resources/js7_converted.tar.gz");

        JS7Converter.CONFIG.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        JS7Converter.CONFIG.getAgentConfig().withMappings("abcd=agent;xyz=agent_cluster");// .withForcedName("my_agent_name");
        JS7Converter.CONFIG.getMockConfig().withUnixScript("$HOME/MockScript.sh");
        JS7Converter.CONFIG.getScheduleConfig().withDefaultWorkingDayCalendarName("AnyDays").withDefaultNonWorkingDayCalendarName(null)
                .withPlanOrders(true).withSubmitOrders(true);
        JS7Converter.CONFIG.getJobConfig().withForcedGraceTimeout(15).withForcedParallelism(1).withForcedFailOnErrWritten(true).withScriptNewLine(
                "\n");
        JS7Converter.CONFIG.getSubFolderConfig().withMappings("aapg=2;ebzc=0;wmad=0;abcd=0").withSeparator("_");

        JS7Converter.convert(new XMLJobParser(), input, outputDir, reportDir);
        JS7ConverterMain.createArchiveFile(outputDir, archive);
    }
}
