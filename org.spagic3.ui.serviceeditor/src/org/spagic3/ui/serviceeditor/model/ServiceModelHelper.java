package org.spagic3.ui.serviceeditor.model;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.spagic3.ui.serviceeditor.Activator;
import org.spagic3.ui.serviceeditor.expression.ScrappyEvaluator;

public class ServiceModelHelper {
	
	
	private static Map<String, String> namespaceMap;
	static {
		namespaceMap = new HashMap<String, String>();
		namespaceMap.put("spagic", "urn:org:spagic3");
	}
	
	private Document scappyDefDocument;
	
	public ServiceModelHelper() throws Exception {
		File scrappyDefFile = Activator.getFileFromPlugin("conf/scrappy-def.xml");
		SAXReader xmlReader = new SAXReader();
		scappyDefDocument = xmlReader.read(scrappyDefFile);
	}
	
	public IServiceModel createModel(String xml) throws DocumentException {
		Document document = DocumentHelper.parseText(xml);

		ServiceModel model = new ServiceModel();

		model.setSpagicId(evalXPathAsString(document, "/spagic:component/@spagic.id"));
		model.setFactoryName(evalXPathAsString(document, "/spagic:component/@factory.name"));
		
		copyProperties(document, model);
		copyMapProperties(document, model);
		applyRules(model);
		
		return model;
	}
	
	private void copyProperties(Document document, IServiceModel model) {
		List<Node> propertyList = evalXPathAsNodes(document, "/spagic:component/spagic:property");
		for (Node propertyNode : propertyList) {
			final String propertyXML = propertyNode.asXML();
            final String name = evalXPathAsString(propertyXML, "/spagic:property/@name");
            final String value = evalXPathAsString(propertyXML, "/spagic:property/@value");
            model.addProperty(name, value);
		}
	}
	
	private void copyMapProperties(Document document, IServiceModel model) {
		List<Node> propertyMaps = evalXPathAsNodes(document, "/spagic:component/spagic:xproperty");
		for (Node propertyMapNode : propertyMaps) {
			final String propertyMapXML = propertyMapNode.asXML();
            final String mapName = evalXPathAsString(propertyMapXML, "/spagic:xproperty/@name");
            model.addPropertyMap(mapName);
            List<Node> mappedPropertyList = evalXPathAsNodes(propertyMapXML, "/spagic:xproperty/spagic:map/spagic:entry");
            for (Node mappedPropertyNode : mappedPropertyList) {
    			final String mappedPropertyXML = mappedPropertyNode.asXML();
            	final String key = evalXPathAsString(mappedPropertyXML, "/spagic:entry/spagic:string");
            	Properties properties = new Properties();
            	List<Node> keyPropertyList = evalXPathAsNodes(mappedPropertyXML, "/spagic:entry/spagic:properties/spagic:property");
            	for (Node keyPropertyNode : keyPropertyList) {
            		final String keyPropertyXML = keyPropertyNode.asXML();
                    final String name = evalXPathAsString(keyPropertyXML, "/spagic:property/@name");
                    final String value = evalXPathAsString(keyPropertyXML, "/spagic:property/@value");
                    properties.put(name, value);
            	}
            	model.addEntryToPropertyMap(mapName, key, properties);
            }
		}
	}

	public void applyRules(IServiceModel model) {
		List<Node> condictionsList = evalXPathAsNodes(scappyDefDocument, "/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when");
        for (Node condictionNode : condictionsList) {
        	final String condictionXML = condictionNode.asXML();
        	final String expr = evalXPathAsString(condictionXML, "/when/@expr");
        	final String action = evalXPathAsString(condictionXML, "/when/@action");
        	ScrappyEvaluator evaluator = new ScrappyEvaluator(expr);
        	if (evaluator.eval(model)) {
	        	if ("handleProperty".equals(action)) {
	        		List<Node> propertyList = evalXPathAsNodes(condictionXML, "/when/property");
	        		for (Node propertyNode : propertyList) {
	        			final String propertyXML = propertyNode.asXML();
	                    final String name = evalXPathAsString(propertyXML, "/property/@name");
	                    if (model.getProperties().get(name) == null) {
		                    final String value = evalXPathAsString(propertyXML, "/property/@default");
	                    	model.addProperty(name, value);
	                    }
	        		}
	        	} else if ("handleKeyMap".equals(action)) {
	            	final String mapName = evalXPathAsString(condictionXML, "/when/@map");
	            	final String variable = evalXPathAsString(condictionXML, "/when/@extractFromProperty");
	        		
	        	} else if ("handleNumberedMap".equals(action)) {
	            	final String mapName = evalXPathAsString(condictionXML, "/when/@map");
	            	final String variable = evalXPathAsString(condictionXML, "/when/@extractFromProperty");
	        		
	        	}
        	}
        }
	}
	
	public String asXML(IServiceModel model) {
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<spagic:component\n");
		xml.append("\t\txmlns:spagic=\"urn:org:spagic3\"\n");
		xml.append("\t\txmlns=\"urn:org:spagic3\"\n");
		xml.append("\t\tspagic.id=\"" + model.getSpagicId() + "\"\n");
		xml.append("\t\tfactory.name=\"" + model.getFactoryName() + "\">\n");
		
		for(Object nameObj : model.getProperties().keySet()) {
			final String name = (String) nameObj;
			xml.append("\t<property name=\"" + name + "\" value=\"" + (String) model.getProperties().get(name) + "\"/>\n");
		}
		xml.append("</spagic:component>");
		return xml.toString();
	}
	
	private String evalXPathAsString(String xml, String xpath) {
		try {
	        Document document = DocumentHelper.parseText(xml);
			return evalXPathAsString(document, xpath);
		} catch (Exception e) {
			return null;
		}
	}
	
	private String evalXPathAsString(Document document, String xpath) {
		try {
			org.dom4j.XPath xPath = new DefaultXPath(xpath);
			xPath.setNamespaceURIs(namespaceMap);
			return xPath.valueOf(document);
		} catch (Exception e) {
			return null;
		}
	}

	private List<Node> evalXPathAsNodes(String xml, String xpath) {
		try {
	        Document document = DocumentHelper.parseText(xml);
			return evalXPathAsNodes(document, xpath);
		} catch (Exception e) {
			return null;
		}
	}
	
	private List<Node> evalXPathAsNodes(Document document, String xpath) {
		try {
//	        Document document = DocumentHelper.parseText(xml);
			org.dom4j.XPath xPath = new DefaultXPath(xpath);
			xPath.setNamespaceURIs(namespaceMap);
			return (List<Node>) xPath.selectNodes(document);
		} catch (Exception e) {
			return null;
		}
	}

}