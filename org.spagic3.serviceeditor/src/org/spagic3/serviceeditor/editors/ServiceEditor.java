package org.spagic3.serviceeditor.editors;

import org.dom4j.VisitorSupport;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class ServiceEditor extends MultiPageEditorPart implements IResourceChangeListener {

	private XMLEditor xmlEditor;
	private FormEditor formEditor;
	private int xmlEditorPageIndex;
	private int formEditorPageIndex;

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

	protected void createPages() {
		createXMLEditorPage();
		createFormEditorPage();
	}
	
	private void createXMLEditorPage() {
		try {
			xmlEditor = new XMLEditor();
			xmlEditorPageIndex = addPage(xmlEditor, getEditorInput());
			setPageText(xmlEditorPageIndex, xmlEditor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
	}
	
	private void createFormEditorPage() {
		String xmlEditorText =
				xmlEditor.getDocumentProvider()
						.getDocument(xmlEditor.getEditorInput()).get();
		//parse xml and create model
		VisitorSupport modelBuilderVisitor = new VisitorSupport() {
			
		};
		//create form from model
		
		
		
	}
	
	protected void pageChange(int pageIndex) {
		super.pageChange(pageIndex);
		if (pageIndex == xmlEditorPageIndex) {
//			sortWords();
		} else if (pageIndex == formEditorPageIndex) {
			
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
