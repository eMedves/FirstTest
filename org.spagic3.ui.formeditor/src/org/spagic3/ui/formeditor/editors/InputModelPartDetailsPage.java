package org.spagic3.ui.formeditor.editors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.spagic3.ui.formeditor.model.FieldDefinition;
import org.spagic3.ui.formeditor.model.InputModelPart;

/**
 * @author dejan
 *
 */
public class InputModelPartDetailsPage implements IDetailsPage {

	private IManagedForm mform;
	private InputModelPart input;
	
	private Section titleSection;
	private Label nameLabel;
	private Text nameText;
	private Label typeLabel;
	private Combo typeCombo;
	private Label defaultLabel;
	private Text defaultText;
	private Label editableLabel;
	private Button editableButton;
	private Label mandatoryLabel;
	private Button mandatoryButton;
	private Label validatorLabel;
	private Combo validatorCombo;
	private Label lengthLabel;
	private Text lengthText;
	private Label precisionLabel;
	private Text precisionText;
	private Label comboLabel;
	private Button comboButton;
	
	public InputModelPartDetailsPage() {
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
		titleSection.setLayoutData(td);
		Composite client = toolkit.createComposite(titleSection);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 2;
		glayout.numColumns = 2;
		client.setLayout(glayout);
		
		//name label
		nameLabel = toolkit.createLabel(client, "Name");
		GridData gd = new GridData();
		gd.widthHint = 100;
		nameLabel.setLayoutData(gd);
		
		//name text
		nameText = toolkit.createText(client, "", SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 200;
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (input != null) {
					input.setId(nameText.getText().replaceAll("\\s+", "").toLowerCase());
					input.setName(nameText.getText());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});

		//type label
		typeLabel = toolkit.createLabel(client, "Type");
		gd = new GridData();
		gd.widthHint = 100;
		typeLabel.setLayoutData(gd);
		
		//type combo
		typeCombo = new Combo(client, SWT.READ_ONLY); //SWT.DROP_DOWN
		typeCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		typeCombo.add("java.lang.String");
		typeCombo.add("java.lang.Integer");
		typeCombo.add("java.lang.Date");
		gd = new GridData();
		gd.widthHint = 183;
		typeCombo.setLayoutData(gd);
		typeCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null) {
					input.setType(typeCombo.getText());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});
		
		//default label
		defaultLabel = toolkit.createLabel(client, "Default value");
		gd = new GridData();
		gd.widthHint = 100;
		defaultLabel.setLayoutData(gd);
		
		//default text
		defaultText = toolkit.createText(client, "", SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 200;
		defaultText.setLayoutData(gd);
		defaultText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (input != null) {
					input.setDefaultValue(defaultText.getText());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});

		//editable label
		editableLabel = toolkit.createLabel(client, "Editable");
		gd = new GridData();
		gd.widthHint = 100;
		editableLabel.setLayoutData(gd);
		
		//editable button
		editableButton = toolkit.createButton(client, "", SWT.CHECK);
		gd = new GridData();
		editableButton.setLayoutData(gd);
		editableButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null) {
					input.setEditable(editableButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});

		//mandatory label
		mandatoryLabel = toolkit.createLabel(client, "Mandatory");
		gd = new GridData();
		gd.widthHint = 100;
		mandatoryLabel.setLayoutData(gd);
		
		//mandatory button
		mandatoryButton = toolkit.createButton(client, "", SWT.CHECK);
		gd = new GridData();
		mandatoryButton.setLayoutData(gd);
		mandatoryButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null) {
					input.setMandatory(mandatoryButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});
		
		//validator label
		validatorLabel = toolkit.createLabel(client, "Validator");
		gd = new GridData();
		gd.widthHint = 100;
		validatorLabel.setLayoutData(gd);
		
		//validator combo
		validatorCombo = new Combo(client, SWT.READ_ONLY); //SWT.DROP_DOWN
		validatorCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		validatorCombo.add("string");
		validatorCombo.add("number");
		validatorCombo.add("e-mail");
		gd = new GridData();
		gd.widthHint = 183;
		validatorCombo.setLayoutData(gd);
		validatorCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null) {
					input.setValidator(validatorCombo.getText());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});
		
		//length label
		lengthLabel = toolkit.createLabel(client, "Length");
		gd = new GridData();
		gd.widthHint = 100;
		lengthLabel.setLayoutData(gd);
		
		//length text
		lengthText = toolkit.createText(client, "", SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 200;
		lengthText.setLayoutData(gd);
		lengthText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (input != null) {
					int length = 0;
					try {
						Integer.parseInt(lengthText.getText());
					} catch (NumberFormatException nfe) {}
					input.setLength(length);
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});

		//precision label
		precisionLabel = toolkit.createLabel(client, "Precision");
		gd = new GridData();
		gd.widthHint = 100;
		precisionLabel.setLayoutData(gd);
		
		//precision text
		precisionText = toolkit.createText(client, "", SWT.SINGLE);
		gd = new GridData();
		gd.widthHint = 200;
		precisionText.setLayoutData(gd);
		precisionText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (input != null) {
					int precision = 0;
					try {
						Integer.parseInt(precisionText.getText());
					} catch (NumberFormatException nfe) {}
					input.setPrecision(precision);
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});

		//combo label
		comboLabel = toolkit.createLabel(client, "Combo");
		gd = new GridData();
		gd.widthHint = 100;
		comboLabel.setLayoutData(gd);
		
		//combo button
		comboButton = toolkit.createButton(client, "", SWT.CHECK);
		gd = new GridData();
		comboButton.setLayoutData(gd);
		comboButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null) {
					input.setCombo(comboButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input});
				}
			}
		});
		
		toolkit.paintBordersFor(titleSection);
		titleSection.setClient(client);
	}
	
	private void createSpacer(FormToolkit toolkit, Composite parent, int span) {
		Label spacer = toolkit.createLabel(parent, "");
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		spacer.setLayoutData(gd);
	}
	
	private void update() {
		if (input != null) {
			boolean isField = input instanceof FieldDefinition;
			titleSection.setText(isField
					? "Field definition details" : "Column definition details");
			nameText.setText(input.getName() != null ? input.getName() : "");
			typeCombo.setText(input.getType() != null ? input.getType() : "");
			defaultText.setText(input.getDefaultValue() != null ? input.getDefaultValue() : "");
			if (isField) {
				editableLabel.setEnabled(false);
				editableButton.setEnabled(false);
				editableButton.setSelection(false);
			} else {
				editableLabel.setEnabled(true);
				editableButton.setEnabled(true);
				editableButton.setSelection(input.isEditable());
			}
			mandatoryButton.setSelection(input.isMandatory());
			validatorCombo.setText(input.getValidator() != null ? input.getValidator() : "");
			lengthText.setText(String.valueOf(input.getLength()));
			precisionText.setText(String.valueOf(input.getPrecision()));
			comboButton.setSelection(input.isCombo());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#inputChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection)selection;
		if (sel.size() == 1) {
			input = (InputModelPart) sel.getFirstElement();
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
		nameText.setFocus();
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
