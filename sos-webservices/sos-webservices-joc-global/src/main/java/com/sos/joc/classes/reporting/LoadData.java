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

import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;

import io.vavr.control.Either;

public class LoadData extends AReporting {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadData.class);
    
    /**
     * 
     * @param monthFrom in the form yyyy-MM
     * @param monthTo in the form yyyy-MM
     * @return
     */
    public static CompletableFuture<Either<Exception, Void>> writeCSVFiles(final String monthFrom, final String monthTo) {
        return writeCSVFiles(getLocalDateFrom(monthFrom), getLocalDateTo(monthTo));
    }
    
    /**
     * 
     * @param monthFrom - LocalDateTime of the first day of a month at 00:00:00
     * @param monthTo - LocalDateTime of the last day of a month at 23:59:59
     * @return
     */
    public static CompletableFuture<Either<Exception, Void>> writeCSVFiles(final LocalDateTime monthFrom, final LocalDateTime monthTo) {
        LocalDateTime firstDayOfCurrentMonth = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1).atStartOfDay();
        
        if (!monthFrom.isBefore(firstDayOfCurrentMonth)) {
            throw new JocBadRequestException("monthFrom has to be in the past");
        }
//        if (monthTo != null) {
//            if (!monthTo.isBefore(firstDayOfCurrentMonth)) {
//                throw new JocBadRequestException("monthTo has to be in the past");
//            }
//            if (monthTo.isBefore(monthFrom)) {
//                throw new JocBadRequestException("monthTo must not be older than monthFrom");
//            }
//        }
        
        final LocalDateTime toMonth = (monthTo == null || !monthTo.isBefore(firstDayOfCurrentMonth)) ? firstDayOfCurrentMonth : monthTo;
        
        
        return CompletableFuture.supplyAsync(() -> {
            SOSHibernateSession session = null;
            List<String> emptyMonths = new ArrayList<>();
            List<String> existingMonths = new ArrayList<>();

            try {
                session = Globals.createSosHibernateStatelessConnection("ReportingLoadData");
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, new HistoryFilter());
                
                LocalDateTime month = monthFrom;
                Dbms dbms = session.getFactory().getDbms();
                
                ReportingLoader jobsReporting = new ReportingLoader(ReportingType.JOBS, dbms);
                ReportingLoader ordersReporting = new ReportingLoader(ReportingType.ORDERS, dbms);
                
                while (month.isBefore(toMonth)) {
                    if (!writeCSVFile(jobsReporting, month, dbLayer, emptyMonths, existingMonths)) {
                        writeCSVFile(ordersReporting, month, dbLayer, emptyMonths, existingMonths);
                    }
                    month = month.plusMonths(1);
                }

                return Either.right(null);
            } catch (Exception e) {
                return Either.left(e);
            } finally {
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
        String _month = month.format(yearMonthFormatter);
        Path monthFile = getMonthFile(loader, _month);
        if (Files.notExists(monthFile)) {
            return writeCSVFile(loader, month, monthFile, dbLayer, emptyMonths, existingMonths);
        } else {
            existingMonths.add(_month);
        }
        return true;
    }
    
    private synchronized static boolean writeCSVFile(ReportingLoader loader, LocalDateTime month, Path monthFile, JobHistoryDBLayer dbLayer,
            List<String> emptyMonths, List<String> existingMonths) throws IOException {
        
        String _month = month.format(yearMonthFormatter);
        
        // check "exists" again because the result could be change in between after synchronizing this function 
        if (Files.exists(monthFile)) {
            existingMonths.add(_month);
            return true;
        }
        
        Path tmpMonthFile = getTmpMonthFile(loader, _month);
        Files.deleteIfExists(tmpMonthFile);
        ScrollableResults<String> result = null;
        
        try {
            result = dbLayer.getCSV(loader, month);
            if (result != null && result.next()) {
                try (OutputStream output = Files.newOutputStream(tmpMonthFile)) {
                    output.write(loader.getHeadline());
                    output.write(getCsvBytes(result.get()));
                    while (result.next()) {
                        output.write(getCsvBytes(result.get()));
                    }
                    output.flush();
                    
                    atomicRename(loader, _month);
                    LOGGER.info("[Reporting][loading] Write " + loader.getType().name().toLowerCase() + " data for " + _month + " (" + monthFile
                            .toString() + ")");
                    
                    return false;
                }
            } else {
                emptyMonths.add(_month);
            }
        } finally {
            if (result != null) {
                result.close();
                result = null;
            }
        }
        return true;
    }
    
    private synchronized static void atomicRename(ReportingLoader loader, String month) throws IOException {
        Path monthFile = getMonthFile(loader, month);
        Path tmpMonthFile = getTmpMonthFile(loader, month);
        if (Files.exists(tmpMonthFile) && !Files.exists(monthFile)) {
            Files.deleteIfExists(monthFile);
            Files.move(tmpMonthFile, monthFile, StandardCopyOption.ATOMIC_MOVE);
        }
    }
    
    private static Path getMonthFile(ReportingLoader loader, String month) {
        return loader.getOutDir().resolve(month + ".csv");
    }
    
    private static Path getTmpMonthFile(ReportingLoader loader, String month) {
        return loader.getOutDir().resolve(month + ".csv~");
    }
}
