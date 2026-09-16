
package com.sos.auth.predicate;

import java.util.function.Predicate;

import com.sos.auth.predicate.joc.Administration;
import com.sos.auth.predicate.joc.AuditLog;
import com.sos.auth.predicate.joc.Calendars;
import com.sos.auth.predicate.joc.Cluster;
import com.sos.auth.predicate.joc.DailyPlan;
import com.sos.auth.predicate.joc.Documentations;
import com.sos.auth.predicate.joc.Encipherment;
import com.sos.auth.predicate.joc.FileTransfer;
import com.sos.auth.predicate.joc.Inventory;
import com.sos.auth.predicate.joc.Notification;
import com.sos.auth.predicate.joc.Others;
import com.sos.auth.predicate.joc.Reports;


public class JocPermissionsPredicate {

    private final static String prefix = "sos:products:joc";
    private Administration administration = new Administration(prefix);
    private final Cluster cluster = new Cluster(prefix);
    private Inventory inventory = new Inventory(prefix);;
    private final Calendars calendars = new Calendars(prefix);
    private Documentations documentations = new Documentations(prefix);
    private final AuditLog auditLog = new AuditLog(prefix);
    private final DailyPlan dailyPlan = new DailyPlan(prefix);
    private FileTransfer fileTransfer = new FileTransfer(prefix);;
    private Notification notification = new Notification(prefix);
    private Encipherment encipherment = new Encipherment(prefix);
    private Reports reports = new Reports(prefix);
    private Others others = new Others(prefix);

    public JocPermissionsPredicate() {
    }
    
    public static Predicate<String> createPredicate(String prefix, String str) {
        return s -> (prefix + ":" + str).startsWith(s);
    }

    public Predicate<String> getGetLog() {
        return createPredicate(prefix, "get_log");
    }

    public Administration getAdministration() {
        return administration;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Calendars getCalendars() {
        return calendars;
    }

    public Documentations getDocumentations() {
        return documentations;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public DailyPlan getDailyPlan() {
        return dailyPlan;
    }

    public FileTransfer getFileTransfer() {
        return fileTransfer;
    }

    public Notification getNotification() {
        return notification;
    }

    public Encipherment getEncipherment() {
        return encipherment;
    }

    public Reports getReports() {
        return reports;
    }

    public Others getOthers() {
        return others;
    }

}
