package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.reporting.AReporting.ReportingType;

public class ReportingLoader {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingLoader.class);
    private Path outDir;
    private String hql;
    private byte[] headline;
    private String dbTable;
    
    public ReportingLoader(ReportingType type) throws IOException {
        outDir = AReporting.getDataDirectory(type);
        headline = AReporting.getCsvHeadLine(type);
        hql = AReporting.getCsvHQL(type);
        dbTable = AReporting.DB_TABLE.get(type);
    }

    public Path getOutDir() {
        return outDir;
    }

    public String getColumnHql() {
        return hql;
    }

    public byte[] getHeadline() {
        return headline;
    }

    public String getDbTable() {
        return dbTable;
    }
}
