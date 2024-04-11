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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.inventory.model.report.Frequency;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JOCSOSShell;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.db.reporting.DBItemReport;
import com.sos.joc.db.reporting.DBItemReportRun;
import com.sos.joc.db.reporting.DBItemReportTemplate;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.reporting.ReportRunsUpdated;
import com.sos.joc.event.bean.reporting.ReportsUpdated;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.model.reporting.Report;
import com.sos.joc.model.reporting.ReportRunStateText;

import io.vavr.control.Either;

public class RunReport extends AReporting {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RunReport.class);
//    private static final String nodeCLOs = "--max-old-space-size=2048";
    
    public static CompletableFuture<Either<Exception, Void>> run(final Report in) {

        in.setMonthFrom(relativeDateToSpecificDate(in.getMonthFrom()));
        in.setMonthTo(relativeDateToSpecificDate(in.getMonthTo()));

        if (in.getMonthFrom() != null) { // automatically load report data before run report

            return LoadData.writeCSVFiles(in.getMonthFrom(), in.getMonthTo()).thenApply(e -> e.isLeft() ? e : _run(in));

        } else {
            return CompletableFuture.supplyAsync(() -> _run(in));
        }
    }
    
    private static String relativeDateToSpecificDate(String month) {
        if (month == null) {
            return null;
        }
        if (month.matches("\\d+\\s*[mMyY]")) { //month is relative
            Matcher m = Pattern.compile("(\\d+)\\s*([mMyY])").matcher(month);
            if (m.find()) {
                LocalDate ld = null;
                switch(m.group(1).toLowerCase()) { //unit
                case "m":
                    ld = LocalDate.now().minusMonths(Long.valueOf(m.group(0)).longValue());
                    break;
                case "y":
                    ld = LocalDate.now().minusYears(Long.valueOf(m.group(0)).longValue());
                    break;
                }
                
                return ld.getYear() + "-" + (ld.getMonthValue() < 10 ?  "" + ld.getMonthValue() : "0" + ld.getMonthValue());
            }
        }
        return month;
    }
    
    private static Either<Exception, Void> _run(final Report in) {
        Set<Path> tempDirs = new HashSet<>();
        List<DBItemReport> dbItems = new ArrayList<>();
        DBItemReportRun runDbItem = null;
        
        try {
            runDbItem = getRunDBItem(in);
            byte[] template = insertRunAndReadTemplate(runDbItem, in);
            String commonScript = getCommonScript(in, getCommandLineOptions());
            //TODO SOSTimeout timeout = new SOSTimeout(10, TimeUnit.Hours);
            for (Frequency f : in.getFrequencies()) {
                Path tempDir = runPerFrequency(f, commonScript, in, template);
                tempDirs.add(tempDir);
                dbItems.addAll(Files.list(tempDir).filter(file -> !file.getFileName().toString().startsWith(templateFilePrefix)).map(
                        file -> getHistoryDBItem(file, f)).collect(Collectors.toList()));
            }
            
            insert(in, runDbItem, dbItems);
            EventBus.getInstance().post(new ReportsUpdated());
            return Either.right(null);
        } catch (Exception e) {
            updateFailedRun(runDbItem, e);
            EventBus.getInstance().post(new ReportRunsUpdated());
            return Either.left(e);
        } finally {
            tempDirs.forEach(dir -> deleteTmpDir(dir));
        }
    }
    
    private static String getCommandLineOptions() {
        ConfigurationGlobalsJoc jocSettings = Globals.getConfigurationGlobalsJoc();
        ConfigurationEntry nodeCommandLineOptions = jocSettings.getNodeCommandLineOptions();
//        String nCLO = nodeCommandLineOptions.getValue();
//        if (nCLO == null || nCLO.isEmpty()) {
//            nCLO = nodeCommandLineOptions.getDefault();
//        }
//        if (nCLO == null || nCLO.isEmpty()) {
//            nCLO = nodeCLOs;
//        }
        return nodeCommandLineOptions.getValue();
    }

    private static String getCommonScript(final Report in, String commandLineOptions) {
        StringBuilder s = new StringBuilder().append("node ").append(commandLineOptions).append(" app/run-report.js -i data");
        if (in.getMonthFrom() != null) {
            s.append(" -s ").append(in.getMonthFrom());
        }
        if (in.getMonthTo() != null) {
            s.append(" -e ").append(in.getMonthTo());
        }
        if (in.getHits() != null) {
            s.append(" -n ").append(in.getHits());
        }
        return s.toString();
    }
    
    private static Path runPerFrequency(Frequency f, String commonScript, final Report in, byte[] template) throws IOException, JocBadRequestException {
        Path tempDir = null;
        try {
            tempDir = createTempDirectory();
            // reportingDir is working directory
            Path relativizeReportingDir = reportingDir.relativize(tempDir);
            Path templateFile = tempDir.resolve(templateFilePrefix + in.getTemplateName().intValue() + ".json");
            Path relativizeTemplateFile = reportingDir.relativize(templateFile);
            Files.write(templateFile, template);
            StringBuilder s = new StringBuilder(commonScript);
            s.append(" -t ").append(relativizeTemplateFile.toString().replace('\\', '/'));
            s.append(" -p ").append(f.strValue());
            s.append(" -o ").append(relativizeReportingDir.toString().replace('\\', '/'));
            String script = s.toString();
            //String script = commonScript + f.strValue() + " -t " + relativizeReportingDir + " -o " + relativizeReportingDir.toString().replace('\\', '/');
            LOGGER.info("[Reporting][run] " + script);
            SOSCommandResult cResult = JOCSOSShell.executeCommand(script, reportingDir);
            
            if (cResult.hasError()) {
                if (cResult.getException() != null) {
                    throw new JocBadRequestException(cResult.getException());
                } else if (cResult.getStdErr() != null && !cResult.getStdErr().isEmpty()) {
                    throw new JocBadRequestException(cResult.getStdErr());
                } else if (cResult.getExitCode() != 0) {
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
    
    private static DBItemReport getHistoryDBItem(final Path report, final Frequency frequency) throws UncheckedIOException {
        LocalDateTime localDateFrom = getLocalDateFrom(report);
        DBItemReport dbItem = new DBItemReport();
        dbItem.setId(null);
        dbItem.setDateFrom(getDate(localDateFrom));
        dbItem.setDateTo(getDate(getLocalDateTo(localDateFrom, frequency)));
        dbItem.setFrequency(frequency.intValue());
        dbItem.setReportFile(report);
        return dbItem;
    }
    
    private static DBItemReportRun getRunDBItem(final Report in) {
        return getRunDBItem(in, ReportRunStateText.IN_PROGRESS, Date.from(Instant.now()));
    }
    
    private static DBItemReportRun getRunDBItem(final Report in, final ReportRunStateText state, final Date now) {
        DBItemReportRun dbItem = new DBItemReportRun();
        dbItem.setId(null);
        dbItem.setPath(in.getPath());
        dbItem.setName(JocInventory.pathToName(in.getPath()));
        dbItem.setFolder(JOCResourceImpl.getParent(in.getPath()));
        dbItem.setTitle(in.getTitle());
        dbItem.setTemplateId(in.getTemplateName().intValue());
        dbItem.setHits(in.getHits());
        dbItem.setFrequencies(in.getFrequencies().stream().map(Frequency::intValue).sorted().map(i -> i.toString()).collect(Collectors.joining(",")));
        dbItem.setDateFrom(getDate(getLocalDateFrom(in.getMonthFrom())));
        dbItem.setDateTo(getDate(getLocalDateToOrNowIfNull(in.getMonthTo())));
        dbItem.setState(state.intValue());
        dbItem.setControllerId(in.getControllerId());
        dbItem.setModified(now);
        dbItem.setCreated(now);
        return dbItem;
    }
    
    private static Date getDate(final LocalDateTime localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    private static LocalDateTime getLocalDateFrom(final Path report) {
        String reportBaseFilename = report.getFileName().toString().replaceFirst("\\.json$", "");
        String[] fileNameParts = reportBaseFilename.split("[-_]");
        int year = Integer.valueOf(fileNameParts[0]).intValue();
        int month = 1;
        int dayOfMonth = 1;
        if (!reportBaseFilename.matches("\\d{4}") && !reportBaseFilename.matches("\\d{4}[-_]\\d{4}")) { // yearly or 3 years frequency
            if (fileNameParts[1].matches("Q[1-4]")) {
                month = (Integer.valueOf(fileNameParts[1].substring(1)) * 3) - 2;
            } else if (fileNameParts[1].matches("H[12]")) {
                month = (Integer.valueOf(fileNameParts[1].substring(1)) * 6) - 5;
            } else {
                month = Integer.valueOf(fileNameParts[1]).intValue();
            }
            if (fileNameParts.length > 2) {
                dayOfMonth = Integer.valueOf(fileNameParts[2]).intValue();
            }
        }
        return LocalDate.of(year, month, dayOfMonth).atStartOfDay();
    }
    
    private static LocalDateTime getLocalDateTo(LocalDateTime dateFrom, final Frequency frequency) {
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
    
//    private static Long insertRun(final DBItemReportRun runDbItem) throws Exception {
//        SOSHibernateSession session = null;
//        try {
//            session = Globals.createSosHibernateStatelessConnection("StoreReportRun");
//            session.save(runDbItem);
//            EventBus.getInstance().post(new ReportRunsUpdated());
//            return runDbItem.getId();
//        } finally {
//            Globals.disconnect(session);
//        }
//    }
    
    private static byte[] insertRunAndReadTemplate(final DBItemReportRun runDbItem, final Report in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("StoreReportRun");
            DBItemReportTemplate dbTemplate = session.get(DBItemReportTemplate.class, in.getTemplateName().intValue());
            session.save(runDbItem);
            EventBus.getInstance().post(new ReportRunsUpdated());
            return dbTemplate.getContent();
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void updateFailedRun(final DBItemReportRun runDbItem, Exception e) {
        SOSHibernateSession session = null;
        if (runDbItem != null) {
            try {
                session = Globals.createSosHibernateStatelessConnection("UpdateReportRun");
                runDbItem.setState(ReportRunStateText.FAILED.intValue());
                runDbItem.setErrorText(e.getMessage());
                runDbItem.setModified(Date.from(Instant.now()));
                session.update(runDbItem);
            } catch (Exception e1) {
                //TODO error handling?
            } finally {
                Globals.disconnect(session);
            }
        }
    }
    
    private static void insert(final Report in, DBItemReportRun runDbItem, Collection<DBItemReport> dbItems) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("ReportingStoreData");
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            
            Date now = Date.from(Instant.now());
            
            for (DBItemReport dbItem : dbItems) {
                //dbItem.setHits(runDbItem.getHits());
                //dbItem.setTemplateId(runDbItem.getTemplateId());
                String constraintHash = dbItem.hashConstraint(runDbItem.getTemplateId(), runDbItem.getHits(), runDbItem.getControllerId());
                dbItem.setConstraintHash(constraintHash);

                DBItemReport oldItem = dbLayer.getReport(constraintHash);
                if (oldItem != null) {
                    oldItem.setRunId(runDbItem.getId());
                    oldItem.setModified(now);
                    
                    session.update(oldItem);
                } else {
                    dbItem.setRunId(runDbItem.getId());
                    dbItem.setCreated(now);
                    dbItem.setModified(now);
                    dbItem.setContent(Files.readAllBytes(dbItem.getReportFile()));
                    
                    session.save(dbItem);
                }
                
            }
            
            runDbItem.setState(ReportRunStateText.SUCCESSFUL.intValue());
            runDbItem.setModified(now);
            session.update(runDbItem);
            
            Globals.commit(session);
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
