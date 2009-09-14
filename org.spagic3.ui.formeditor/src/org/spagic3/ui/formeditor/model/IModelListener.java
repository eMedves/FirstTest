package org.spagic3.ui.formeditor.model;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IModelListener {
	
	void modelChanged(Object[] objects, ModelChangeType type);

}