package nl.flowfabric.api;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.json.JSONObject;

public class GetTicketInfo {

	/*
	 * Ticketinformatie ophalen:
	 * nog een hoop todo's:
	 * soap doet het nu alleen zonder authenticatie
	 * gericht kunnen zoeken (onderwerp of comments bijv.)
	 */
	
	private static final String url = "http://localhost:8080/ws/GetTicketInfo/";
	private static final String nameSpace = "http://localhost:8080/ws-doc/GetTicketInfo?wsdl";
	private static final String serverURI = "http://localhost:8080/ws/";
	
	public static void getTicketInfo(String user, String channel, String ticketNummer, RtmSession session) throws Exception{
		
		try{
			Integer.parseInt(ticketNummer);
		} catch(NumberFormatException e){
			session.sendMessage(user, channel, ticketNummer + " is geen nummer, probeer het nog een keer...");
		}
		
		JSONObject json = getInfo(ticketNummer);
		session.sendMessage(user, channel, json.getString("customer") + "\\n" + json.getString("onderwerp") + "\\n" + json.getString("expectedbehaviour"));
	}
	
	private static JSONObject getInfo(String ticketNummer) throws Exception{
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        // Send SOAP Message to SOAP Server
        SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(ticketNummer), url);
        
        soapConnection.close();
        String response = printSOAPResponse(soapResponse);
        
        response = response.substring(response.indexOf("CDATA[") + 6, response.indexOf("</Result>") - 3);
        System.out.println(response);
        return new JSONObject(response);
	}
	
	private static SOAPMessage createSOAPRequest(String ticketNummer) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        

        SOAPEnvelope envelope = soapPart.getEnvelope();
        
        envelope.addNamespaceDeclaration("get", nameSpace);
        
        SOAPHeader soapHeader = envelope.getHeader();
        SOAPHeaderElement auth = soapHeader.addHeaderElement(new QName(nameSpace, "get", "authentication"));
        SOAPElement username = auth.addChildElement("username");
        username.addTextNode("wsuser");
        SOAPElement password = auth.addChildElement("password");
        password.addTextNode("1");
        
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem3 = soapBody.addChildElement("ticketinfo");
        SOAPElement soapBodyElem4 = soapBodyElem3.addChildElement("TicketNumber");
        soapBodyElem4.addTextNode(ticketNummer);
        
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "ticketinfo");

        soapMessage.saveChanges();
        return soapMessage;
    }

    private static String printSOAPResponse(SOAPMessage soapResponse) throws Exception {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	soapResponse.writeTo(stream);
    	String message = new String(stream.toByteArray(), "utf-8"); 	
    		
    	return message;
    }
}
