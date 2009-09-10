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
	
	private List<IModelListener> modelListeners;
	private List<IModelPart> parts;
	
	public Model() {
		modelListeners = new ArrayList<IModelListener>();
		initialize();
	}
	
	public void addModelListener(IModelListener listener) {
		if (!modelListeners.contains(listener))
			modelListeners.add(listener);
	}
	
	public void removeModelListener(IModelListener listener) {
		modelListeners.remove(listener);
	}
	
	public void fireModelChanged(Object[] objects) {
		for (int i = 0; i < modelListeners.size(); i++) {
			((IModelListener) modelListeners.get(i)).modelChanged(objects);
		}
	}
	
	public IModelPart[] getParts() {
		return parts.toArray(new IModelPart[0]);
	}
	
	private void initialize() {
		parts = new ArrayList<IModelPart>();
		FormDefinition formDefinition = new FormDefinition();
		formDefinition.setModel(this);
		FieldDefinition fieldDefinition = new FieldDefinition();
		fieldDefinition.setId("field1");
		fieldDefinition.setName("Field 1");
		formDefinition.addPart(fieldDefinition);
		addContent(formDefinition);
	}
	
	public boolean addContent(IModelPart e) {
		e.setModel(this);
		e.setParent(null);
		return parts.add(e);
	}
	
	public boolean removeContent(IModelPart o) {
		o.setModel(null);
		o.setParent(null);
		return parts.remove(o);
	}

}