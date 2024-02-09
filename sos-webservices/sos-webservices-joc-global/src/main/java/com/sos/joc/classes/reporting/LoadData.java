package com.sos.joc.classes.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * @param accessToken
     * @param jocError
     * @return
     */
    public static CompletableFuture<Either<Exception, Void>> writeCSVFiles(String monthFrom) {
        return CompletableFuture.supplyAsync(() -> {
            SOSHibernateSession session = null;
            ScrollableResults jobResult = null;
            ScrollableResults orderResult = null;
            try {
                session = Globals.createSosHibernateStatelessConnection("ReportingLoadData");
                JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, new HistoryFilter());
                
                List<String> emptyMonths = new ArrayList<>();
                List<String> existingMonths = new ArrayList<>();

                LocalDateTime firstDayOfCurrentMonth = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1).atStartOfDay();
                String[] yearMonth = monthFrom.split("-");
                LocalDateTime dateFrom = LocalDate.of(Integer.valueOf(yearMonth[0]).intValue(), Integer.valueOf(yearMonth[1]).intValue(), 1)
                        .atStartOfDay();
                
                ReportingLoader jobsReporting = new ReportingLoader(ReportingType.JOBS);
                ReportingLoader ordersReporting = new ReportingLoader(ReportingType.ORDERS);
                
                while (dateFrom.isBefore(firstDayOfCurrentMonth)) {
                    jobResult = dbLayer.getCSV(jobsReporting, dateFrom);
                    boolean skipped = writeCSVFile(jobsReporting, dateFrom, jobResult, emptyMonths, existingMonths);
                    if (!skipped) {
                        orderResult = dbLayer.getCSV(ordersReporting, dateFrom);
                        writeCSVFile(ordersReporting, dateFrom, orderResult, emptyMonths, existingMonths);
                    }
                    dateFrom = dateFrom.plusMonths(1);
                }

                if (!emptyMonths.isEmpty()) {
                    LOGGER.info("[Reporting][loading] Skipped: No data for " + String.join(", ", emptyMonths));
                }
                if (!existingMonths.isEmpty()) {
                    LOGGER.info("[Reporting][loading] Skipped: Data already exists for " + String.join(", ", existingMonths));
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
            }
        });
    }
    
    private static boolean writeCSVFile(ReportingLoader loader, LocalDateTime dateFrom, ScrollableResults result, List<String> emptyMonths,
            List<String> existingMonths) throws IOException {
        boolean skipped = true;
        String month = dateFrom.format(yearMonthFormatter);
        if (result != null) {
            Path monthFile = loader.getOutDir().resolve(month + ".csv");
            if (Files.notExists(monthFile)) {
                if (result.next()) {
                    try (OutputStream output = Files.newOutputStream(monthFile)) {
                        output.write(loader.getHeadline());
                        output.write(getCsvBytes(result.get(0)));
                        while (result.next()) {
                            output.write(getCsvBytes(result.get(0)));
                        }
                        output.flush();
                        LOGGER.info("[Reporting][loading] Write data for " + month + " (" + monthFile.toString() + ")");
                        skipped = false;
                    }
                } else {
                    emptyMonths.add(month);
                }
            } else {
                existingMonths.add(month);
            }
            result.close();
            result = null;
        } else {
            emptyMonths.add(month);
        }
        return skipped;
    }
}
