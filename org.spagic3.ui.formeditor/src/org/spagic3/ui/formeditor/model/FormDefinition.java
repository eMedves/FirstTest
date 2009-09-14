package org.spagic3.ui.formeditor.model;

import java.util.ArrayList;
import java.util.List;

public class FormDefinition implements IModelPart {

	private boolean dynamic = false;
	private List<IModelPart> parts = new ArrayList<IModelPart>();
	private IModel model;
	private IModelPart parent;

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public boolean addPart(IModelPart e) {
		e.setModel(model);
		e.setParent(this);
		return parts.add(e);
	}

	public boolean removePart(IModelPart o) {
		o.setModel(null);
		o.setParent(null);
		return parts.remove(o);
	}

	public IModelPart[] getParts() {
		return parts.toArray(new IModelPart[0]);
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
