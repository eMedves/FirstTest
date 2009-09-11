package org.spagic3.ui.formeditor.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;

public class ModelHelper {
	
	private static Map<String, String> namespaceMap;

	static {
		namespaceMap = new HashMap<String, String>();
//		namespaceMap.put("spagic", "urn:org:spagic3");
	}

	public ModelHelper() {}

	public IModel buildFromXML(String xml) throws DocumentException {
		Document document = DocumentHelper.parseText(xml);
		IModel model = new Model();
		FormDefinition formDefinition = new FormDefinition();
		formDefinition.setDynamic(false);
		formDefinition.setModel(model);
		List<Node> fields = evalXPathAsNodes(document, "/formdefinition/field");
		if (fields != null) {
			for (Node node : fields) {
				final Element field = (Element) node;
				final FieldDefinition fieldDefinition = new FieldDefinition();
				fieldDefinition.setId(field.attributeValue("id"));
				fieldDefinition.setName(field.attributeValue("name"));
				fieldDefinition.setType(field.attributeValue("type"));
				fieldDefinition.setDefaultValue(field.attributeValue("defaultValue"));
//				fieldDefinition.setEditable("true".equals(field.attributeValue("editable")));
				fieldDefinition.setMandatory("true".equals(field.attributeValue("mandatory")));
				fieldDefinition.setValidator(field.attributeValue("validator"));
				fieldDefinition.setCombo("true".equals(field.attributeValue("combo")));
				addListItems(fieldDefinition, node);
				formDefinition.addPart(fieldDefinition);
			}
		}
		List<Node> columns = evalXPathAsNodes(document, "/formdefinition/column");
		if (columns != null && columns.size() > 0) {
			TableDefinition tableDefinition = new TableDefinition();
			formDefinition.addPart(tableDefinition);
			for (Node node : columns) {
				final Element column = (Element) node;
				final ColumnDefinition columnDefinition = new ColumnDefinition();
				columnDefinition.setId(column.attributeValue("id"));
				columnDefinition.setName(column.attributeValue("name"));
				columnDefinition.setType(column.attributeValue("type"));
				columnDefinition.setDefaultValue(column.attributeValue("defaultValue"));
				columnDefinition.setEditable("true".equals((column.attributeValue("editable"))));
				columnDefinition.setMandatory("true".equals(column.attributeValue("mandatory")));
				columnDefinition.setValidator(column.attributeValue("validator"));
				columnDefinition.setCombo("true".equals(column.attributeValue("combo")));
				addListItems(columnDefinition, node);
				tableDefinition.addColumn(columnDefinition);
			}
		}
		model.addPart(formDefinition);
		return model;
	}
	
	private void addListItems(InputModelPart inputPart, Node parentNode) {
		
	}
	
	public String asXML(IModel model) {
		
		
		return null;
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
			org.dom4j.XPath xPath = new DefaultXPath(xpath);
			xPath.setNamespaceURIs(getNamespaceMap());
			return (List<Node>) xPath.selectNodes(document);
		} catch (Exception e) {
			return null;
		}
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
	
	public static Map<String, String> getNamespaceMap() {
		return namespaceMap;
	}

	public static void setNamespaceMap(Map<String, String> namespaceMap) {
		ModelHelper.namespaceMap = namespaceMap;
	}
	
}
