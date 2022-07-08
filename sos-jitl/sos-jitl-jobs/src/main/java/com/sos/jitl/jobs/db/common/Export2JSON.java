package com.sos.jitl.jobs.db.common;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sos.commons.hibernate.SOSHibernateSQLExecutor;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.jitl.jobs.common.JobLogger;

public class Export2JSON {

    public static void export(ResultSet resultSet, Path outputFile, JobLogger logger) throws Exception {
        if (resultSet == null) {
            throw new Exception("missing ResultSet");
        }
        if (outputFile == null) {
            throw new Exception("missing outputFile");
        }

        boolean removeOutputFile = false;

        ByteArrayOutputStream baos = null;
        JsonGenerator gen = null;
        try {
            Instant start = Instant.now();

            int dataRows = 0;
            int columnCount = resultSet.getMetaData().getColumnCount();
            String[] columnLabels = SOSHibernateSQLExecutor.getColumnLabels(resultSet);

            baos = new ByteArrayOutputStream();
            gen = new JsonFactory().createGenerator(baos, JsonEncoding.UTF8);
            gen.useDefaultPrettyPrinter();
            gen.writeStartArray();
            while (resultSet.next()) {
                gen.writeStartObject();
                for (int i = 1; i <= columnCount; ++i) {
                    Object o = resultSet.getObject(i);
                    String label = columnLabels[i - 1];
                    if (o == null) {
                        gen.writeStringField(label, null);
                    } else {
                        if (o instanceof Number) {
                            if (o instanceof BigDecimal) {
                                gen.writeNumberField(label, (BigDecimal) o);
                            } else if (o instanceof BigInteger) {
                                gen.writeNumberField(label, (BigInteger) o);
                            } else if (o instanceof Integer) {
                                gen.writeNumberField(label, (Integer) o);
                            } else if (o instanceof Long) {
                                gen.writeNumberField(label, (Long) o);
                            } else if (o instanceof Float) {
                                gen.writeNumberField(label, (Float) o);
                            } else if (o instanceof Short) {
                                gen.writeNumberField(label, (Short) o);
                            } else {
                                gen.writeStringField(label, o.toString());
                            }
                        } else if (o instanceof Boolean) {
                            gen.writeBooleanField(label, (Boolean) o);
                        } else {
                            gen.writeStringField(label, SOSHibernateSQLExecutor.sqlValueToString(o));
                        }
                    }
                }
                gen.writeEndObject();
                dataRows++;

                if (logger != null) {
                    int count = dataRows;
                    if (count % 1_000 == 0) {
                        logger.info("[export]%s entries processed ...", count);
                    }
                }
            }
            gen.writeEndArray();
            gen.close();
            gen = null;

            if (logger != null) {
                logger.info("[export][%s]total data rows written=%s, duration=%s", outputFile, dataRows, SOSDate.getDuration(start, Instant.now()));
            }

            try (OutputStream os = new FileOutputStream(outputFile.toFile())) {
                baos.writeTo(os);
            }
        } catch (Throwable e) {
            removeOutputFile = true;
            String f = outputFile.toString();
            try {
                f = outputFile.toAbsolutePath().toString();
            } catch (Throwable ee) {

            }
            throw new Exception(String.format("[%s]%s", f, e.toString()), e);
        } finally {
            if (gen != null) {
                try {
                    gen.close();
                } catch (Throwable e) {
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (Throwable e) {
                }
            }
            if (removeOutputFile) {
                try {
                    SOSPath.deleteIfExists(outputFile);
                } catch (Exception ex) {
                }
            }
        }
    }
}
