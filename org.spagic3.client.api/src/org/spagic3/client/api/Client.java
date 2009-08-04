package org.spagic3.client.api;

import java.util.Map;

public interface Client {

	public ClientMessage invokeService(ClientMessage message, Map<String, String> properties);
	
}
