package org.spagic3.service.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;

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
	
	public Document getScappyDefDocument() {
		return scappyDefDocument;
	}

	public String getComponentName(String factoryName) {
		return evalXPathAsString(scappyDefDocument, 
				"(/scrappy/definitions/def[@factory=\"" + 
				factoryName + 
				"\"]/@name)");
	}
	
	public List<PropertyHelper> getDefBaseProperties(String factory) {
		List<Node> defPropertyNodes = evalXPathAsNodes(scappyDefDocument, "(/scrappy/definitions/def[@factory=\"" + factory + "\"]/property)");
		List<PropertyHelper> defProperties = new ArrayList<PropertyHelper>();
		for (Node defPropertyNode : defPropertyNodes) {
			defProperties.add(new PropertyHelper(defPropertyNode.asXML()));
		}
		return defProperties;
	}
			
	public List<CategoryHelper> getDefinitionCategories() {
		List<Node> categoryNodes = evalXPathAsNodes(scappyDefDocument, "(/scrappy/connectors)" +
				" | (/scrappy/services)");
		List<CategoryHelper> categories = new ArrayList<CategoryHelper>();
		for (Node categoryNode : categoryNodes) {
			categories.add(new CategoryHelper(categoryNode.asXML()));
		}
		return categories;
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

	public static List<Node> evalXPathAsNodes(String xml, String xpath) {
		try {
	        Document document = DocumentHelper.parseText(xml);
			return evalXPathAsNodes(document, xpath);
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Node> evalXPathAsNodes(Document document, String xpath) {
		try {
//	        Document document = DocumentHelper.parseText(xml);
			org.dom4j.XPath xPath = new DefaultXPath(xpath);
			xPath.setNamespaceURIs(namespaceMap);
			return (List<Node>) xPath.selectNodes(document);
		} catch (Exception e) {
			return null;
		}
	}
	
	public class PropertyHelper {
		
		private Document propertyDoc;
		
		public PropertyHelper(String propertyXML) {
			try {
				propertyDoc = DocumentHelper.parseText(propertyXML);
			} catch (DocumentException de) {}
		}
		
		public String getName() {
			return evalXPathAsString(propertyDoc, "/property/@name");
		}

		public String getLabel() {
			return evalXPathAsString(propertyDoc, "/property/@label");
		}

		public String getUICategory() {
			return evalXPathAsString(propertyDoc, "/property/@uicategory");
		}

		public String getEditor() {
			return evalXPathAsString(propertyDoc, "/property/@editor");
		}

		public String getCombo() {
			return evalXPathAsString(propertyDoc, "/property/@combo");
		}

		public String getDefault() {
			return evalXPathAsString(propertyDoc, "/property/@default");
		}
		
		public String getDroptarget() {
			return evalXPathAsString(propertyDoc, "/property/@droptarget");
		}

		public boolean refreshModel() {
			return "true".equals(evalXPathAsString(propertyDoc, "/property/@refreshModel"));
		}

		public boolean isMandatory() {
			return "true".equals(evalXPathAsString(propertyDoc, "/property/@mandatory"));
		}

	}

	public class MapPropertyHelper {
		
		private Document mapPropertyDoc;
		
		public MapPropertyHelper(String mapPropertyXML) {
			try {
				mapPropertyDoc = DocumentHelper.parseText(mapPropertyXML);
			} catch (DocumentException de) {}
		}
		
		public String getMapName() {
			return evalXPathAsString(mapPropertyDoc, "/when/@map");
		}

		public List<PropertyHelper> getDefProperties() {
			List<Node> defPropertyNodes = evalXPathAsNodes(mapPropertyDoc, "(/when/property)");
			List<PropertyHelper> defProperties = new ArrayList<PropertyHelper>();
			for (Node defPropertyNode : defPropertyNodes) {
				defProperties.add(new PropertyHelper(defPropertyNode.asXML()));
			}
			return defProperties;
		}

	}
	
	public class CategoryHelper {
		
		private Document categoryDoc;
		private List<ServiceHelper> services = new ArrayList<ServiceHelper>();
		
		public CategoryHelper(String categoryXML) {
			try {
				categoryDoc = DocumentHelper.parseText(categoryXML);
			} catch (DocumentException de) {}

			List<Node> serviceNodes = evalXPathAsNodes(categoryDoc, "(/connectors/connector) | (/services/service)");
			for (Node serviceNode : serviceNodes) {
				services.add(new ServiceHelper(this, serviceNode.asXML()));
			}
		}
		
		public String getName() {
			return categoryDoc.getRootElement().getName();
		}
		
		public boolean hasServices() {
			return !services.isEmpty();
		}
		
		public List<ServiceHelper> getServices() {
			return services;
		}
	}

	public class ServiceHelper {
		
		private CategoryHelper category;
		private Document serviceDoc;
		
		public ServiceHelper(CategoryHelper category, String serviceXML) {
			this.category = category;
			try {
				serviceDoc = DocumentHelper.parseText(serviceXML);
			} catch (DocumentException de) {}
		}
		
		public String getType() {
			return serviceDoc.getRootElement().getName();
		}

		public String getLabel() {
			return evalXPathAsString(serviceDoc, "/" + getType() + "/@label");
		}

		public String getName() {
			return evalXPathAsString(serviceDoc, "/" + getType() + "/@name");
		}

		public String getFactory() {
			return evalXPathAsString(serviceDoc, "/" + getType() + "/@factory");
		}

		public CategoryHelper getCategory() {
			return category;
		}
	}

}
