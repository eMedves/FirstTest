package org.spagic3.ui.formeditor.model;

import java.util.ArrayList;
import java.util.List;

public class TableDefinition extends NamedModelPart {

	private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

	public boolean addColumn(ColumnDefinition e) {
		e.setModel(getModel());
		e.setParent(this);
		return columns.add(e);
	}

	public boolean removeColumn(ColumnDefinition o) {
		o.setModel(null);
		o.setParent(null);
		return columns.remove(o);
	}

	public ColumnDefinition[] getColumns() {
		return columns.toArray(new ColumnDefinition[0]);
	}
	
}
