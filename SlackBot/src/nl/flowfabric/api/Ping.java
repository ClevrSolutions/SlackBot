package nl.flowfabric.api;

import java.util.Date;

import com.neovisionaries.ws.client.WebSocket;

public class Ping {
	
	// Ping, we versturten een ping verzoek naar de server. RtmSession weet wel wat ie met een pong moet doen zolang we de gebruiker en het kanaal maar goed zetten.
	public static void ping(String channel, String user, RtmSession session, WebSocket ws) {
		session.pingUser = user;
		session.pingChannel = channel;
		
		Date date = new Date();
		session.pingTime = (int) date.getTime();
		ws.sendText("{\"id\": 2, \"type\": \"ping\", \"time\": " + session.pingTime + "}");
	}

}
