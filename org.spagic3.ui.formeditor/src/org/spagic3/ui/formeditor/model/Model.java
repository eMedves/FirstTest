package org.spagic3.ui.formeditor.model;

import java.util.ArrayList;
import java.util.List;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Model implements IModel {
	
	protected List<IModelListener> modelListeners;
	private List<IModelPart> parts;
	
	public Model() {
		modelListeners = new ArrayList<IModelListener>();
		parts = new ArrayList<IModelPart>();
	}
	
	public List<IModelListener> getListeners() {
		return modelListeners;
	}
	
	public void addModelListener(IModelListener listener) {
		if (!modelListeners.contains(listener))
			modelListeners.add(listener);
	}
	
	public void removeModelListener(IModelListener listener) {
		modelListeners.remove(listener);
	}
	
	public void fireModelChanged(Object[] objects, ModelChangeType type) {
		for (int i = 0; i < modelListeners.size(); i++) {
			((IModelListener) modelListeners.get(i)).modelChanged(objects, type);
		}
	}
	
	public void addListeners(IModel model) {
		this.modelListeners.addAll(model.getListeners());		
	}
	
	public IModelPart[] getParts() {
		return parts.toArray(new IModelPart[0]);
	}
	
	public boolean addPart(IModelPart e) {
		e.setModel(this);
		e.setParent(null);
		return parts.add(e);
	}
	
	public boolean removePart(IModelPart o) {
		o.setModel(null);
		o.setParent(null);
		return parts.remove(o);
	}

}