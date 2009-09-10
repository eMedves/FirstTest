package org.spagic3.ui.formeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.spagic3.ui.formeditor.model.IModel;
import org.spagic3.ui.formeditor.model.Model;

public class FormEditorInput implements IEditorInput {

	private IModel model;
	private IFileEditorInput fileEditorInput;
		
	public FormEditorInput(IFileEditorInput fileEditorInput) {
		this.model = new Model();
		this.fileEditorInput = fileEditorInput;
	}

	public IModel getModel() {
		return model;
	}

	public IFileEditorInput getFileEditorInput() {
		return fileEditorInput;
	}

	public String getName() {
		return fileEditorInput.getName();
	}

	public boolean exists() {
		return fileEditorInput.exists();
	}

	public Object getAdapter(Class arg0) {
		return fileEditorInput.getAdapter(arg0);
	}

	public IFile getFile() {
		return fileEditorInput.getFile();
	}

	public ImageDescriptor getImageDescriptor() {
		return fileEditorInput.getImageDescriptor();
	}

	public IPersistableElement getPersistable() {
		return fileEditorInput.getPersistable();
	}

	public IStorage getStorage() throws CoreException {
		return fileEditorInput.getStorage();
	}

	public String getToolTipText() {
		return fileEditorInput.getToolTipText();
	}

}