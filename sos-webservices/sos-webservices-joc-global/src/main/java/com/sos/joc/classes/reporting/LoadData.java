package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;

import io.vavr.control.Either;

public class LoadData extends AReporting {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadData.class);
    
    /**
     * 
     * @param monthFrom in the form yyyy-MM
     * @return
     */
    public static CompletableFuture<Either<Exception, Void>> writeCSVFiles(final String monthFrom) {
        String[] yearMonth = monthFrom.split("-");
        return writeCSVFiles(LocalDate.of(Integer.valueOf(yearMonth[0]).intValue(), Integer.valueOf(yearMonth[1]).intValue(), 1).atStartOfDay());
    }
    
    /**
     * 
     * @param monthFrom - LocalDateTime of the first day of a month at 00:00:00
     * @return
     */
    public static CompletableFuture<Either<Exception, Void>> writeCSVFiles(final LocalDateTime monthFrom) {
        return CompletableFuture.supplyAsync(() -> {
            SOSHibernateSession session = null;
            ScrollableResults jobResult = null;
            ScrollableResults orderResult = null;
            List<String> emptyMonths = new ArrayList<>();
            List<String> existingMonths = new ArrayList<>();

            try {
                session = Globals.createSosHibernateStatelessConnection("ReportingLoadData");
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, new HistoryFilter());
                
                LocalDateTime firstDayOfCurrentMonth = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1).atStartOfDay();
                LocalDateTime month = monthFrom;
                
                ReportingLoader jobsReporting = new ReportingLoader(ReportingType.JOBS);
                ReportingLoader ordersReporting = new ReportingLoader(ReportingType.ORDERS);
                
                while (month.isBefore(firstDayOfCurrentMonth)) {
                    jobResult = dbLayer.getCSV(jobsReporting, month);
                    boolean skipped = writeCSVFile(jobsReporting, month, dbLayer, emptyMonths, existingMonths);
                    if (!skipped) {
                        writeCSVFile(ordersReporting, month, dbLayer, emptyMonths, existingMonths);
                        
                        atomicRename(ordersReporting, month);
                        atomicRename(jobsReporting, month);
                    }
                    month = month.plusMonths(1);
                }

                return Either.right(null);
            } catch (Exception e) {
                return Either.left(e);
            } finally {
                if (jobResult != null) {
                    jobResult.close();
                }
                if (orderResult != null) {
                    orderResult.close();
                }
                Globals.disconnect(session);
                
                if (!emptyMonths.isEmpty()) {
                    LOGGER.info("[Reporting][loading] Skipped: No data for " + String.join(", ", emptyMonths));
                }
                if (!existingMonths.isEmpty()) {
                    LOGGER.info("[Reporting][loading] Skipped: Data already exists for " + String.join(", ", existingMonths));
                }
            }
        });
    }
    
    private static boolean writeCSVFile(ReportingLoader loader, LocalDateTime month, JobHistoryDBLayer dbLayer, List<String> emptyMonths,
            List<String> existingMonths) throws IOException {
        boolean skipped = true;
        ScrollableResults result = null;
        try {
            String _month = month.format(yearMonthFormatter);
            Path monthFile = getMonthFile(loader, _month);
            Path tmpMonthFile = getTmpMonthFile(loader, _month);
            Files.deleteIfExists(tmpMonthFile);
            if (Files.notExists(monthFile)) {
                result = dbLayer.getCSV(loader, month);
                if (result != null) {
                    if (result.next()) {
                        try (OutputStream output = Files.newOutputStream(tmpMonthFile)) {
                            output.write(loader.getHeadline());
                            output.write(getCsvBytes(result.get(0)));
                            while (result.next()) {
                                output.write(getCsvBytes(result.get(0)));
                            }
                            output.flush();
                            LOGGER.info("[Reporting][loading] Write " + loader.getType().name().toLowerCase() + " data for " + month + " ("
                                    + monthFile.toString() + ")");
                            skipped = false;
                        }
                    } else {
                        emptyMonths.add(_month);
                    }
                } else {
                    emptyMonths.add(_month);
                }
            } else {
                existingMonths.add(_month);
            }
        } finally {
            if (result != null) {
                result.close();
                result = null;
            }
        }
        return skipped;
    }
    
    private static void atomicRename(ReportingLoader loader, LocalDateTime month) throws IOException {
        String _month = month.format(yearMonthFormatter);
        atomicRename(loader, _month);
    }
    
    private static void atomicRename(ReportingLoader loader, String month) throws IOException {
        Path monthFile = getMonthFile(loader, month);
        Path tmpMonthFile = getTmpMonthFile(loader, month);
        Files.move(tmpMonthFile, monthFile, StandardCopyOption.ATOMIC_MOVE);
    }
    
    private static Path getMonthFile(ReportingLoader loader, String month) {
        return loader.getOutDir().resolve(month + ".csv");
    }
    
    private static Path getTmpMonthFile(ReportingLoader loader, String month) {
        return loader.getOutDir().resolve(month + ".csv~");
    }
    
    private static boolean checkCSVFileExists(Path outdir, LocalDateTime month) throws IOException {
        String _month = month.format(yearMonthFormatter);
        Path monthFile = outdir.resolve(_month + ".csv");
        return Files.notExists(monthFile);
    }
    
    private static boolean checkLastMonthCSVFileExists() throws IOException {
        return checkCSVFileExists(getDataDirectory(ReportingType.JOBS), LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1).atStartOfDay()
                .minusMonths(1));
    }
}
