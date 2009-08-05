package org.spagic3.client.api;

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

public class ClientMessage {

	private String id = null;
	private String body = null;
	private Map<String, String> properties = null;
	private Map<String, DataHandler> attachments = null;
	
	

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
	
	public void setProperty(String key, String value){
		if (properties == null)
			this.properties = new HashMap<String, String>();
		properties.put(key,value);
	}
	
	public void setAttachment(String key, DataHandler dh){
		if (attachments == null)
			this.attachments = new HashMap<String, DataHandler>();
		attachments.put(key,dh);
	}
	
	public Map<String, DataHandler> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, DataHandler> attachments) {
		this.attachments = attachments;
	}
}
