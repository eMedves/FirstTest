package org.spagic3.ui.formeditor.editors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.spagic3.ui.formeditor.model.FieldDefinition;
import org.spagic3.ui.formeditor.model.IModelPart;
import org.spagic3.ui.formeditor.model.InputModelPart;
import org.spagic3.ui.formeditor.model.ItemDefinition;
import org.spagic3.ui.formeditor.model.ModelChangeType;

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
	
	private Label itemsLabel;
	private Table itemsTable;
	private TableViewer itemsViewer;
	private Button addItemButton;
	private Button removeItemButton;

	
	public InputModelPartDetailsPage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}
	
	
	class DetailLabelProvider extends LabelProvider
	implements ITableLabelProvider {
		public Image getColumnImage(Object element, int index) {
			return null;
		}
		public String getColumnText(Object element, int index) {
			ItemDefinition itemDefinition = (ItemDefinition) element;
			switch (index) {
				case 0 :
					return itemDefinition.getName();
				case 1 :
					return itemDefinition.getValue();
				default :
					return "<unknown(" + index + ")>";
			}
		}	
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
		glayout.numColumns = 3;
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
				if (input != null 
						&& !(nameText.getText() == null ? input.getName() == null : nameText.getText().equals(input.getName()))) {
					input.setId(nameText.getText().replaceAll("\\s+", "").toLowerCase());
					input.setName(nameText.getText());
					input.getModel().fireModelChanged(new Object[]{input, "name"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);

		//type label
		typeLabel = toolkit.createLabel(client, "Type");
		gd = new GridData();
		gd.widthHint = 100;
		typeLabel.setLayoutData(gd);
		
		//type combo
		typeCombo = new Combo(client, SWT.READ_ONLY); //SWT.DROP_DOWN
		typeCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		typeCombo.add("String");
		typeCombo.add("Date");
		typeCombo.add("Long");
		typeCombo.add("Double");
		typeCombo.add("Boolean");
		gd = new GridData();
		gd.widthHint = 183;
		typeCombo.setLayoutData(gd);
		typeCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null 
						&& !(typeCombo.getText() == null ? input.getType() == null : typeCombo.getText().equals(input.getType()))) {
					input.setType(typeCombo.getText());
					input.getModel().fireModelChanged(new Object[]{input, "type"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);
		
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
				if (input != null 
						&& !(defaultText.getText() == null ? input.getDefaultValue() == null : defaultText.getText().equals(input.getDefaultValue()))) {
					input.setDefaultValue(defaultText.getText());
					input.getModel().fireModelChanged(new Object[]{input, "defaultValue"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);

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
				if (input != null 
						&& editableButton.getSelection() != input.isEditable()) {
					input.setEditable(editableButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input, "editable"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);

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
				if (input != null 
						&& mandatoryButton.getSelection() != input.isMandatory()) {
					input.setMandatory(mandatoryButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input, "mandatory"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);
		
		//validator label
		validatorLabel = toolkit.createLabel(client, "Validator");
		gd = new GridData();
		gd.widthHint = 100;
		validatorLabel.setLayoutData(gd);
		
		//validator combo
		validatorCombo = new Combo(client, SWT.READ_ONLY); //SWT.DROP_DOWN
		validatorCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		validatorCombo.add("Number");
		validatorCombo.add("Text");
		validatorCombo.add("Number & Text");
		validatorCombo.add("Telephone Number");
		validatorCombo.add("E-mail");
		gd = new GridData();
		gd.widthHint = 183;
		validatorCombo.setLayoutData(gd);
		validatorCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (input != null 
						&& !(validatorCombo.getText() == null ? input.getValidator() == null : validatorCombo.getText().equals(input.getValidator()))) {
					input.setValidator(validatorCombo.getText());
					input.getModel().fireModelChanged(new Object[]{input, "validator"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);
		
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
				int length = 0;
				try {
					length = Integer.parseInt(lengthText.getText());
				} catch (NumberFormatException nfe) {}
				if (input != null
						&& length != input.getLength()) {
					input.setLength(length);
					input.getModel().fireModelChanged(new Object[]{input, "length"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);

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
				int precision = 0;
				try {
					precision = Integer.parseInt(precisionText.getText());
				} catch (NumberFormatException nfe) {}
				if (input != null
						&& precision != input.getPrecision()) {
					input.setPrecision(precision);
					input.getModel().fireModelChanged(new Object[]{input, "precision"}, ModelChangeType.CHANGE_PROPERTY);
				}
			}
		});
		createSpacer(toolkit, client, 1);

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
				if (input != null 
						&& comboButton.getSelection() != input.isCombo()) {
					input.setCombo(comboButton.getSelection());
					input.getModel().fireModelChanged(new Object[]{input, "combo"}, ModelChangeType.CHANGE_PROPERTY);
					enableComboPart();
				}
			}
		});
		createSpacer(toolkit, client, 1);
		
		//items label
		itemsLabel = toolkit.createLabel(client, "Combo items");
		gd = new GridData();
		gd.widthHint = 100;
		itemsLabel.setLayoutData(gd);

		//items table
		itemsTable = toolkit.createTable(client, SWT.V_SCROLL);
		gd = new GridData();
		gd.heightHint = 80;
		gd.widthHint = 192;
		itemsTable.setLayoutData(gd);
		itemsTable.setHeaderVisible(true);
		itemsTable.setLinesVisible(true);
		TableColumn itemsLabelColumn = new TableColumn(itemsTable, SWT.LEFT);
		itemsLabelColumn.setText("Name");
		itemsLabelColumn.setWidth(96);
		TableColumn itemsValueColumn = new TableColumn(itemsTable, SWT.LEFT);
		itemsValueColumn.setText("Value");
		itemsValueColumn.setWidth(96);

		itemsViewer = new TableViewer(itemsTable);
		itemsViewer.setContentProvider(new ArrayContentProvider());
		itemsViewer.setLabelProvider(new DetailLabelProvider());
		if (input != null) {
			itemsViewer.setInput(input.getItems());
		}
		
		TableViewerColumn itemsLabelViewerColumn = new TableViewerColumn(itemsViewer, itemsLabelColumn);
		itemsLabelViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((ItemDefinition) element).getName();
			}
		});
		itemsLabelViewerColumn.setEditingSupport(new EditingSupport(itemsViewer) {
			TextCellEditor editor = null;
			protected boolean canEdit(Object element) {
				return true;
			}
			protected CellEditor getCellEditor(Object element) {
				if (editor == null) {
					Composite table = (Composite) itemsViewer.getControl();
					editor = new TextCellEditor(table);
				}
				return editor;
			}
			protected Object getValue(Object element) {
				return ((ItemDefinition) element).getName();
			}
			protected void setValue(Object element, Object value) {
				final ItemDefinition itemDefinition = (ItemDefinition) element;
				if (!(itemDefinition.getName() == null ? value == null
						: itemDefinition.getName().equals((String) value))) {
					itemDefinition.setName((String) value);
					itemDefinition.getModel().fireModelChanged(
							new Object[] {itemDefinition, "name"}, 
							ModelChangeType.CHANGE_PROPERTY);
					itemsViewer.refresh(element);
				}
			}
		});

		TableViewerColumn itemsValueViewerColumn = new TableViewerColumn(itemsViewer, itemsValueColumn);
		itemsValueViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((ItemDefinition) element).getValue();
			}
		});
		itemsValueViewerColumn.setEditingSupport(new EditingSupport(itemsViewer) {
			TextCellEditor editor = null;
			protected boolean canEdit(Object element) {
				return true;
			}
			protected CellEditor getCellEditor(Object element) {
				if (editor == null) {
					Composite table = (Composite) itemsViewer.getControl();
					editor = new TextCellEditor(table);
				}
				return editor;
			}
			protected Object getValue(Object element) {
				return ((ItemDefinition) element).getValue();
			}
			protected void setValue(Object element, Object value) {
				final ItemDefinition itemDefinition = (ItemDefinition) element;
				if (!(itemDefinition.getValue() == null ? value == null
						: itemDefinition.getValue().equals((String) value))) {
					itemDefinition.setValue((String) value);
					itemDefinition.getModel().fireModelChanged(
							new Object[] {itemDefinition, "name"}, 
							ModelChangeType.CHANGE_PROPERTY);
					itemsViewer.refresh(element);
				}
			}
		});
		
		//items buttons
		Composite itemsButtons = toolkit.createComposite(client);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		itemsButtons.setLayoutData(gd);
		itemsButtons.setLayout(new FillLayout(SWT.VERTICAL));

		addItemButton = toolkit.createButton(itemsButtons, "Add Item", SWT.PUSH);
		addItemButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (input != null) {
					final ItemDefinition itemDefinition = new ItemDefinition();
					input.addItem(itemDefinition);
					input.getModel().fireModelChanged(new Object[]{itemDefinition, input}, ModelChangeType.CHANGE_ADD);
					itemsViewer.refresh();
					itemsTable.select(itemsTable.getItemCount() - 1);
					itemsTable.showSelection();
				}
			}
		});
		
		removeItemButton = toolkit.createButton(itemsButtons, "Remove", SWT.PUSH);
		removeItemButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) itemsViewer.getSelection();
				if (selection.size() == 1) {
					final IModelPart selected = (IModelPart) selection.getFirstElement();
					if (input != null) {
						final ItemDefinition itemDefinition = (ItemDefinition) selected;
						input.removeItem(itemDefinition);
						input.getModel().fireModelChanged(new Object[]{itemDefinition, input}, ModelChangeType.CHANGE_REMOVE);
						itemsViewer.refresh();
					}
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
	
	private void enableComboPart() {
		if (comboButton.getSelection()) {
			itemsLabel.setEnabled(true);
			itemsTable.setEnabled(true);
			addItemButton.setEnabled(true);
			removeItemButton.setEnabled(true);
		} else {
			itemsLabel.setEnabled(false);
			itemsTable.setEnabled(false);
			addItemButton.setEnabled(false);
			removeItemButton.setEnabled(false);
		}
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
			enableComboPart();
			itemsViewer.setInput(input.getItems());
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
