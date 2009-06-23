package org.spagic3.connectors.filesystem.adapters;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class DefaultFileAdapter implements IFileAdapter{
	
	public String getOutputFileName(Exchange exchange, Message in){
		String fileName = null;
		fileName = (String)in.getHeader(IFileAdapter.FILE_NAME_PROPERTY);
		if (fileName == null)
			fileName = (String)exchange.getProperty(IFileAdapter.FILE_NAME_PROPERTY);
		return fileName;
	}

	@Override
	public void writeMessage(Exchange exchange, Message in, BufferedWriter bw) throws Exception  {
		String body = (String)in.getBody();
		PrintWriter pw = new PrintWriter(bw);
		pw.write(body);
		pw.flush();
	}

	public void readFile(Exchange exchange, Message in, InputStream is, String filePath) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(is);
		in.setHeader(IFileAdapter.FILE_PATH_PROPERTY, filePath);
		in.setBody(doc.asXML());
	    exchange.setIn(in);
	}
	
	
}
