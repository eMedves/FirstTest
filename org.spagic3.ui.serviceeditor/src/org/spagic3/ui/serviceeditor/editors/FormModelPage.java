package org.spagic3.ui.serviceeditor.editors;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.spagic3.ui.serviceeditor.model.IPropertyModifier;
import org.spagic3.ui.serviceeditor.model.IServiceModel;
import org.spagic3.ui.serviceeditor.model.MapPropertyModifier;
import org.spagic3.ui.serviceeditor.model.PropertyModifier;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper.MapPropertyHelper;
import org.spagic3.ui.serviceeditor.model.ServiceModelHelper.PropertyHelper;

public class FormModelPage extends FormPage {
	
	private static String MODIFIER_DATA_REFERENCE = "MODIFIER_DATA_REFERENCE";
	
	private ServiceEditor editor;
	private ServiceModelHelper helper;
	private IServiceModel model;
	
	private IManagedForm managedForm;
	
	public FormModelPage(ServiceEditor editor, ServiceModelHelper helper, IServiceModel model) {
		super(editor, "FormServiceEditor", "Form Service Editor");
		this.editor = editor;
		this.helper = helper;
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

	private void updateModel() {
		updateModel(managedForm.getForm().getBody());
		editor.refreshModel();
	}
	
	private void updateModel(Control control) {
		IPropertyModifier modifier = (IPropertyModifier) control.getData("MODIFIER_DATA_REFERENCE");
		if (modifier != null) {
			if (control instanceof Text) {
				Text text = (Text) control;
				modifier.setValue(text.getText());
			} else if (control instanceof Combo) {
				Combo combo = (Combo)control;
				modifier.setValue(combo.getText());
			} else if (control instanceof StyledText) {
				StyledText textarea = (StyledText) control;
				modifier.setValue(textarea.getText().replaceAll("\\s+", " ").trim());
			}
		}
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				updateModel(child);
			}
		}
	}

	protected void createFormContent(IManagedForm managedForm) {
		this.managedForm = managedForm;
		
		ScrolledForm form = managedForm.getForm();
		form.setText(helper.getComponentName(model) + " : " + model.getSpagicId());
		
		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 2;
		layout.minNumColumns = 1;
		form.getBody().setLayout(layout);
		
		createModelForm(managedForm);
	}
	
	private void createModelForm(IManagedForm mform) {
		FormToolkit toolkit = mform.getToolkit();
		
		Composite client = toolkit.createComposite(mform.getForm().getBody());
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 4;
		client.setLayout(layout);
		
		//ID
		toolkit.createLabel(client, "ID");
		Text text = toolkit.createText(client, 
				model.getSpagicId(), 
				SWT.SINGLE);
		GridData gd = new GridData();
		gd.widthHint = 200;
		text.setLayoutData(gd);
		IPropertyModifier modifier = new IPropertyModifier() {
				@Override
				public void setValue(String value) {
					model.setSpagicId(value);
				}
				@Override
				public String getValue() {
					return model.getSpagicId();
				}
			};
		text.setData(MODIFIER_DATA_REFERENCE, modifier);
		ListenerHelper listener	= new ListenerHelper(editor, model, modifier, true);
		text.addKeyListener(listener);


		//Apply
		Composite composite = toolkit.createComposite(client);
		gd = new GridData();
		gd.widthHint = 30;
		gd.heightHint = 0;
		if (model.getProperties().containsKey("target")) {
			gd.verticalSpan = 2;
		}
		composite.setLayoutData(gd);
		Button button = toolkit.createButton(client, "Apply", SWT.NULL);
		gd = new GridData();
		gd.widthHint = 60;
		gd.heightHint = 30;
		if (model.getProperties().containsKey("target")) {
			gd.verticalSpan = 2;
		}
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				updateModel();
			}
		});

		//target
		if (model.getProperties().containsKey("target")) {
			toolkit.createLabel(client, "target");
			text = toolkit.createText(client, 
					(String) model.getProperties().get("target"), 
					SWT.SINGLE);
			gd = new GridData();
			gd.widthHint = 200;
			text.setLayoutData(gd);
			modifier = new PropertyModifier(model, "target");
			text.setData(MODIFIER_DATA_REFERENCE, modifier);
			listener = new ListenerHelper(editor, model, modifier, false);
			text.addKeyListener(listener);
		}
		
		createPropertySection(mform);
		createMapPropertySections(mform);
	}
	
	private void createPropertySection(IManagedForm mform) {
		Composite client = createSection(mform, "Root Properties", "", 2);
		FormToolkit toolkit = mform.getToolkit();
		Set<String> uiCategories = new LinkedHashSet<String>();
		uiCategories.add("");
		uiCategories.addAll(helper.getDefUICategories(model));
		for (String uiCategory : uiCategories) {
			List<PropertyHelper> defProperties = helper.getDefProperties(model, uiCategory);
			if (!"".equals(uiCategory) && !defProperties.isEmpty()) {
				client = createSection(mform, uiCategory, "", 2);
			}
			for (PropertyHelper propertyHelper : defProperties) {
				final String name = propertyHelper.getName();
				final String label = propertyHelper.getLabel();
				if (model.getProperties().containsKey(name)) {
					toolkit.createLabel(client, (label == null || "".equals(label)) ? name : label);
					
//					if ("date".equals(propertyHelper.getEditor())) {
//						DateTime date = new DateTime(client, SWT.BORDER);
//						date.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
////						date.setText((String) model.getProperties().get(name));
//						GridData gd = new GridData();
//						gd.widthHint = 190;
//						date.setLayoutData(gd);
//						ListenerHelper listener
//								= new ListenerHelper(editor, model, 
//										new PropertyModifier(model, name));
//						date.addKeyListener(listener);
//					} else 
					if ("textarea".equals(propertyHelper.getEditor())) {
						StyledText textarea = new StyledText(client, SWT.WRAP | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
						textarea.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
						textarea.setText((String) model.getProperties().get(name));
						GridData gd = new GridData();
						gd.heightHint = 80;
						gd.widthHint = 190;
						textarea.setLayoutData(gd);
						IPropertyModifier modifier = new PropertyModifier(model, name);
						textarea.setData(MODIFIER_DATA_REFERENCE, modifier);
						ListenerHelper listener = new ListenerHelper(
								editor, model, modifier, propertyHelper.refreshModel());
						textarea.addKeyListener(listener);
					} else if ("combo".equals(propertyHelper.getEditor())) {
						Combo combo = new Combo(client, SWT.DROP_DOWN);
						combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
						for (String item : helper.getComboItems(propertyHelper.getCombo())) {
							combo.add(item);
						}
						combo.setText((String) model.getProperties().get(name));
						GridData gd = new GridData();
						gd.widthHint = 183;
						combo.setLayoutData(gd);
						IPropertyModifier modifier = new PropertyModifier(model, name);
						combo.setData(MODIFIER_DATA_REFERENCE, modifier);
						ListenerHelper listener	= new ListenerHelper(
								editor, model, modifier, propertyHelper.refreshModel());
						combo.addSelectionListener(listener);
					} else {
						Text text = toolkit.createText(client, 
								(String) model.getProperties().get(name), 
								SWT.SINGLE);
						GridData gd = new GridData();
						gd.widthHint = 200;
						text.setLayoutData(gd);
						IPropertyModifier modifier = new PropertyModifier(model, name);
						text.setData(MODIFIER_DATA_REFERENCE, modifier);
						ListenerHelper listener	= new ListenerHelper(
								editor, model, modifier, propertyHelper.refreshModel());
						text.addKeyListener(listener);
					}
				}
			}
		}
		
//		for(Object nameObj : model.getProperties().keySet()) {
//			final String name = (String) nameObj;
//			toolkit.createLabel(client, name);
//			Text text = toolkit.createText(client, 
//					(String) model.getProperties().get(name), 
//					SWT.SINGLE);
//			GridData gd = new GridData();
//			gd.widthHint = 150;
//			text.setLayoutData(gd);
//			ListenerHelper listener
//				= new ListenerHelper(editor, model, 
//						new PropertyModifier(model, name));
////			text.addFocusListener(listener);
//			text.addKeyListener(listener);
//			//toolkit.paintBordersFor(client);
//		}
	}
	
	private void createMapPropertySections(IManagedForm mform) {
		FormToolkit toolkit = mform.getToolkit();
		
		List<MapPropertyHelper> defMapProperties = helper.getDefMapProperties(model);
		for (MapPropertyHelper mapPropertyHelper : defMapProperties) {
			final String mapName = mapPropertyHelper.getMapName();
			if (model.getMapProperties().containsKey(mapName)) {
				final Composite client = createSection(mform, mapName, "", 1);
				for(Object keyObj : model.getMapProperties().get(mapName).keySet()) {
					final String key = (String) keyObj;
					final Composite subClient = createSubSection(mform, client, key, "", 2);
					List<PropertyHelper> defProperties = mapPropertyHelper.getDefProperties();
					for (PropertyHelper propertyHelper : defProperties) {
						final String name = propertyHelper.getName();
						final String label = propertyHelper.getLabel();
						if (model.getEntryForPropertyMap(mapName, key).containsKey(name)) {
							toolkit.createLabel(subClient, (label == null || "".equals(label)) ? name : label);
							if ("textarea".equals(propertyHelper.getEditor())) {
								StyledText textarea = new StyledText(subClient, SWT.WRAP | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
								textarea.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
								textarea.setText((String) model.getEntryForPropertyMap(mapName, key).get(name));
								GridData gd = new GridData();
								gd.heightHint = 80;
								gd.widthHint = 190;
								textarea.setLayoutData(gd);
								IPropertyModifier modifier = new MapPropertyModifier(model, mapName, key, name);
								textarea.setData(MODIFIER_DATA_REFERENCE, modifier);
								ListenerHelper listener	= new ListenerHelper(
										editor, model, modifier, propertyHelper.refreshModel());
								textarea.addKeyListener(listener);
							} else if ("combo".equals(propertyHelper.getEditor())) {
								Combo combo = new Combo(subClient, SWT.DROP_DOWN);
								combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
								for (String item : helper.getComboItems(propertyHelper.getCombo())) {
									combo.add(item.trim());
								}
								combo.setText((String) model.getEntryForPropertyMap(mapName, key).get(name));
								GridData gd = new GridData();
								gd.widthHint = 183;
								combo.setLayoutData(gd);
								IPropertyModifier modifier = new MapPropertyModifier(model, mapName, key, name);
								combo.setData(MODIFIER_DATA_REFERENCE, modifier);
								ListenerHelper listener	= new ListenerHelper(
										editor, model, modifier, propertyHelper.refreshModel());
								combo.addSelectionListener(listener);
							} else {
								Text text = toolkit.createText(subClient, 
										(String) model.getEntryForPropertyMap(mapName, key).get(name), 
										SWT.SINGLE);
								GridData gd = new GridData();
								gd.widthHint = 200;
								text.setLayoutData(gd);
								IPropertyModifier modifier = new MapPropertyModifier(model, mapName, key, name);
								text.setData(MODIFIER_DATA_REFERENCE, modifier);
								ListenerHelper listener	= new ListenerHelper(
										editor, model, modifier, propertyHelper.refreshModel());
								text.addKeyListener(listener);
							}
						}
					}
				}
			}
		}
		
//		for (String mapName : model.getMapProperties().keySet()) {
//			final Composite client = createSection(mform, mapName, "", 1);
//			for(Object keyObj : model.getMapProperties().get(mapName).keySet()) {
//				final String key = (String) keyObj;
//				final Composite subClient = createSubSection(mform, client, key, "", 2);
//				for (Object nameObj : model.getEntryForPropertyMap(mapName, key).keySet()) {
//					final String name = (String) nameObj;
//					toolkit.createLabel(subClient, name);
//					Text text = toolkit.createText(subClient, 
//							(String) model.getEntryForPropertyMap(mapName, key).get(name), 
//							SWT.SINGLE);
//					GridData gd = new GridData();
//					gd.widthHint = 150;
//					text.setLayoutData(gd);
//					ListenerHelper listener
//						= new ListenerHelper(editor, model, 
//								new MapPropertyModifier(model, mapName, key, name));
//					text.addKeyListener(listener);
//				}
//			}
//		}
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