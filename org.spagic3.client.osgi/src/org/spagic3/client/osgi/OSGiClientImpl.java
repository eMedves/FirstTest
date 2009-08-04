package org.spagic3.client.osgi;

import java.util.Map;

import org.spagic3.client.api.Client;
import org.spagic3.client.api.ClientMessage;

public class OSGiClientImpl implements Client {

	@Override
	public ClientMessage invokeService(ClientMessage message,
			Map<String, String> properties) {
		System.out.println(" This is my client osgi side");
		
		System.out.println("Message get Body"+ message.getBody());
		
		return new ClientMessage("response", "<ANDREA>Ah Ah </ANDREA>");
	}

}
