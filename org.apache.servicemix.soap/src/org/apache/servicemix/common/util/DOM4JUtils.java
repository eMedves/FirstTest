package org.apache.servicemix.common.util;

import javax.xml.transform.Source;

import org.apache.servicemix.nmr.api.Message;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentSource;

public class DOM4JUtils {
	public static  Source getDOM4JDocumentSource(Message nm) throws Exception {
    	if (nm.getBody() instanceof Source) {
			return (Source)nm.getBody();	
		}
		if (nm.getBody() instanceof String) {
			return getDOM4JSourceFromStringBody((String)nm.getBody());
		}else{
			throw new Exception(" Cannot get Source from nm");
		}
    }
	
	private static Source getDOM4JSourceFromStringBody(String body) throws Exception {
		Document doc = DocumentHelper.parseText(body);
    	DocumentSource ds = new DocumentSource(doc);
    	return ds;
	}
}
