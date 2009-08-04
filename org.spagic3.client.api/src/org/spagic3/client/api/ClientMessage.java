package org.spagic3.client.api;

import java.util.Map;

public class ClientMessage {

	private String id = null;
	private String body = null;
	private Map<String, String> properties = null;
	
	public ClientMessage(String id, String body, Map<String, String> properties){
		this.body = body;
		this.properties = properties;
	}
	
	public ClientMessage(String id){
		this(id, null, null);
	}
	
	public ClientMessage(String id, String body){
		this(id, body, null);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	
	
	
	public void setBody(String body){
		this.body = body;
	}
	public void setProperties(Map<String, String> properties){
		this.properties = properties;
	}
	
	public String getBody(){
		return this.body;
	}
	
	public Map<String, String> getProperties(){
		return this.properties;
	}
}
