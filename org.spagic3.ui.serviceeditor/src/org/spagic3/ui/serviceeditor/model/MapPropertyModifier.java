package org.spagic3.ui.serviceeditor.model;

public class MapPropertyModifier implements IPropertyModifier {

	private IServiceModel model;
	private String mapName;
	private String key;
	private String name;
	
	public MapPropertyModifier(IServiceModel model, String mapName, String key, String name) {
		this.model = model;
		this.mapName = mapName;
		this.key = key;
		this.name = name;
	}
	
	public String getValue() {
		return model == null ? null : (String) model.getEntryForPropertyMap(mapName, key).get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.spagic3.ui.serviceeditor.model.IPropertyModifier#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		if (model != null) {
			model.getEntryForPropertyMap(mapName, key).put(name, value);
		}
	}

	public IServiceModel getModel() {
		return model;
	}

	public void setModel(IServiceModel model) {
		this.model = model;
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
