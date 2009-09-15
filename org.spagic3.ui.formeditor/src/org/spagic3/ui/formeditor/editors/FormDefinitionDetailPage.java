package org.spagic3.ui.formeditor.editors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.spagic3.ui.formeditor.model.FormDefinition;
import org.spagic3.ui.formeditor.model.IModelPart;
import org.spagic3.ui.formeditor.model.ModelChangeType;

/**
 * @author dejan
 *
 */
public class FormDefinitionDetailPage implements IDetailsPage {
	private IManagedForm mform;
	private FormDefinition input;
	
	private Section titleSection;
	private Label dynamicLabel;
	private Button dynamicButton;

	public FormDefinitionDetailPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 5;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();
		titleSection = toolkit.createSection(parent, Section.DESCRIPTION|Section.TITLE_BAR);
		titleSection.marginWidth = 10;
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		titleSection.setText("Form definition details");
		titleSection.setLayoutData(td);
		Composite client = toolkit.createComposite(titleSection);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		glayout.numColumns = 2;
		client.setLayout(glayout);
				
		//dynamic label
		dynamicLabel = toolkit.createLabel(client, "Dynamic");
		GridData gd = new GridData();
		gd.widthHint = 100;
		dynamicLabel.setLayoutData(gd);
		
		//dynamic button
		dynamicButton = toolkit.createButton(client, "", SWT.CHECK);
		gd = new GridData();
		dynamicButton.setLayoutData(gd);
		dynamicButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null 
						&& dynamicButton.getSelection() != input.isDynamic()) {
					input.setDynamic(dynamicButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input, "dynamic"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		
		toolkit.paintBordersFor(titleSection);
		titleSection.setClient(client);
	}

	private void update() {
		dynamicButton.setSelection(input.isDynamic());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#inputChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection)selection;
		if (sel.size() == 1) {
			input = (FormDefinition) sel.getFirstElement();
		} else {
			input = null;
		}
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#commit()
	 */
	public void commit(boolean onSave) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#setFocus()
	 */
	public void setFocus() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#dispose()
	 */
	public void dispose() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
	
	public boolean isStale() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#refresh()
	 */
	public void refresh() {
		update();
	}
	
	public boolean setFormInput(Object input) {
		return false;
	}
	
}
