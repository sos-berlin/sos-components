package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.nio.file.Path;

import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.joc.classes.reporting.AReporting.ReportingType;

public class ReportingLoader {
    
    private Path outDir;
    private String hql;
    private byte[] headline;
    private String dbTable;
    private ReportingType type;
    private boolean withChildOrders;
    
    public ReportingLoader(ReportingType type, Dbms dbms) throws IOException {
        this.outDir = AReporting.getDataDirectory(type);
        this.headline = AReporting.getCsvHeadLine(type);
        this.hql = AReporting.getCsvHQL(type, dbms);
        this.dbTable = AReporting.DB_TABLE.get(type);
        this.type = type;
        this.withChildOrders = ReportingType.JOBS.equals(type);
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
    
    public boolean withoutChildOrders() {
        return !withChildOrders;
    }
}
