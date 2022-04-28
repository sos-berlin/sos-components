package com.sos.js7.converter.commons.report;

import java.util.ArrayList;
import java.util.List;

public class CSVRecords {

    private final Class<? extends Enum<?>> header;
    private final List<Iterable<?>> records;

    public CSVRecords(Class<? extends Enum<?>> header) {
        this.header = header;
        this.records = new ArrayList<>();
    }

    public Class<? extends Enum<?>> getHeader() {
        return header;
    }

    public void addRecord(Iterable<?> record) {
        this.records.add(record);
    }

    public List<Iterable<?>> getRecords() {
        return records;
    }

}
