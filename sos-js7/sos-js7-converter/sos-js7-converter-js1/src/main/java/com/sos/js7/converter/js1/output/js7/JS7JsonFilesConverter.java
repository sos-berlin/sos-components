package com.sos.js7.converter.js1.output.js7;

import java.nio.file.Path;
import java.util.List;

import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStream;
import com.sos.js7.converter.js1.common.runtime.CalendarsHelper;

public class JS7JsonFilesConverter {

    public static void convert(JS7Converter js7Converter, JS7ConverterResult result) {
        if (js7Converter.getPr().getJsonFiles().size() > 0) {
            ParserReport.INSTANCE.addAnalyzerRecord("JSON FILES", "START");
            for (Path file : js7Converter.getPr().getJsonFiles()) {
                try {
                    List<JobStream> jobStreams = JS7JobStreamsConverter.read(file);
                    ParserReport.INSTANCE.addAnalyzerRecord(file, "JOBSTREAM", "");

                    JS7JobStreamsConverter.convert(js7Converter, result, file, jobStreams);
                } catch (Throwable e) {
                    // calendar
                    try {
                        CalendarsHelper h = CalendarsHelper.convert(file);

                        ParserReport.INSTANCE.addAnalyzerRecord(file, "CALENDAR", "");
                    } catch (Throwable ee) {
                        ParserReport.INSTANCE.addAnalyzerRecord(file, "NEITHER JOBSTREAM NOR CALENDAR", "");
                    }
                }
            }
            ParserReport.INSTANCE.addAnalyzerRecord("JSON FILES", "END");
        }
    }

}
