package org.spagic3.service.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;

public class ServiceModelHelper {
	
	
	private static Map<String, String> namespaceMap;
	private static ServiceModelHelper instance;
	
	static {
		namespaceMap = new HashMap<String, String>();
		namespaceMap.put("spagic", "urn:org:spagic3");
	}
	
	private Document scappyDefDocument;
	
	
	public ServiceModelHelper() throws Exception {
		File scrappyDefFile = Activator.getFileFromPlugin("conf/scrappy-def.xml");
		SAXReader xmlReader = new SAXReader();
		scappyDefDocument = xmlReader.read(scrappyDefFile);
		instance = this;
	}
	
	public static ServiceModelHelper getInstance() {
		return instance;
	}

	public Document getScappyDefDocument() {
		return scappyDefDocument;
	}

	public static Map<String, String> getNamespaceMap() {
		return namespaceMap;
	}

	public static void setNamespaceMap(Map<String, String> namespaceMap) {
		ServiceModelHelper.namespaceMap = namespaceMap;
	}

	public String getComponentName(String factoryName) {
		return evalXPathAsString(scappyDefDocument, 
				"(/scrappy/definitions/def[@factory=\"" + 
				factoryName + 
				"\"]/@name)");
	}
	
	public static String evalXPathAsString(String xml, String xpath) {
		try {
	        Document document = DocumentHelper.parseText(xml);
			return evalXPathAsString(document, xpath);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String evalXPathAsString(Document document, String xpath) {
		try {
			org.dom4j.XPath xPath = new DefaultXPath(xpath);
			xPath.setNamespaceURIs(namespaceMap);
			return xPath.valueOf(document);
		} catch (Exception e) {
			return null;
		}
	}

}
