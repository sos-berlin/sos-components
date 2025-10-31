package com.sos.js7.converter.js1.output.js7;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendar;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStream;
import com.sos.js7.converter.js1.common.runtime.CalendarsHelper;

public class JS7JsonFilesConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7JsonFilesConverter.class);

    public static void convert(JS12JS7Converter js7Converter, JS7ConverterResult result) {
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

                        if (h.hasCalendars()) {
                            for (JS1Calendar c : h.getCalendars().getCalendars()) {
                                js7Converter.addJS1Calendar(c);
                            }

                            String paths = SOSString.join(h.getCalendars().getCalendars(), n -> n.getPath());
                            LOGGER.info("[convert][json file][calendar][" + file + "][found]" + paths);
                            ParserReport.INSTANCE.addAnalyzerRecord(file, "CALENDARS", paths);
                        } else {
                            LOGGER.info("[convert][json file][calendar][" + file + "]no calendars found");
                            ParserReport.INSTANCE.addAnalyzerRecord(file, "CALENDARS", "no calendars found");
                        }
                    } catch (Throwable ee) {
                        ParserReport.INSTANCE.addAnalyzerRecord(file, "NEITHER JOBSTREAM NOR CALENDAR", "");
                    }
                }
            }
            ParserReport.INSTANCE.addAnalyzerRecord("JSON FILES", "END");
        }
    }

}
