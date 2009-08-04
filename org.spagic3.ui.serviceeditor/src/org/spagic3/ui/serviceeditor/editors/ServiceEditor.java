package org.spagic3.ui.serviceeditor.editors;

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
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.spagic3.ui.serviceeditor.Activator;
import org.spagic3.ui.serviceeditor.model.IServiceModel;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper;

public class ServiceEditor extends FormEditor implements IResourceChangeListener {

	private XMLEditor xmlEditor;
//	private FormEditor formEditor;
	private int xmlEditorPageIndex;
	private int formPageIndex;
	
	private ServiceModelHelper helper;
	private IServiceModel model;

	public ServiceEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(Activator.getDefault().getFormColors(
				display));
	}
	
	public boolean isSaveAsAllowed() {
		return true;
	}

	public void doSave(IProgressMonitor monitor) {
		getEditor(xmlEditorPageIndex).doSave(monitor);
	}

	public void doSaveAs() {
		IEditorPart editor = getEditor(xmlEditorPageIndex);
		editor.doSaveAs();
		setPageText(xmlEditorPageIndex, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	public void gotoMarker(IMarker marker) {
		setActivePage(xmlEditorPageIndex);
		IDE.gotoMarker(getEditor(xmlEditorPageIndex), marker);
	}

	protected void addPages() {
		createXMLEditorPage();
		createFormEditorPage();
	}
	
	private void createXMLEditorPage() {
		try {
			xmlEditor = new XMLEditor();
			xmlEditorPageIndex = addPage(xmlEditor, getEditorInput());
			setPageText(xmlEditorPageIndex, "XML");
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested xml editor",
				null,
				e.getStatus());
		}
	}
	
	void refreshFromModel() {
		helper.applyRules(model);
		xmlEditor.getDocumentProvider()
			.getDocument(xmlEditor.getEditorInput()).set(helper.asXML(model));
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				removePage(formPageIndex);
				createFormEditorPage();
				setActivePage(formPageIndex);
			}
		});

	}
	
	private void createFormEditorPage() {
		try {
			String xmlEditorText =
				xmlEditor.getDocumentProvider()
						.getDocument(xmlEditor.getEditorInput()).get();

			//parse xml and create model
			helper = new ServiceModelHelper();
			model = helper.createModel(xmlEditorText);
			
			//create form from model
			formPageIndex = addPage(new FormModelPage(this, model));
			setPageText(formPageIndex, "Form");
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
//			xmlEditor.getDocumentProvider()
//				.getDocument(xmlEditor.getEditorInput()).set(helper.asXML(model));
		} else if (pageIndex == formPageIndex) {
			
		}
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
