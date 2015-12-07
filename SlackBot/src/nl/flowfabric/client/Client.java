package nl.flowfabric.client;

import java.io.IOException;

import nl.flowfabric.api.RtmSession;

import org.json.JSONException;

import com.neovisionaries.ws.client.WebSocketException;

public class Client {
	private static RtmSession session;
	private static final String TOKEN = "INSERT TOKEN HERE";
	
	/*
	 * Dit is waar het begint, vul hierboven een bot token in en dan gaan we!
	 */
	
	public static void main(String[] args) {
		session = new RtmSession();
		try {
			session.connect(TOKEN);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WebSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
