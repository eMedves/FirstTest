package org.spagic3.ui.formeditor.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.spagic3.ui.formeditor.Activator;
import org.spagic3.ui.formeditor.FormEditorInput;
import org.spagic3.ui.formeditor.model.ColumnDefinition;
import org.spagic3.ui.formeditor.model.FieldDefinition;
import org.spagic3.ui.formeditor.model.FormDefinition;
import org.spagic3.ui.formeditor.model.IModelListener;
import org.spagic3.ui.formeditor.model.IModelPart;
import org.spagic3.ui.formeditor.model.ItemDefinition;
import org.spagic3.ui.formeditor.model.ModelChangeType;
import org.spagic3.ui.formeditor.model.NamedModelPart;
import org.spagic3.ui.formeditor.model.TableDefinition;
/**
 *
 */
public class ScrolledPropertiesBlock extends MasterDetailsBlock implements IModelListener {
	
	private FormPage page;
	private TreeViewer viewer;
	
	private Button addFieldButton;
	private Button addTableButton;
	private Button addColumnButton;
	private Button removeButton;

	public ScrolledPropertiesBlock(FormPage page) {
		this.page = page;
	}
	
	/**
	 * @param id
	 * @param title
	 */
	class MasterContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof FormDefinition) {
				final FormDefinition formDefinition = (FormDefinition) parentElement;
				return formDefinition.getParts();
			} else if (parentElement instanceof TableDefinition) {
				final TableDefinition tableDefinition = (TableDefinition) parentElement;
				return tableDefinition.getColumns();
			}
			return null;
		}

		public Object getParent(Object element) {
			if (element instanceof FormDefinition) {
				return null;
			} else {
				final IModelPart part = (IModelPart) element;
				return part.getParent();
			}
		}

		public boolean hasChildren(Object element) {
			if (element instanceof FormDefinition) {
				return ((FormDefinition) element).getParts().length > 0;
			} else if (element instanceof TableDefinition) {
				return ((TableDefinition) element).getColumns().length > 0;
			} else {
				return false;
			}
		}
		
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof FormEditorInput) {
				//TODO check this way
//				FormEditorInput input = (FormEditorInput) page
//						.getEditor().getEditorInput();
				FormEditorInput input = (FormEditorInput) inputElement;
				return input.getModel().getParts();
			}
			return new Object[0];
		}
		
		public void dispose() {
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}
	
	class MasterLabelProvider extends LabelProvider {
		
		public Image getImage(Object element) {
			if (element instanceof FormDefinition) {
				return Activator.getDefault().getImage(Activator.IMG_FORM);
			} else if (element instanceof FieldDefinition) {
				return Activator.getDefault().getImage(Activator.IMG_FIELD);
			} else if (element instanceof TableDefinition) {
				return Activator.getDefault().getImage(Activator.IMG_TABLE);
			} else if (element instanceof ColumnDefinition) {
				return Activator.getDefault().getImage(Activator.IMG_COLUMN);
			} else {
				return null;
			}
		}
		
		public String getText(Object element) {
			if (element instanceof NamedModelPart && !(element instanceof TableDefinition)) {
				final NamedModelPart part = (NamedModelPart) element;
				return part.getName();
			}
			return "<anonymous>";
		}
		
	}
	
	protected void createMasterPart(final IManagedForm managedForm,
			Composite parent) {
		//final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION|Section.TITLE_BAR);
		section.setText("Form definition parts");
		section.marginWidth = 10;
		section.marginHeight = 5;

		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);
		
		Tree tree = toolkit.createTree(client, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		tree.setLayoutData(gd);
		toolkit.paintBordersFor(client);
		
		Composite buttons = toolkit.createComposite(client);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		buttons.setLayoutData(gd);
		buttons.setLayout(new FillLayout(SWT.VERTICAL));

		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		addFieldButton = toolkit.createButton(buttons, "Add Field", SWT.PUSH);
		addFieldButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.size() == 1) {
					final IModelPart selected = (IModelPart) selection.getFirstElement();
					if (selected instanceof FormDefinition) {
						final FormDefinition formDefinition = (FormDefinition) selected;
						final FieldDefinition fieldDefinition = new FieldDefinition();
						formDefinition.addPart(fieldDefinition);
						formDefinition.getModel().fireModelChanged(new Object[]{fieldDefinition, formDefinition}, ModelChangeType.CHANGE_ADD);
						viewer.setExpandedState(formDefinition, true);
					}
				}
			}
		});
		
		addTableButton = toolkit.createButton(buttons, "Add Table", SWT.PUSH);
		addTableButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.size() == 1) {
					final IModelPart selected = (IModelPart) selection.getFirstElement();
					if (selected instanceof FormDefinition) {
						final FormDefinition formDefinition = (FormDefinition) selected;
						final TableDefinition tableDefinition = new TableDefinition();
						formDefinition.addPart(tableDefinition);
						formDefinition.getModel().fireModelChanged(new Object[]{tableDefinition, formDefinition}, ModelChangeType.CHANGE_ADD);
						addTableButton.setEnabled(
								shouldEnableAddTableButton(formDefinition));
					}
				}
			}
		});

		addColumnButton = toolkit.createButton(buttons, "Add Column", SWT.PUSH);
		addColumnButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.size() == 1) {
					final IModelPart selected = (IModelPart) selection.getFirstElement();
					if (selected instanceof TableDefinition) {
						final TableDefinition tableDefinition = (TableDefinition) selected;
						final ColumnDefinition columnDefinition = new ColumnDefinition();
						tableDefinition.addColumn(columnDefinition);
						tableDefinition.getModel().fireModelChanged(new Object[]{columnDefinition, tableDefinition}, ModelChangeType.CHANGE_ADD);
						viewer.setExpandedState(tableDefinition, true);
					}
				}
			}
		});

		removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.size() == 1) {
					final IModelPart selected = (IModelPart) selection.getFirstElement();
					if (selected instanceof FormDefinition) {
						//do not remove forms
					} else if (selected instanceof FieldDefinition) {
						final FormDefinition parent = (FormDefinition) selected.getParent();
						parent.removePart(selected);
						parent.getModel().fireModelChanged(new Object[]{selected, parent}, ModelChangeType.CHANGE_REMOVE);
					} else if (selected instanceof TableDefinition) {
						final FormDefinition parent = (FormDefinition) selected.getParent();
						parent.removePart(selected);
						parent.getModel().fireModelChanged(new Object[]{selected, parent}, ModelChangeType.CHANGE_REMOVE);
						addTableButton.setEnabled(
								shouldEnableAddTableButton(parent));
					} else if (selected instanceof ColumnDefinition) {
						final TableDefinition parent = (TableDefinition) selected.getParent();
						parent.removeColumn((ColumnDefinition) selected);
						parent.getModel().fireModelChanged(new Object[]{selected, parent}, ModelChangeType.CHANGE_REMOVE);
					}
				}
			}
		});
		
		viewer = new TreeViewer(tree);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					final IModelPart selected = (IModelPart) selection.getFirstElement();
					if (selected instanceof FormDefinition) {
						addFieldButton.setEnabled(true);
						addTableButton.setEnabled(
								shouldEnableAddTableButton((FormDefinition) selected));
						addColumnButton.setEnabled(false);
						removeButton.setEnabled(false);
					} else if (selected instanceof FieldDefinition) {
						addFieldButton.setEnabled(false);
						addTableButton.setEnabled(false);
						addColumnButton.setEnabled(false);
						removeButton.setEnabled(true);
					} else if (selected instanceof TableDefinition) {
						addFieldButton.setEnabled(false);
						addTableButton.setEnabled(false);
						addColumnButton.setEnabled(true);
						removeButton.setEnabled(true);
					} else if (selected instanceof ColumnDefinition) {
						addFieldButton.setEnabled(false);
						addTableButton.setEnabled(false);
						addColumnButton.setEnabled(false);
						removeButton.setEnabled(true);
					}
				} else {
					addFieldButton.setEnabled(false);
					addTableButton.setEnabled(false);
					addColumnButton.setEnabled(false);
					removeButton.setEnabled(false);
				}
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});
		viewer.setContentProvider(new MasterContentProvider());
		viewer.setLabelProvider(new MasterLabelProvider());
		viewer.setInput(page.getEditor().getEditorInput());
		
		((FormEditorInput) page.getEditor().getEditorInput())
				.getModel().addModelListener(this);
	}
	
	private boolean shouldEnableAddTableButton(FormDefinition formDefinition) {
		boolean containsATable = false;
		for (IModelPart formPart : formDefinition.getParts()) {
			if (formPart instanceof TableDefinition) {
				containsATable = true;
				break;
			}
		}
		return !containsATable;
	}
	
	public void modelChanged(Object[] objects, ModelChangeType type) {
		if ((type != ModelChangeType.CHANGE_PROPERTY 
						&& !(objects[0] instanceof ItemDefinition))
				|| (type == ModelChangeType.CHANGE_PROPERTY 
						&& "name".equals(objects[1]))) {
			viewer.refresh();
		}
	}
	
	protected void createToolBarActions(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		haction.setChecked(true);
		haction.setToolTipText("flip horizontal");
		haction.setImageDescriptor(Activator.getDefault()
				.getImageDescriptor(Activator.IMG_HORIZONTAL));
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);
		vaction.setToolTipText("flip vertical");
		vaction.setImageDescriptor(Activator.getDefault()
				.getImageDescriptor(Activator.IMG_VERTICAL));
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
	}
	
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.registerPage(FormDefinition.class, new FormDefinitionDetailPage());
		detailsPart.registerPage(FieldDefinition.class, new InputModelPartDetailsPage());
		detailsPart.registerPage(TableDefinition.class, new NoPropertiesDetailsPage());
		detailsPart.registerPage(ColumnDefinition.class, new InputModelPartDetailsPage());
	}
	
}