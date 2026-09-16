
package com.sos.joc.model.security.configuration.predicate;

import java.util.function.Predicate;

import com.sos.auth.predicate.joc.AuditLog;
import com.sos.auth.predicate.joc.Calendars;
import com.sos.auth.predicate.joc.Cluster;
import com.sos.auth.predicate.joc.DailyPlan;


public class JocPermissionsPredicate {

    private final static String prefix = "sos:products:joc";
//    private Administration administration;
    private final Cluster cluster = new Cluster(prefix);
//    private Inventory inventory;
    private final Calendars calendars = new Calendars(prefix);
//    private Documentations documentations;
    private final AuditLog auditLog = new AuditLog(prefix);
    private final DailyPlan dailyPlan = new DailyPlan(prefix);
//    private FileTransfer fileTransfer;
//    private Notification notification;
//    private Encipherment encipherment;
//    private Reports reports;
//    private Others others;

    public JocPermissionsPredicate() {
    }
    
    public static Predicate<String> createPredicate(String prefix, String str) {
        return s -> (prefix + ":" + str).startsWith(s);
    }

    public Predicate<String> getGetLog() {
        return createPredicate(prefix, "get_log");
    }

//    public Administration getAdministration() {
//        return administration;
//    }

    public Cluster getCluster() {
        return cluster;
    }

//    public Inventory getInventory() {
//        return inventory;
//    }

    public Calendars getCalendars() {
        return calendars;
    }

//    public Documentations getDocumentations() {
//        return documentations;
//    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

//    public DailyPlan getDailyPlan() {
//        return dailyPlan;
//    }
//
//    public FileTransfer getFileTransfer() {
//        return fileTransfer;
//    }
//
//    public Notification getNotification() {
//        return notification;
//    }
//
//    public Encipherment getEncipherment() {
//        return encipherment;
//    }
//
//    public Reports getReports() {
//        return reports;
//    }
//
//    public Others getOthers() {
//        return others;
//    }

}
