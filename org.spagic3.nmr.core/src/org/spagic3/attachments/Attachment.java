package org.spagic3.attachments;
import javax.activation.DataHandler;


public class Attachment {
	private String exId = null;
	
	DataHandler dh = null;

	public String getExId() {
		return exId;
	}

	public void setExId(String exId) {
		this.exId = exId;
	}

	public DataHandler getDh() {
		return dh;
	}

	public void setDh(DataHandler dh) {
		this.dh = dh;
	}

	public Attachment(String exId, DataHandler dh) {
		super();
		this.exId = exId;
		this.dh = dh;
	}
	
}
