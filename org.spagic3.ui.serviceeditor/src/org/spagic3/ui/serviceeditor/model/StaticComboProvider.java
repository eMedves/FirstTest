package org.spagic3.ui.serviceeditor.model;

import java.util.ArrayList;
import java.util.List;

public class StaticComboProvider implements IComboProvider {

	private String config;
	
	public StaticComboProvider(String config) {
		this.config = config;
	}

	@Override
	public List<String> getComboItems() {
		String comboValues = ServiceModelHelper.evalXPathAsString(config, "(/combo-provider/combo-provider-parameter[@name=\"comboValues\"]/@value)");
		List<String> values = new ArrayList<String>();
		for (String value : comboValues.split(";")) {
			values.add(value.trim());
		}
		return values;
	}

}
