class JS7Job extends js7.Job {

	processOrder(step) {
		var sql = "select TEXT_VALUE from JOC_VARIABLES where NAME='version'";

		var factory = new com.sos.commons.hibernate.SOSHibernateFactory("src/test/resources/hibernate.cfg.xml");
		var session = null;
		try {
			factory.build();

			session = factory.openStatelessSession();
			var r = session.getSingleValueNativeQuery(sql);

			step.getLogger().info("[" + sql + "]" + r);
		}
		catch (e) {
			throw e;
		}
		finally {
			factory.close(session);
		}

	}
}

