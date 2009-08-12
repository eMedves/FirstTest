package org.spagic3.ui.serviceeditor.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper.PropertyHelper;


public class ServiceNewWizard extends Wizard implements INewWizard {

	private ServiceNewWizardPage page;
	private ISelection selection;
	private ServiceModelHelper helper;

	public ServiceNewWizard() {
		super();
		setNeedsProgressMonitor(true);
		try {
			helper = new ServiceModelHelper();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), 
					"Error", e.getMessage());
		}
	}
	
	public void addPages() {
		page = new ServiceNewWizardPage(selection, helper);
		addPage(page);
	}

	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName() + "." + page.getFileType();
		final String factory = page.getFactory();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, factory, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), 
					"Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	private void doFinish(String containerName, String fileName, String factory, IProgressMonitor monitor)
			throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(fileName, factory);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	private InputStream openContentStream(String fileName, String factory) {
		StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xml.append("<spagic:component\n");
		xml.append("\t\txmlns:spagic=\"urn:org:spagic3\"\n");
		xml.append("\t\txmlns=\"urn:org:spagic3\"\n");
		xml.append("\t\tspagic.id=\"").append("").append("\"\n");
		xml.append("\t\tfactory.name=\"").append(factory).append("\">\n");
		if (fileName.endsWith("connector")) {
			xml.append("\t<property name=\"target\" value=\"\"/>\n");
		}
		for(PropertyHelper propertyHelper : helper.getDefBaseProperties(factory)) {
			xml.append("\t<property name=\"")
					.append(propertyHelper.getName())
					.append("\" value=\"")
					.append(propertyHelper.getDefault())
					.append("\"/>\n");
		}
		xml.append("</spagic:component>");
		return new ByteArrayInputStream(xml.toString().getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.spagic.proof.wizard", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}