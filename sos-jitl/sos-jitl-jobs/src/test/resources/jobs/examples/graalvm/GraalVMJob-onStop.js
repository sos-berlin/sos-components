if (js7JobEnvironment.getPayload() != null && js7JobEnvironment.getPayload() instanceof com.sos.commons.hibernate.SOSHibernateFactory) {
	var f = js7JobEnvironment.getPayload();
	f.close();
}