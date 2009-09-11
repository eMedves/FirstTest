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
		StringBuffer xml = new StringBuffer();
		appendXML(xml, model, "");
		return xml.toString();
	}
	
	private void appendXML(StringBuffer xml, Object part, String indent) {
		if (part instanceof IModel) {
			final IModel model = (IModel) part;
			for (IModelPart modelPart : model.getParts()) {
				appendXML(xml, modelPart, "");
			}
		} else if (part instanceof FormDefinition) {
			final FormDefinition formDefinition = (FormDefinition) part;
			xml.append(indent).append("<formdefinition")
				.append(" dynamyc=\"").append(formDefinition.isDynamic() ? "true" : "false").append("\"")
				.append(">\n");
			for (IModelPart formParts : formDefinition.getParts()) {
				appendXML(xml, formParts, indent + "\t");
			}
			xml.append(indent).append("</formdefinition>\n");
		} else if (part instanceof FieldDefinition) {
			final FieldDefinition fieldDefinition = (FieldDefinition) part;
			xml.append(indent).append("<field")
				.append(" id=\"").append(fieldDefinition.getId()).append("\"")
				.append(" name=\"").append(fieldDefinition.getName()).append("\"")
				.append(" type=\"").append(fieldDefinition.getType()).append("\"")
				.append(" defaultValue=\"").append(fieldDefinition.getDefaultValue()).append("\"")
				.append(" mandatory=\"").append(fieldDefinition.isMandatory() ? "true" : "false").append("\"")
				.append(" validator=\"").append(fieldDefinition.getValidator()).append("\"")
				.append(" length=\"").append(fieldDefinition.getLength()).append("\"")
				.append(" precision=\"").append(fieldDefinition.getPrecision()).append("\"")
				.append(" combo=\"").append(fieldDefinition.isCombo() ? "true" : "false").append("\"");
			if (fieldDefinition.getItems().isEmpty()) {
				xml.append("/>\n");
			} else {
				xml.append(">\n");
				for (ItemDefinition itemDefinition : fieldDefinition.getItems()) {
					appendXML(xml, itemDefinition, indent + "\t");
				}
				xml.append(indent).append("</field>\n");
			}
		} else if (part instanceof TableDefinition) {
			final TableDefinition tableDefinition = (TableDefinition) part;
			for (ColumnDefinition columnDefinition : tableDefinition.getColumns()) {
				appendXML(xml, columnDefinition, indent);
			}
		} else if (part instanceof ColumnDefinition) {
			final ColumnDefinition columnDefinition = (ColumnDefinition) part;
			xml.append(indent).append("<column")
				.append(" id=\"").append(columnDefinition.getId()).append("\"")
				.append(" name=\"").append(columnDefinition.getName()).append("\"")
				.append(" type=\"").append(columnDefinition.getType()).append("\"")
				.append(" defaultValue=\"").append(columnDefinition.getDefaultValue()).append("\"")
				.append(" editable=\"").append(columnDefinition.isEditable() ? "true" : "false").append("\"")
				.append(" mandatory=\"").append(columnDefinition.isMandatory() ? "true" : "false").append("\"")
				.append(" validator=\"").append(columnDefinition.getValidator()).append("\"")
				.append(" length=\"").append(columnDefinition.getLength()).append("\"")
				.append(" precision=\"").append(columnDefinition.getPrecision()).append("\"")
				.append(" combo=\"").append(columnDefinition.isCombo() ? "true" : "false").append("\"");
			if (columnDefinition.getItems().isEmpty()) {
				xml.append("/>\n");
			} else {
				xml.append(">\n");
				for (ItemDefinition itemDefinition : columnDefinition.getItems()) {
					appendXML(xml, itemDefinition, indent + "\t");
				}
				xml.append(indent).append("</column>\n");
			}
		} else if (part instanceof ItemDefinition) {
			final ItemDefinition itemDefinition = (ItemDefinition) part;
			xml.append(indent).append("<item")
				.append(" name=\"").append(itemDefinition.getName()).append("\"")
				.append(" value=\"").append(itemDefinition.getValue()).append("\"")
				.append("/>\n");
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
