package org.spagic3.ui.formeditor.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.spagic3.ui.formeditor.Activator;
import org.spagic3.ui.formeditor.FormEditorInput;
import org.spagic3.ui.formeditor.model.IModel;
import org.spagic3.ui.formeditor.model.IModelListener;
import org.spagic3.ui.formeditor.model.ModelChangeType;
import org.spagic3.ui.formeditor.model.ModelHelper;

public class FormEditor extends org.eclipse.ui.forms.editor.FormEditor implements IResourceChangeListener, IModelListener {

	private XMLEditor xmlEditor;
	private FormPage formEditor;
	private int xmlEditorPageIndex;
	private int formEditorPageIndex;
	
	private ModelHelper modelHelper;
	private IModel model;
	
	public FormEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, new FormEditorInput((IFileEditorInput) editorInput));
		modelHelper = new ModelHelper();
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(Activator.getDefault().getFormColors(display));
	}
	
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	public boolean isDirty() {
		return xmlEditor.isDirty();
	}

	public void doSave(IProgressMonitor monitor) {
		updateXML();
		getEditor(xmlEditorPageIndex).doSave(monitor);
	}

	public void doSaveAs() {
		updateXML();
		IEditorPart editor = getEditor(xmlEditorPageIndex);
		editor.doSaveAs();
		setInput(new FormEditorInput((IFileEditorInput) editor.getEditorInput()));
		updateModel();
		updateTitle();
	}

	public void gotoMarker(IMarker marker) {
		setActivePage(xmlEditorPageIndex);
		IDE.gotoMarker(getEditor(xmlEditorPageIndex), marker);
	}
	
	protected void addPages() {
		createXMLEditorPage();
		createFormEditorPage();
		updateTitle();
	}
	
	private void createXMLEditorPage() {
		try {
			xmlEditor = new XMLEditor();
			xmlEditorPageIndex = addPage(xmlEditor, 
					((FormEditorInput) getEditorInput()).getFileEditorInput());
			setPageText(xmlEditorPageIndex, "XML");
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested xml editor",
				null,
				e.getStatus());
		}
	}
	
	private String getXMLEditorText() {
		return xmlEditor.getDocumentProvider()
				.getDocument(xmlEditor.getEditorInput()).get();
	}

	private void setXMLEditorText(String xmlText) {
		xmlEditor.getDocumentProvider()
				.getDocument(xmlEditor.getEditorInput())
						.set(xmlText);
	}

	private void updateModel() {
		try {
			IModel oldModel = model;
			model = modelHelper.buildFromXML(getXMLEditorText());
			((FormEditorInput) getEditorInput()).setModel(model);
			if (oldModel != null) {
				model.addListeners(oldModel);
			} else {
				model.addModelListener(this);
			}
			model.fireModelChanged(new Object[] {model, oldModel}, ModelChangeType.CHANGE_NEW);
		} catch (Exception e) {
			ErrorDialog.openError(
					getSite().getShell(),
					"Error updating model: maybe not an xml file",
					null,
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR, e.getMessage(), e));
		}
	}

	public void modelChanged(Object[] objects, ModelChangeType type) {
		if (type != ModelChangeType.CHANGE_NEW) {
			makeXMLEditorTextDirty();
		}
	}

	private void makeXMLEditorTextDirty() {
		setXMLEditorText(" - out of date - \n" + getXMLEditorText());
	}
	
	private void updateXML() {
		String xmlFromModel = modelHelper.asXML(model);
		if (xmlFromModel != null && !xmlFromModel.equals(getXMLEditorText())) {
			setXMLEditorText(xmlFromModel);
		}
	}
	
	private void createFormEditorPage() {
		try {
			updateModel();
			formEditor = new MasterDetailsPage(this);
			formEditorPageIndex = addPage(formEditor);
			setPageText(formEditorPageIndex, "Form");
			setActivePage(formEditorPageIndex);
		} catch (PartInitException e) {
			ErrorDialog.openError(
					getSite().getShell(),
					"Error creating nested form editor",
					null,
					e.getStatus());
		} catch (Exception e) {
			ErrorDialog.openError(
					getSite().getShell(),
					"Error creating nested form editor model",
					null,
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR, e.getMessage(), e));
		}
	}
	
	protected void pageChange(int pageIndex) {
		super.pageChange(pageIndex);
		if (pageIndex == xmlEditorPageIndex) {
			updateXML();
		} else if (pageIndex == formEditorPageIndex) {
			if (getXMLEditorText() != null 
					&& !getXMLEditorText().equals(modelHelper.asXML(model))) {
				updateModel();
			}
		}
	}
	
	public void setFocus() {
		int active = getActivePage();
		if (active == xmlEditorPageIndex) {
			xmlEditor.setFocus();
		} else if (active == formEditorPageIndex) {
			formEditor.setFocus();
		}
	}
	
	void updateTitle() {
		IEditorInput input = getEditorInput();
		setPartName(input.getName());
		setTitleToolTip(input.getToolTipText());
	}
	
	public void resourceChanged(final IResourceChangeEvent event) {
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if(((FileEditorInput)xmlEditor.getEditorInput()).getFile().getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(xmlEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}            
			});
		}
	}

}
