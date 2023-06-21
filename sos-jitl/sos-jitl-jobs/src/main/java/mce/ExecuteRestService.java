package mce;

import java.net.SocketException;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;

public class ExecuteRestService {


	public void execute(String[] args) {

		String url = System.getenv("URL");
		SOSRestApiClient sosRestApiClient = new SOSRestApiClient();
		try {
			sosRestApiClient.executeRestServiceCommand("get", url);
		} catch (SocketException | SOSException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ExecuteRestService executeRestService = new ExecuteRestService();
		executeRestService.execute(args);

	}

}
 

 
