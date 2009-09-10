package org.spagic3.ui.formeditor.model;

public interface IModel {

	IModelPart[] getParts();
	
	void addModelListener(IModelListener listener);
	
	void removeModelListener(IModelListener listener);
	
	void fireModelChanged(Object[] objects);
	
}
