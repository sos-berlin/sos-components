package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.file.Path;

import com.sos.joc.classes.reporting.AReporting.ReportingType;

public class ReportingLoader {
    
    private Path outDir;
    private String hql;
    private byte[] headline;
    private String dbTable;
    private ReportingType type;
    
    public ReportingLoader(ReportingType type) throws IOException {
        outDir = AReporting.getDataDirectory(type);
        headline = AReporting.getCsvHeadLine(type);
        hql = AReporting.getCsvHQL(type);
        dbTable = AReporting.DB_TABLE.get(type);
        this.type = type;
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
    
    public ReportingType getType() {
        return type;
    }
}
