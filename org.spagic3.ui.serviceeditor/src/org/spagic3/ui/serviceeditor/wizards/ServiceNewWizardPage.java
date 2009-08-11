package org.spagic3.ui.serviceeditor.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.spagic3.ui.serviceeditor.model.ServiceModel;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper.CategoryHelper;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper.ServiceHelper;


public class ServiceNewWizardPage extends WizardPage {

	private Text containerText;
	private Text fileText;
	private TreeViewer definitionTree;
	private ISelection selection;
	
	private ServiceModelHelper helper;

	public ServiceNewWizardPage(ISelection selection, ServiceModelHelper helper) {
		super("serviceNewWizardPage");
		setTitle("Service Editor File");
		setDescription("This wizard creates a new service editor file.");
		this.selection = selection;
		this.helper = helper;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		Composite treeComposite = new Composite(container, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		treeComposite.setLayoutData(gd);
		treeComposite.setLayout(new FillLayout());
		
		definitionTree = new TreeViewer(treeComposite, SWT.SINGLE);
		definitionTree.setLabelProvider(new DefinitionLabelProvider());
		definitionTree.setContentProvider(new DefinitionContentProvider());
		definitionTree.setInput(helper.getDefinitionCategories().toArray());
		
		initialize();
		dialogChanged();
		setControl(container);
	}
	
	public class DefinitionLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if (element  instanceof CategoryHelper)
				return ((CategoryHelper) element).getName();
			else if (element  instanceof ServiceHelper)
				return ((ServiceHelper) element).getName();
			else
				return "<empty>";
		}
	}
	
	public class DefinitionContentProvider
			extends ArrayContentProvider
			implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement  instanceof CategoryHelper)
				return ((CategoryHelper) parentElement).getServices().toArray();
			else
				return null;
		}
		
		public Object getParent(Object element) {
			if (element  instanceof ServiceHelper)
				return ((ServiceHelper) element).getCategory();
			else 
				return null;
		}
		
		public boolean hasChildren(Object element) {
			if (element  instanceof CategoryHelper)
				return ((CategoryHelper) element).hasServices();
			else
				return false;
		}
	} 

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText("default");
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
//		int dotLoc = fileName.lastIndexOf('.');
//		if (dotLoc != -1) {
//			String ext = fileName.substring(dotLoc + 1);
//			if (ext.equalsIgnoreCase("service") == false) {
//				updateStatus("File extension must be \"service\"");
//				return;
//			}
//		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}
}