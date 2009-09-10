package org.spagic3.ui.formeditor.model;

public class NamedModelPart implements IModelPart {

	private String id;
	private String name;
	private IModel model;
	private IModelPart parent;

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
