package com.sos.jobscheduler.history.helper;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogLevel;

public class ChunkParser {

    private final String chunk;
    private Date date;
    private LogLevel logLevel = LogLevel.Info;

    public ChunkParser(String val, Date chunkDate) {
        chunk = val;
        date = chunkDate;
    }

    // TODO
    // parse level, date ...
    public void parse() {
        Pattern pattern = Pattern.compile("(\\[DEBUG\\])|(\\[WARN\\])|(\\[ERROR\\])|(\\[TRACE\\])");
        Matcher matcher = pattern.matcher(chunk);
        if (matcher.find()) {
            switch (matcher.group(0)) {
            case "[DEBUG]":
                logLevel = LogLevel.Debug;
                break;
            case "[WARN]":
                logLevel = LogLevel.Warn;
                break;
            case "[ERROR]":
                logLevel = LogLevel.Error;
                break;
            case "[TRACE]":
                logLevel = LogLevel.Trace;
                break;
            }
        }
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public Date getDate() {
        return date;
    }
}
