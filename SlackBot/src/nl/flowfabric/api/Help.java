package nl.flowfabric.api;

import java.io.IOException;

public class Help {
	
	public static void help(String user, String channel, RtmSession session) throws IOException{
		session.sendMessage(user, channel, "Ik ken de volgende commando's:\\n- Help > Deze help functie\\n- ticket > Haal ticket informatie op\\nType help <commando> voor meer informatie");
	}
}
