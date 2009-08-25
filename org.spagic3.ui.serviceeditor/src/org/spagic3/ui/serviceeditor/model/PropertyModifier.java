package org.spagic3.ui.serviceeditor.model;

public class PropertyModifier implements IPropertyModifier {

	private IServiceModel model;
	private String name;
	
	public PropertyModifier(IServiceModel model, String name) {
		this.model = model;
		this.name = name;
	}
	
	public String getValue() {
		return model == null ? null : model.getProperties().getProperty(name);
	}
	
	/* (non-Javadoc)
	 * @see org.spagic3.ui.serviceeditor.model.IPropertyModifier#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		if (model != null) {
			model.getProperties().setProperty(name, value);
		}
	}

	public IServiceModel getModel() {
		return model;
	}

	public void setModel(IServiceModel model) {
		this.model = model;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getId() {
		return name;
	}
	
	
}
