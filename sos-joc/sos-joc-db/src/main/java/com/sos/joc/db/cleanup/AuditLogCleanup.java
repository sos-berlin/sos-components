package com.sos.joc.db.cleanup;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernate.Dbms;
import com.sos.commons.hibernate.SOSHibernateSession;

public class AuditLogCleanup {

    public static void showUsage() {
        System.out.println("Usage: hibernateFile age");

        System.out.println("- Remove entries older as n (14) days:");
        System.out.println("      hibernateFile 14");
        System.out.println("");
        System.out.println("- Remove all entries:");
        System.out.println("      hibernateFile 0");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            AuditLogCleanup.showUsage();
            System.exit(0);
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String param = args[i].trim();
            System.out.println(String.format("  %s) %s", i + 1, param));
        }
        String hibernateFile = args[0];
        String age = args[1];
        System.out.println("");
        if (age.equals("0")) {
            System.out.print("Remove all entries");
        } else {
            System.out.print("Remove entries older as " + age + " days");
        }
        System.out.println("");

        SOSHibernateSession connection = null;
        SOSHibernateFactory factory = null;

        try {
            factory = new SOSHibernateFactory(hibernateFile);
            factory.setAutoCommit(false);
            factory.build();
            connection = factory.openStatelessSession("AuditLogCleanup");
            connection.beginTransaction();

            Dbms dbms = factory.getDbms();
            String stmt = null;
            switch (dbms) {
            case MSSQL:
                stmt = "EXEC JOC_AUDIT_LOG_CLEANUP " + age;
                break;
            case MYSQL:
                stmt = "CALL JOC_AUDIT_LOG_CLEANUP(" + age + ")";
                break;
            case ORACLE:
                stmt = "CALL JOC_AUDIT_LOG_CLEANUP(" + age + ")";
                break;
            case PGSQL:
                stmt = "SELECT JOC_AUDIT_LOG_CLEANUP(" + age + ")";
                break;
            default:
                break;
            }

            System.out.println("Execute " + dbms + ": " + stmt);
            connection.getSQLExecutor().execute(stmt);
            connection.commit();
            System.out.println("Entries removed");
            System.exit(0);

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
            }
            System.out.println("");
            AuditLogCleanup.showUsage();
            System.out.println("");
            System.out.println("Exception: ");
            System.out.println(e.toString());
            System.exit(1);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

}
