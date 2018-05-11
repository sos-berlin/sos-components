package com.sos.webservices.deployment.interfaces;

public interface IDeploy {
	
	// for use on the configuration server side
	
	public void connectToSCM();
	
	public void commitConfiguration();
	
	public void tagCommittedConfiguration();
	
	// for use on the JobScheduler side
	
	public void sendCheckoutCommand();
	

}
