package org.spagic3.ui.serviceeditor.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;
import org.spagic3.ui.serviceeditor.expression.ScrappyEvaluator;

public class ServiceModelHelper extends org.spagic3.service.model.ServiceModelHelper {
			
	public ServiceModelHelper() throws Exception {
		super();
	}

	private Pattern parameterPattern = Pattern.compile("\\$\\w+");
		
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
			xPath.setNamespaceURIs(getNamespaceMap());
			return (List<Node>) xPath.selectNodes(document);
		} catch (Exception e) {
			return null;
		}
	}

	private void copyProperties(Document document, IServiceModel model) {
		List<Node> propertyList = evalXPathAsNodes(document, "/spagic:component/spagic:property");
		for (Node propertyNode : propertyList) {
			final String propertyXML = propertyNode.asXML();
            final String name = evalXPathAsString(propertyXML, "/spagic:property/@name");
            final String value = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeXml(
            		evalXPathAsString(propertyXML, "/spagic:property/@value")));
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
                    final String value = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeXml(
                    		evalXPathAsString(keyPropertyXML, "/spagic:property/@value")));
                    properties.put(name, value);
            	}
            	model.addEntryToPropertyMap(mapName, key, properties);
            }
		}
	}

	public List<PropertyHelper> getDefBaseProperties(String factory) {
		List<Node> defPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + factory + "\"]/property)");
		List<PropertyHelper> defProperties = new ArrayList<PropertyHelper>();
		for (Node defPropertyNode : defPropertyNodes) {
			defProperties.add(new PropertyHelper(defPropertyNode.asXML()));
		}
		return defProperties;
	}
			
	public List<CategoryHelper> getDefinitionCategories() {
		List<Node> categoryNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/connectors)" +
				" | (/scrappy/services)");
		List<CategoryHelper> categories = new ArrayList<CategoryHelper>();
		for (Node categoryNode : categoryNodes) {
			categories.add(new CategoryHelper(categoryNode.asXML()));
		}
		return categories;
	}
	
	public void applyRules(IServiceModel model) {
		List<Node> condictionsList = evalXPathAsNodes(getScappyDefDocument(), "/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when");
		for (Node condictionNode : condictionsList) {
			final String condictionXML = condictionNode.asXML();
			final String expr = evalXPathAsString(condictionXML, "/when/@expr");
			final String action = evalXPathAsString(condictionXML, "/when/@action");
			ScrappyEvaluator evaluator = new ScrappyEvaluator(expr);
        	boolean condiction = evaluator.eval(model);
        	if ("handleProperty".equals(action)) {
        		List<Node> propertyList = evalXPathAsNodes(condictionXML, "/when/property");
        		for (Node propertyNode : propertyList) {
					final String propertyXML = propertyNode.asXML();
					final String name = evalXPathAsString(propertyXML, "/property/@name");
					if (condiction && !model.getProperties().containsKey(name)) {
                    	final String value = evalXPathAsString(propertyXML, "/property/@default");
	                    model.addProperty(name, value);
                    }
					if (!condiction && model.getProperties().containsKey(name)) {
						model.removeProperty(name);
					}
        		}
        	} else if ("handleKeyMap".equals(action)
        			|| "handleNumberedMap".equals(action)) {
            	final String mapName = evalXPathAsString(condictionXML, "/when/@map");
    			final boolean mapExists = model.getMapProperties().containsKey(mapName);
    			if (!condiction) {
    				if (mapExists) {
    					model.removePropertyMap(mapName);
    				}
    			} else {
	            	final String variableName = evalXPathAsString(condictionXML, "/when/@extractFromProperty");
	        		final String variable = model.getProperties().getProperty(variableName);
	        		Set<String> keys = new LinkedHashSet<String>();
	        		if (variable != null) {
	        			if( "handleKeyMap".equals(action)) {
		        			Matcher matcher = parameterPattern.matcher(variable);
		        			while (matcher.find()) {
		        				keys.add(matcher.group().substring(1));
		        			}
	        			} else {
	        				int number = 0;
	        				try {
	        					number = Integer.valueOf(variable);
	        				} catch (NumberFormatException nfe) {}
	        				for (int i = 1; i <= number; i++) {
	        					keys.add(new Integer(i).toString());
	        				}
	        			}
	        		}
	        		List<Node> propertyList = evalXPathAsNodes(condictionXML, "/when/property");
	        		Map<String, Properties> oldMap = model.getMapProperties().get(mapName);
	        		model.addPropertyMap(mapName);
	        		for (String key : keys) {
	        			final boolean keyExists = mapExists && oldMap.containsKey(key);
						if (keyExists) {
							model.addEntryToPropertyMap(mapName, key, oldMap.get(key));
						} else {
	 						model.addEntryToPropertyMap(mapName, key, new Properties());
	                 		for (Node propertyNode : propertyList) {
	        					final String propertyXML = propertyNode.asXML();
	        					final String name = evalXPathAsString(propertyXML, "/property/@name");
	                        	final String value = evalXPathAsString(propertyXML, "/property/@default");
	    	                    model.getEntryForPropertyMap(mapName, key).put(name, value);
	                		}
	                    }
	        		}
    			}
        	}
        }
	}
	
	public List<PropertyHelper> getDefProperties(IServiceModel model) {
		List<Node> defPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/property)" +
				" | (/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when/property)");
		List<PropertyHelper> defProperties = new ArrayList<PropertyHelper>();
		for (Node defPropertyNode : defPropertyNodes) {
			defProperties.add(new PropertyHelper(defPropertyNode.asXML()));
		}
		return defProperties;
	}

	public List<PropertyHelper> getDefProperties(IServiceModel model, String category) {
		List<Node> defPropertyNodes;
		if (category == null || "".equals(category)) {
			defPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/property[not(@uicategory)])" +
					" | (/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when/property[not(@uicategory)])");
		} else {
			defPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/property[@uicategory=\"" + category + "\"])" +
				" | (/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when/property[@uicategory=\"" + category + "\"])");
		}
		List<PropertyHelper> defProperties = new ArrayList<PropertyHelper>();
		for (Node defPropertyNode : defPropertyNodes) {
			defProperties.add(new PropertyHelper(defPropertyNode.asXML()));
		}
		return defProperties;
	}
	
	public Set<String> getDefUICategories(IServiceModel model) {
		List<Node> defCategoryNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/property/@uicategory)" +
				" | (/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when/property/@uicategory)");
		Set<String> defCategories = new LinkedHashSet<String>();
		for (Node defCategoryNode : defCategoryNodes) {
			defCategories.add(defCategoryNode.getStringValue());
		}
		return defCategories;
	}
	
	public List<String> getComboItems(String comboName) {
		Node comboProviderNode = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/combo-providers/combo-provider[@name=\"" + comboName + "\"])").get(0);
		String config = comboProviderNode.asXML();
		String type = evalXPathAsString(config, "(/combo-provider/@type)");
		IComboProvider comboProvider = new ComboProviderFactory().getComboProvider(type, config);
		if (comboProvider != null) {
			return comboProvider.getComboItems();
		} else {
			return new ArrayList<String>();
		}
	}
	
	public List<MapPropertyHelper> getDefMapProperties(IServiceModel model) {
		List<Node> defMapPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when[(@action=\"handleKeyMap\") or (@action=\"handleNumberedMap\")])");
		List<MapPropertyHelper> defMapProperties = new ArrayList<MapPropertyHelper>();
		for (Node defMapPropertyNode : defMapPropertyNodes) {
			defMapProperties.add(new MapPropertyHelper(defMapPropertyNode.asXML()));
		}
		return defMapProperties;
	}

	public String getComponentName(IServiceModel model) {
		return getComponentName(model.getFactoryName());
	}
	
	public List<String> getEmptyMandatoryProperties(IServiceModel model) {
		List<Node> mandatoryPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/property[@mandatory=\"true\"])" +
				" | (/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when/property[@mandatory=\"true\"])");
		List<String> emptyMandatoryPropertyNames = new ArrayList<String>();
		for (Node mandatoryPropertyNode : mandatoryPropertyNodes) {
			final PropertyHelper propertyHelper = new PropertyHelper(mandatoryPropertyNode.asXML());
			final String name = propertyHelper.getName();
			final String value = model.getProperties().getProperty(name);
			if ("".equals(value)) {
				emptyMandatoryPropertyNames.add(name);
			}
		}
		return emptyMandatoryPropertyNames;
	}

	public List<String> getEmptyMandatoryMapProperties(IServiceModel model) {
		List<Node> defMapPropertyNodes = evalXPathAsNodes(getScappyDefDocument(), "(/scrappy/definitions/def[@factory=\"" + model.getFactoryName() + "\"]/when[(@action=\"handleKeyMap\") or (@action=\"handleNumberedMap\")])");
		List<String> emptyMandatoryMapPropertyNames = new ArrayList<String>();
		for (Node defMapPropertyNode : defMapPropertyNodes) {
			MapPropertyHelper mapPropertyHelper = new MapPropertyHelper(defMapPropertyNode.asXML());
			final String mapName = mapPropertyHelper.getMapName();
			final List<PropertyHelper> propertyHelpers = mapPropertyHelper.getDefProperties();
			for (String key : model.getMapProperties().get(mapName).keySet()) {
				final Properties properties = model.getEntryForPropertyMap(mapName, key);
				for (PropertyHelper propertyHelper : propertyHelpers) {
					if (propertyHelper.isMandatory()) {
						final String name = propertyHelper.getName();
						final String value = properties.getProperty(name);
						if ("".equals(value)) {
							emptyMandatoryMapPropertyNames.add(mapName + ":" + key + ":" + name);
						}
					}
				}
			}
		}
		return emptyMandatoryMapPropertyNames;
	}

	public String asXML(IServiceModel model) {
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<spagic:component\n");
		xml.append("\t\txmlns:spagic=\"urn:org:spagic3\"\n");
		xml.append("\t\txmlns=\"urn:org:spagic3\"\n");
		xml.append("\t\tspagic.id=\"").append(model.getSpagicId()).append("\"\n");
		xml.append("\t\tfactory.name=\"").append(model.getFactoryName()).append("\">\n");
		
		for(Object nameObj : model.getProperties().keySet()) {
			final String name = (String) nameObj;
			xml.append("\t<property name=\"").append(name).append("\" value=\"")
					.append(StringEscapeUtils.escapeXml(StringEscapeUtils.escapeJava(
							(String) model.getProperties().get(name))))
					.append("\"/>\n");
		}
		for (String mapName : model.getMapProperties().keySet()) {
			xml.append("\t<xproperty name=\"").append(mapName).append("\">\n");
			xml.append("\t\t<map>\n");
			for(Object keyObj : model.getMapProperties().get(mapName).keySet()) {
				final String key = (String) keyObj;
				xml.append("\t\t\t<entry>\n");
				xml.append("\t\t\t\t<string>").append(key).append("</string>\n");
				xml.append("\t\t\t\t<properties>\n");
				for (Object nameObj : model.getEntryForPropertyMap(mapName, key).keySet()) {
					final String name = (String) nameObj;
					xml.append("\t\t\t\t\t<property name=\"").append(name).append("\" value=\"")
							.append(StringEscapeUtils.escapeXml(StringEscapeUtils.escapeJava(
									(String) model.getEntryForPropertyMap(mapName, key).get(name))))
							.append("\"/>\n");
				}
				xml.append("\t\t\t\t</properties>\n");
				xml.append("\t\t\t</entry>\n");
			}
			xml.append("\t\t</map>\n");
			xml.append("\t</xproperty>\n");
		}
		xml.append("</spagic:component>");
		return xml.toString();
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
