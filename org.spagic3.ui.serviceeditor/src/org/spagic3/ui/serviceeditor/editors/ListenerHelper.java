package org.spagic3.ui.serviceeditor.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;
import org.spagic3.ui.serviceeditor.model.IPropertyModifier;
import org.spagic3.ui.serviceeditor.model.IServiceModel;

public class ListenerHelper implements FocusListener, KeyListener {

	private ServiceEditor editor;
	private IServiceModel model;
	private IPropertyModifier modifier; 

	public ListenerHelper(ServiceEditor editor, IServiceModel model, IPropertyModifier modifier) {
		this.editor = editor;
		this.model = model;
		this.modifier = modifier;
	}
	
	@Override
	public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource() instanceof Text) {
			Text text = (Text) e.getSource();
			modifier.setValue(text.getText());
			editor.refreshModel();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.character == SWT.CR) {
			if (e.getSource() instanceof Text) {
				Text text = (Text) e.getSource();
				modifier.setValue(text.getText());
				editor.refreshModel();
			}
		}
	}

}
