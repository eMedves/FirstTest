package org.spagic3.ui.formeditor.model;

public interface IModel {

	void addModelListener(IModelListener listener);
	
	void removeModelListener(IModelListener listener);
	
	void fireModelChanged(Object[] objects);
	
	IModelPart[] getParts();
	
	public boolean addPart(IModelPart e);	
	
	public boolean removePart(IModelPart o);
	
}
