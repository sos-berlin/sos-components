package com.sos.commons.hibernate;

public class SOSHibernateFileProcessorTest {

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            String hibernateConfigFile = "src/test/resources/hibernate.cfg.xml";
            String path = "C:\\Temp";
            String fileSpec = "*.sql";

            args = new String[] { hibernateConfigFile, path, fileSpec };
        }

        SOSHibernateFileProcessor.main(args);

    }
}
