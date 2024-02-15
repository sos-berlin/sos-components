package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCSOSShell;
import com.sos.joc.db.reporting.DBItemReportHistory;
import com.sos.joc.db.reporting.DBItemReportRun;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.reporting.Frequency;
import com.sos.joc.model.reporting.RunFilter;

import io.vavr.control.Either;

public class RunReport extends AReporting {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RunReport.class);
    
    public static CompletableFuture<Either<Exception, Void>> run(final RunFilter in) {
        if (in.getDateFrom() != null) { //automatically load reort data before run report
            return LoadData.writeCSVFiles(in.getDateFrom()).thenApply(e -> {
                if (e.isLeft()) {
                    return e;
                }
                return _run(in);
            });
        } else {
            return CompletableFuture.supplyAsync(() -> _run(in));
        }
    }
    
    private static Either<Exception, Void> _run(final RunFilter in) {
        Set<Path> tempDirs = new HashSet<>();
        List<DBItemReportHistory> dbItems = new ArrayList<>();
        try {
            String commonScript = getCommonScript(in);
            //TODO SOSTimeout timeout = new SOSTimeout(10, TimeUnit.Hours);
            for (Frequency f : in.getFrequencies()) {
                Path tempDir = runPerFrequency(f, commonScript);
                tempDirs.add(tempDir);
                dbItems.addAll(Files.list(tempDir).map(file -> getHistoryDBItem(file, f, in)).collect(Collectors.toList()));
            }
            
            insert(in, dbItems);
            return Either.right(null);
        } catch (Exception e) {
            return Either.left(e);
        } finally {
            tempDirs.forEach(dir -> deleteTmpDir(dir));
        }
    }
    
    private static String getCommonScript(final RunFilter in) {
        StringBuilder s = new StringBuilder()
                .append("node bin/src/index.js -i data -t bin/templates/template_")
                .append(in.getTemplateId())
                .append(".json");
        if (in.getDateFrom() != null) {
            s.append(" -s ").append(in.getDateFrom());
        }
        if (in.getSize() != null) {
            s.append(" -n ").append(in.getSize());
        }
        s.append(" -p ");
        return s.toString();
    }
    
    private static Path runPerFrequency(Frequency f, String commonScript) throws IOException, JocBadRequestException {
        Path tempDir = null;
        try {
            tempDir = createTempDirectory();
            // reportingDir is working directory
            String script = commonScript + f.strValue() + " -o " + reportingDir.relativize(tempDir).toString();
            LOGGER.info("[Reporting][run] " + script);
            SOSCommandResult cResult = JOCSOSShell.executeCommand(script, reportingDir);

            if (cResult.getExitCode() != 0) {
                if (cResult.getException() != null) {
                    throw new JocBadRequestException(cResult.getException());
                } else if (cResult.getStdErr() != null && !cResult.getStdErr().trim().isEmpty()) {
                    throw new JocBadRequestException(cResult.getStdErr().trim());
                } else {
                    throw new JocBadRequestException("Unknown error when calling: " + cResult.getCommand());
                }
            }
            return tempDir;
        } catch (Exception e) {
            deleteTmpDir(tempDir);
            throw e;
        }
    }
    
    private static void deleteTmpDir(final Path tempDir) {
        if (tempDir != null) {
            try {
                Files.list(tempDir).forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e2) {
                        //
                    }
                });
                Files.deleteIfExists(tempDir);
            } catch (IOException e1) {
                //;
            }
        }
    }
    
    private static DBItemReportHistory getHistoryDBItem(final Path report, final Frequency frequency, final RunFilter in) throws UncheckedIOException {
        LocalDateTime localDateFrom = getLocalDateFrom(report);
        DBItemReportHistory dbItem = new DBItemReportHistory();
        dbItem.setId(null);
        dbItem.setDateFrom(getDate(localDateFrom));
        dbItem.setDateTo(getDate(getLocalDateTo(localDateFrom, frequency)));
        dbItem.setFrequency(frequency.intValue());
        dbItem.setSize(in.getSize());
        dbItem.setTemplateId(in.getTemplateId());
        try {
            dbItem.setContent(Files.readAllBytes(report));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return dbItem;
    }
    
    private static DBItemReportRun getRunDBItem(final RunFilter in) {
        DBItemReportRun dbItem = new DBItemReportRun();
        dbItem.setId(null);
        dbItem.setName(in.getName());
        dbItem.setTitle(in.getTitle());
        dbItem.setTemplateId(in.getTemplateId());
        dbItem.setSize(in.getSize());
        dbItem.setFrequencies(in.getFrequencies().stream().map(Frequency::intValue).sorted().map(i -> i.toString()).collect(Collectors.joining(",")));
        dbItem.setDateFrom(getDate(getLocalDateFrom(in.getDateFrom())));
        return dbItem;
    }
    
    private static Date getDate(final LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    private static LocalDateTime getLocalDateFrom(final Path report) {
        String[] fileNameParts = report.getFileName().toString().replaceFirst("\\.json$", "").split("[-_]");
        int year = Integer.valueOf(fileNameParts[0]).intValue();
        int month = 1;
        if (fileNameParts[1].matches("Q[1-4]")) {
            month = (Integer.valueOf(fileNameParts[1].substring(1)) * 3) - 2;
        } else if (fileNameParts[1].matches("H[12]")) {
            month = (Integer.valueOf(fileNameParts[1].substring(1)) * 6) - 5;
        } else {
            month = Integer.valueOf(fileNameParts[1]).intValue();
        }
        int dayOfMonth = fileNameParts.length > 2 ? Integer.valueOf(fileNameParts[2]).intValue() : 1;
        return LocalDate.of(year, month, dayOfMonth).atStartOfDay();
    }
    
    private static LocalDateTime getLocalDateFrom(final String yyyymmdd) {
        String[] dateParts = yyyymmdd.split("-");
        int year = Integer.valueOf(dateParts[0]).intValue();
        int month = Integer.valueOf(dateParts[1]).intValue();
        int dayOfMonth = Integer.valueOf(dateParts[2]).intValue();
        return LocalDate.of(year, month, dayOfMonth).atStartOfDay();
    }
    
    private static LocalDateTime getLocalDateTo(LocalDateTime dateFrom, final Frequency frequency) {
        // TODO minus one millisecond?
        switch(frequency) {
        case WEEKLY:
            return dateFrom.plusWeeks(1).minusSeconds(1);
        case TWO_WEEKS:
            return dateFrom.plusWeeks(2).minusSeconds(1);
        case MONTHLY:
            return dateFrom.plusMonths(1).minusSeconds(1);
        case THREE_MONTHS:
            return dateFrom.plusMonths(3).minusSeconds(1);
        case SIX_MONTHS:
            return dateFrom.plusMonths(6).minusSeconds(1);
        case YEARLY:
            return dateFrom.plusYears(1).minusSeconds(1);
        case THREE_YEARS:
            return dateFrom.plusYears(3).minusSeconds(1);
        }
        return null;
    }
    
    private static void insert(final RunFilter in, Collection<DBItemReportHistory> dbItems) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("ReportingStoreData");
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            
            Date now = Date.from(Instant.now());
            
            DBItemReportRun runDbItem = getRunDBItem(in);
            runDbItem.setCreated(now);
            
            session.save(runDbItem);

            for (DBItemReportHistory dbItem : dbItems) {
                dbItem.setCreated(now);
                dbItem.setRunId(runDbItem.getId());
                session.save(dbItem); 
            }
            Globals.commit(session);
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
