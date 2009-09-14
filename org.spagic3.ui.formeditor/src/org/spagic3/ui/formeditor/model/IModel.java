package org.spagic3.ui.formeditor.model;

import java.util.List;

public interface IModel {
		
	List<IModelListener> getListeners();

	void addModelListener(IModelListener listener);
	
	void removeModelListener(IModelListener listener);
	
	void fireModelChanged(Object[] objects, ModelChangeType type);
	
	void addListeners(IModel model);
	
	IModelPart[] getParts();
	
	boolean addPart(IModelPart e);	
	
	boolean removePart(IModelPart o);
	
}
