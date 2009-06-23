package org.spagic3.connectors.filesystem.adapters;

import java.io.BufferedWriter;
import java.io.InputStream;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;

public interface IFileAdapter {
	
	public static final String FILE_NAME_PROPERTY = "spagic.filename";
	
	public static final String FILE_PATH_PROPERTY = "spagic.filepath";
	
	public String getOutputFileName(Exchange exchange, Message in);
	
	public void writeMessage(Exchange exchange, Message in, BufferedWriter bw) throws Exception;
	
	public void readFile(Exchange exchange, Message in, InputStream is, String filePath) throws Exception;
	
}
