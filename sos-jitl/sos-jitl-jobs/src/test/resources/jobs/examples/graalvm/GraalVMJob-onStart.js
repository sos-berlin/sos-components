var factory = new com.sos.commons.hibernate.SOSHibernateFactory(java.nio.file.Paths.get("src/test/resources/hibernate.cfg.xml"));
factory.build();
js7JobEnvironment.setPayload(factory)