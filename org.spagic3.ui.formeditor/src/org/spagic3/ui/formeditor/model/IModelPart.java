package org.spagic3.ui.formeditor.model;

public interface IModelPart {

	IModel getModel();
	void setModel(IModel model);
	
	IModelPart getParent();
	void setParent(IModelPart part);
	
}
