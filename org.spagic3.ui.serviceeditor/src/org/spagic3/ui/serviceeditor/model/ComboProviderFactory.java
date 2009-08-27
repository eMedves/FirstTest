package org.spagic3.ui.serviceeditor.model;

public class ComboProviderFactory {

	public ComboProviderFactory() {}
	
	public IComboProvider getComboProvider(String type, String config) {
		if("StaticComboProvider".equals(type)) {
			return new StaticComboProvider(config);
		} else if("DataSourcesComboProvider".equals(type)) {
			return new DataSourcesComboProvider(config);
		} else {
			return null;
		}
	}
	
}
