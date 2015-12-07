package nl.flowfabric.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

import nl.flowfabric.client.Client;

import org.json.JSONException;
import org.json.JSONObject;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

public class RtmSession {
	
	/*
	 * Declareer alle dingen die we nodig hebben
	 */
	private String token = null;
	private static WebSocket ws;
	protected String pingUser = "";
	protected String pingChannel = "";
	int pingTime = 0;
	
	/*
	 * Hier controleren we alle berichten, alleen berichten die aan @testbob zijn gericht gaan we ook echt wat mee doen (U0FTWHERH is zijn userID)
	 */
	private void checkMessage(JSONObject json) throws Exception{
		// Is het een bericht?
		if (json.getString("type").equals("message")) {
			// Is het aan mij gericht?
			if (json.getString("text").contains("<@U0FTWHERH>")){
				String text = json.getString("text");
				String commandList;
				if (text.indexOf(":") > 0){
					commandList = text.substring(text.indexOf(":") + 2);
				} else {
					commandList = text.substring(text.indexOf(">") + 2);
				}
				String reply = "";
				String command = commandList.split(" ")[0];
				String user = json.getString("user");
				String channel = json.getString("channel");
				
				/*
				 * The Magic!
				 * Hier kijken we naar het eerste woord dat verstuurd wordt, hiermee bepalen we welke actie we gaan uitvoeren
				 * switch op een string waarde, thanks Java 7
				 */
				switch (command) {
					case "ping" : // Iemand wilt weten hoe snel we zijn, let's show them!
						Ping.ping(channel, user, this, ws);
						break;
					case "ticket" : // Iemand wil ticket informatie ophalen, let's show them!
						GetTicketInfo.getTicketInfo(user, channel, commandList.split(" ")[1], this);
						break;
					case "help":
						Help.help(user, channel, this);
						break;
					default:	// Whoops, geen bekend command
						//reply = "Hmm.. dat commando ken ik nog niet, probeer help voor een overzicht van alle commando's";
						//sendMessage(user, channel, reply);
						Help.help(user, channel, this);
						break;
				}
			}
		}
		// Is het een ping response?
		else if (json.getString("type").equals("pong")) {
			Date date = new Date();
			int time = (int) date.getTime() - pingTime;
			ws.sendText("{\"id\": 1, \"type\": \"message\", \"channel\": \"" + pingChannel + "\", \"text\": \"<@" + pingUser + ">: De vertraging is " + time + "ms\"}");
		}
	}
	
	// Lekker makkelijk, geef een kanaal, gebruiker en bericht mee en ik verstuur het.
	public void sendMessage(String user, String channel, String message){
		ws.sendText("{\"id\": 1, \"type\": \"message\", \"channel\": \"" + channel + "\", \"text\": \"<@" + user + ">: " + message + "\"}");
	}
	
	public static void sendRAWMessage(String message){
		ws.sendText(message);
	}
	
	/*
	 * Om te beginnen moeten we een verbinding maken, door rtm.start aan te roepen krijgen we een wss url terug om mee te verbinden.
	 * Letop! We hebben maar 30 seconden om de verbinding op te zetten, snel werken dus!
	 */
	public void connect(String token) throws UnknownHostException, IOException, InterruptedException, JSONException, WebSocketException{

		this.token = token;
		// URL met de bot token
		String url = "https://slack.com/api/rtm.start?token="+ token;
		
		// Verbinding maken
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		// Response uitlezen, hierin zit de nieuwe URL verstopt!
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer sb = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
		}
		
		// Altijd de verbindingen sluiten, else you're gonna have a bad time!
		in.close();
	    con.disconnect();
	    
	    // Gelukkig is het een json object, maakt het zoeken naar de url wel zo makkelijk:
	    JSONObject json = new JSONObject(sb.toString());
	    String rtmURL = json.getString("url");
	    
	    // Tijd om de RealTimeMessaging connectie te openen
	    ws = new WebSocketFactory().createSocket(rtmURL);
        
	    // Voeg een listener toe zodat we mee kunnen lezen
	    ws.addListener(new WebSocketAdapter() {
	        @Override
	        public void onTextMessage(WebSocket websocket, String response) throws Exception {
	        	System.out.println(response);
	            checkMessage(new JSONObject(response));
	            
	        }
	    });
        
	    // Niet vergeten te verbinden, anders is al het werk voor niks geweest...
	    ws.connect();
	}
}
