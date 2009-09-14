package org.spagic3.ui.formeditor.model;

public class ItemDefinition implements IModelPart {

	private String name = "";
	private String value = "";
	private IModel model;
	private IModelPart parent;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public IModel getModel() {
		return model;
	}

	public void setModel(IModel model) {
		this.model = model;
	}

	public IModelPart getParent() {
		return parent;
	}

	public void setParent(IModelPart parent) {
		this.parent = parent;
	}
	
}
