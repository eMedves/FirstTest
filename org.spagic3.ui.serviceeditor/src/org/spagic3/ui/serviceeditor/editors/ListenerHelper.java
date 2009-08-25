package org.spagic3.ui.serviceeditor.editors;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.spagic3.ui.serviceeditor.model.IPropertyModifier;

public class ListenerHelper implements FocusListener, KeyListener, SelectionListener {

	private ServiceEditor editor;
	private FormModelPage formPage;
	private IPropertyModifier modifier;
	private boolean refreshModel;

	public ListenerHelper(ServiceEditor editor, IPropertyModifier modifier, boolean refreshModel) {
		this.editor = editor;
		this.formPage = editor.getFormPage();
		this.modifier = modifier;
		this.refreshModel = false;
		this.refreshModel = refreshModel;
	}
	
	@Override
	public void focusGained(FocusEvent arg0) {
		if (formPage.isDirty()) {
			formPage.refreshModel();
		}
	}

	@Override
	public void focusLost(FocusEvent arg0) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource() instanceof Text) {
			Text text = (Text) e.getSource();
			modifier.setValue(text.getText());
		} else if (e.getSource() instanceof Combo) {
			Combo combo = (Combo) e.getSource();
			modifier.setValue(combo.getText());
		} else if (e.getSource() instanceof StyledText) {
			StyledText textarea = (StyledText) e.getSource();
			modifier.setValue(textarea.getText()/*.replaceAll("\\s+", " ").trim()*/);
		}
		if (refreshModel) {
			formPage.setDirty(true);
		} 
		editor.refreshXML();
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
					formPage.setDirty(true);
				} 
				editor.refreshXML();
			}
		} else if(e.getSource() instanceof Button) {
			Button button = (Button) e.getSource();
			if ("true".equals(modifier.getValue()) ^ button.getSelection()) {
				modifier.setValue(button.getSelection() ? "true" : "false");
				if (refreshModel) {
					formPage.setDirty(true);
				} 
				editor.refreshXML();
			}
		}
	}

}
