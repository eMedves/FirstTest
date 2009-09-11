package org.spagic3.ui.formeditor.model;

import java.util.ArrayList;
import java.util.List;

public class InputModelPart extends NamedModelPart {

	private String type;
	private String defaultValue;
	private boolean editable; 
	private boolean mandatory;
	private String validator;
	private int length;
	private int precision;
	
	private boolean combo;
	private List<ItemDefinition> items = new ArrayList<ItemDefinition>();

	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
	public String getValidator() {
		return validator;
	}
	
	public void setValidator(String validator) {
		this.validator = validator;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public int getPrecision() {
		return precision;
	}
	
	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public boolean isCombo() {
		return combo;
	}

	public void setCombo(boolean combo) {
		this.combo = combo;
	}

	public List<ItemDefinition> getItems() {
		return items;
	}

	public boolean addItem(ItemDefinition e) {
		e.setModel(getModel());
		e.setParent(this);
		return items.add(e);
	}

	public boolean removeItem(ItemDefinition o) {
		o.setModel(null);
		o.setParent(null);
		return items.remove(o);
	}
	
}
