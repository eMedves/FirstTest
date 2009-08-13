package org.spagic3.ui.serviceeditor.editors;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.spagic3.ui.serviceeditor.model.IPropertyModifier;
import org.spagic3.ui.serviceeditor.model.IServiceModel;

public class ListenerHelper implements KeyListener, SelectionListener {

	private ServiceEditor editor;
	@SuppressWarnings("unused")
	private IServiceModel model;
	private IPropertyModifier modifier;
	private boolean refreshModel;

	public ListenerHelper(ServiceEditor editor, IServiceModel model, IPropertyModifier modifier, boolean refreshModel) {
		this.editor = editor;
		this.model = model;
		this.modifier = modifier;
		this.refreshModel = false;
//		this.refreshModel = refreshModel;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
//		if(e.character == SWT.CR) {
			if (e.getSource() instanceof Text) {
				Text text = (Text) e.getSource();
				modifier.setValue(text.getText());
			} else if (e.getSource() instanceof Combo) {
				Combo combo = (Combo) e.getSource();
				modifier.setValue(combo.getText());
			} else if (e.getSource() instanceof StyledText) {
				StyledText textarea = (StyledText) e.getSource();
				modifier.setValue(textarea.getText().replaceAll("\\s+", " ").trim());
			}
			if (refreshModel) {
				editor.refreshModel();
			} else {
				editor.refreshXML();
			}
//		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof Combo) {
			Combo combo = (Combo) e.getSource();
			if (!modifier.getValue().equals(combo.getText())) {
				modifier.setValue(combo.getText());
				if (refreshModel) {
					editor.refreshModel();
				} else {
					editor.refreshXML();
				}
			}
		}
	}

}
