package com.sos.joc.classes.reporting;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.sos.inventory.model.report.ReportPeriod;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JOCSOSShell;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.db.reporting.DBItemReport;
import com.sos.joc.db.reporting.DBItemReportRun;
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
    private static final String className = "com.sos.reports.ReportGenerator";

    public static CompletableFuture<Either<Exception, Void>> run(final Report in) {

        in.setMonthFrom(relativeDateToSpecificDateFrom(in.getMonthFrom()));
        in.setMonthTo(relativeDateToSpecificDateTo(in.getMonthTo()));

        if (in.getMonthFrom() != null) { // automatically load report data before run report

            return LoadData.writeCSVFiles(in.getMonthFrom(), in.getMonthTo()).thenApply(e -> e.isLeft() ? e : _run(in));

        } else {
            return CompletableFuture.supplyAsync(() -> _run(in));
        }
    }

    // protected for JUnit test
    protected static String relativeDateToSpecificDateFrom(String month) {
        return relativeDateToSpecificDate(month, false);
    }

    // protected for JUnit test
    protected static String relativeDateToSpecificDateTo(String month) {
        return relativeDateToSpecificDate(month, true);
    }

    private static String relativeDateToSpecificDate(String month, boolean to) {
        // TODO for unit y -> always 01-yyyy
        // for unit q -> always 01-yyyy, 04-yyyy, 07-yyyy or 10-yyyy
        if (month == null) {
            return null;
        }
        if (month.matches("[+-]*\\d+\\s*[mMQqyY]")) { // month is relative
            Matcher m = Pattern.compile("[+-]*(\\d+)\\s*([mMQqyY])").matcher(month);
            if (m.find()) {
                LocalDate ld = null;
                switch (m.group(2).toLowerCase()) { // unit
                case "m":
                    ld = LocalDate.now().withDayOfMonth(1).minusMonths(Long.valueOf(m.group(1)).longValue());
                    break;
                case "q":
                    ld = LocalDate.now().withDayOfMonth(1);
                    switch (ld.getMonth()) {
                    case JANUARY:
                    case FEBRUARY:
                    case MARCH:
                        ld = ld.withMonth(1);
                        break;
                    case APRIL:
                    case MAY:
                    case JUNE:
                        ld = ld.withMonth(4);
                        break;
                    case JULY:
                    case AUGUST:
                    case SEPTEMBER:
                        ld = ld.withMonth(7);
                        break;
                    case OCTOBER:
                    case NOVEMBER:
                    case DECEMBER:
                        ld = ld.withMonth(10);
                        break;
                    }
                    ld = ld.minusMonths(Long.valueOf(m.group(1)).longValue() * 3);
                    if (to) {
                        ld = ld.plusMonths(3).minusDays(1);
                    }
                    break;
                case "y":
                    ld = LocalDate.now().withDayOfMonth(1).withMonth(1).minusYears(Long.valueOf(m.group(1)).longValue());
                    if (to) {
                        ld = ld.plusYears(1).minusDays(1);
                    }
                    break;
                }

                String leadingMonthZero = ld.getMonthValue() < 10 ? "0" : "";
                return ld.getYear() + "-" + leadingMonthZero + ld.getMonthValue();
            }
        }
        return month;
    }

    private static Either<Exception, Void> _run(final Report in) {
        Set<Path> tempDirs = new HashSet<>();
        List<DBItemReport> dbItems = new ArrayList<>();
        DBItemReportRun runDbItem = null;
        runDbItem = getRunDBItem(in);

        DBItemReport reportDBItem = new DBItemReport();
        reportDBItem.setDateFrom(runDbItem.getDateFrom());

        try {
            insertRun(runDbItem);

            String commonScript = getCommonScript(in, getCommandLineOptions());
            // TODO SOSTimeout timeout = new SOSTimeout(10, TimeUnit.Hours);
            for (Frequency f : in.getFrequencies()) {

                reportDBItem.setFrequency(f.intValue());
                boolean reportExists = reportExists(reportDBItem, in);
                if (!reportExists) {
                    Path tempDir = runPerFrequency(f, commonScript);
                    tempDirs.add(tempDir);
                    dbItems.addAll(Files.list(tempDir).filter(file -> file.getFileName().toString().startsWith(reportFilePrefix)).map(
                            file -> getHistoryDBItem(file, f)).collect(Collectors.toList()));
                }
            }

            runDbItem.setReportCount(dbItems.size());
            insert(in, runDbItem, dbItems);
            updateRun(runDbItem);
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
        ConfigurationEntry reportJavaOptions = jocSettings.getReportJavaOptions();
        return reportJavaOptions.getValue();
    }

    private static String getCommonScript(final Report in, String commandLineOptions) {
        StringBuilder s = new StringBuilder().append("\"").append(Paths.get(System.getProperty("java.home"), "bin", "java").toString()).append("\" ")
                .append(commandLineOptions).append(" -cp \"../resources/joc").append(File.pathSeparator).append("../webapps/joc/WEB-INF/lib/*\" ")
                .append(className).append(" -i data").append(" -r ").append(in.getTemplateName().getJavaClass());
        if (in.getMonthFrom() != null) {
            s.append(" -s ").append(in.getMonthFrom());
        }
        if (in.getMonthTo() != null) {
            s.append(" -e ").append(in.getMonthTo());
        }
        if (in.getHits() != null) {
            s.append(" -n ").append(in.getHits());
        }
        if (in.getPeriod() != null) {
            if (in.getPeriod().getLength() != null) {
                s.append(" -a ").append(in.getPeriod().getLength());
            }
            if (in.getPeriod().getStep() != null) {
                s.append(" -b ").append(in.getPeriod().getStep());
            }
        }
        if (in.getSort() != null) {
            s.append(" -f ").append(in.getSort());
        }
        if (in.getControllerId() != null && !in.getControllerId().isBlank()) {
            s.append(" -c ").append(in.getControllerId());
        }
        return s.toString();
    }

    private static Path runPerFrequency(Frequency f, String commonScript) throws IOException, JocBadRequestException {
        Path tempDir = null;
        try {
            tempDir = createTempDirectory();
            // reportingDir (jetty_base/reporting) is working directory
            Path relativizeTempDir = reportingDir.relativize(tempDir);
            StringBuilder s = new StringBuilder(commonScript);
            s.append(" -p ").append(f.strValue());
            s.append(" -o ").append(relativizeTempDir.toString().replace('\\', '/'));
            String script = s.toString();
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
                // ;
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

        LocalDateTime lDateFrom = getLocalDateFrom(in.getMonthFrom());
        LocalDateTime lDateTo = getLocalDateToOrLastMonthIfNull(in.getMonthTo());
        if (in.getMonthTo() != null && lDateFrom.isAfter(lDateTo)) {
            throw new JocBadRequestException("'monthFrom' has to be older than 'monthTo' in report configuration '" + in.getPath() + "'");
        }

        DBItemReportRun dbItem = new DBItemReportRun();
        dbItem.setId(null);
        dbItem.setPath(in.getPath());
        dbItem.setName(JocInventory.pathToName(in.getPath()));
        dbItem.setFolder(JOCResourceImpl.getParent(in.getPath()));
        dbItem.setTitle(in.getTitle());
        dbItem.setTemplateId(in.getTemplateName().intValue());
        dbItem.setHits(in.getHits());
        dbItem.setFrequencies(in.getFrequencies().stream().map(Frequency::intValue).sorted().map(i -> i.toString()).collect(Collectors.joining(",")));
        dbItem.setSort(in.getSort().intValue());
        ReportPeriod period = in.getPeriod();
        if (period == null) {
            period = new ReportPeriod();
        }

        dbItem.setPeriodLength(period.getLength());
        dbItem.setPeriodStep(period.getStep());

        dbItem.setDateFrom(getDate(lDateFrom));
        dbItem.setDateTo(getDate(lDateTo));
        dbItem.setState(state.intValue());
        dbItem.setControllerId(in.getControllerId());
        dbItem.setReportCount(0);
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
        String reportBaseFilename = report.getFileName().toString().replaceFirst("^" + reportFilePrefix, "").replaceFirst("\\.json$", "");
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
            if (fileNameParts.length > 3) { // _frequencyId is always trailing part
                dayOfMonth = Integer.valueOf(fileNameParts[2]).intValue();
            }
        }
        return LocalDate.of(year, month, dayOfMonth).atStartOfDay();
    }

    private static LocalDateTime getLocalDateTo(LocalDateTime dateFrom, final Frequency frequency) {
        switch (frequency) {
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

    private static void insertRun(final DBItemReportRun runDbItem) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("StoreReportRun");

            runDbItem.setModified(Date.from(Instant.now()));
            session.save(runDbItem);
            EventBus.getInstance().post(new ReportRunsUpdated());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void updateRun(final DBItemReportRun runDbItem) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("StoreReportRun");

            runDbItem.setState(ReportRunStateText.SUCCESSFUL.intValue());
            runDbItem.setModified(Date.from(Instant.now()));
            session.update(runDbItem);
            EventBus.getInstance().post(new ReportRunsUpdated());
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
                // TODO error handling?
            } finally {
                Globals.disconnect(session);
            }
        }
    }

    private static boolean reportExists(DBItemReport dbItemReport, final Report in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("ReportExists");

            ReportingDBLayer dbLayer = new ReportingDBLayer(session);

            String constraintHash = dbItemReport.hashConstraint(in.getTemplateName().intValue(), in.getHits(), in.getControllerId(), in.getSort()
                    .intValue(), in.getPeriod().getLength(), in.getPeriod().getStep());

            DBItemReport oldItem = dbLayer.getReport(constraintHash);
            return (oldItem != null);

        } finally {
            Globals.disconnect(session);
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
                String constraintHash = dbItem.hashConstraint(runDbItem.getTemplateId(), runDbItem.getHits(), runDbItem.getControllerId(), runDbItem
                        .getSort(), runDbItem.getPeriodLength(), runDbItem.getPeriodStep());
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

            Globals.commit(session);
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
