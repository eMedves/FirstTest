package org.spagic3.ui.serviceeditor.editors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.spagic3.ui.serviceeditor.model.IServiceModel;
import org.spagic3.ui.serviceeditor.model.MapPropertyModifier;
import org.spagic3.ui.serviceeditor.model.PropertyModifier;

public class FormModelPage extends FormPage {
	
	private ServiceEditor editor;
	private IServiceModel model;
	
	private IManagedForm managedForm;
	
	public FormModelPage(ServiceEditor editor, IServiceModel model) {
		super(editor, "FormServiceEditor", "Form Service Editor");
		this.editor = editor;
		this.model = model;
	}
	
	public void removeFocusListeners() {
		removeFocusListeners(managedForm.getForm().getBody());
	}
	
	private void removeFocusListeners(Control control) {
		for (Listener listener : control.getListeners(SWT.FocusOut)) {
			control.removeListener(SWT.FocusOut, listener);
		}
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				removeFocusListeners(child);
			}
		}
	}
	
	protected void createFormContent(IManagedForm managedForm) {
		this.managedForm = managedForm;
		
		ScrolledForm form = managedForm.getForm();
		form.setText("Service Editor");
		
		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 3;
		layout.minNumColumns = 1;
		form.getBody().setLayout(layout);
		
		createModelForm(managedForm);
	}
	
	private void createModelForm(IManagedForm mform) {
		createPropertySection(mform);
		createMapPropertySections(mform);
	}
	
	private void createPropertySection(IManagedForm mform) {
		Composite client = createSection(mform, "Root Properties", "", 2);
		FormToolkit toolkit = mform.getToolkit();
		for(Object nameObj : model.getProperties().keySet()) {
			final String name = (String) nameObj;
			toolkit.createLabel(client, name);
			Text text = toolkit.createText(client, 
					(String) model.getProperties().get(name), 
					SWT.SINGLE);
			GridData gd = new GridData();
			gd.widthHint = 150;
			text.setLayoutData(gd);
			ListenerHelper listener
				= new ListenerHelper(editor, model, 
						new PropertyModifier(model, name));
//			text.addFocusListener(listener);
			text.addKeyListener(listener);
			//toolkit.paintBordersFor(client);
		}
	}
	
	private void createMapPropertySections(IManagedForm mform) {
		FormToolkit toolkit = mform.getToolkit();
		for (String mapName : model.getMapProperties().keySet()) {
			final Composite client = createSection(mform, mapName, "", 1);
			for(Object keyObj : model.getMapProperties().get(mapName).keySet()) {
				final String key = (String) keyObj;
				final Composite subClient = createSubSection(mform, client, key, "", 2);
				for (Object nameObj : model.getEntryForPropertyMap(mapName, key).keySet()) {
					final String name = (String) nameObj;
					toolkit.createLabel(subClient, name);
					Text text = toolkit.createText(subClient, 
							(String) model.getEntryForPropertyMap(mapName, key).get(name), 
							SWT.SINGLE);
					GridData gd = new GridData();
					gd.widthHint = 150;
					text.setLayoutData(gd);
					ListenerHelper listener
						= new ListenerHelper(editor, model, 
								new MapPropertyModifier(model, mapName, key, name));
//					text.addFocusListener(listener);
					text.addKeyListener(listener);
					//toolkit.paintBordersFor(client);
				}
			}
		}
	}
	
	private Composite createSection(IManagedForm mform, String title,
			String desc, int numColumns) {
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(form.getBody(), 
				Section.TITLE_BAR | Section.TWISTIE | Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		section.setDescription(desc);
		//toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}
	
	private Composite createSubSection(IManagedForm mform, Composite parent, String title,
			String desc, int numColumns) {
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(parent, 
				Section.TWISTIE | Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		section.setDescription(desc);
		//toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}

}