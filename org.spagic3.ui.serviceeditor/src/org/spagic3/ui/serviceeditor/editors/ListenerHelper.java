package org.spagic3.ui.serviceeditor.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.spagic3.ui.serviceeditor.model.IPropertyModifier;

public class ListenerHelper implements FocusListener, VerifyKeyListener, ModifyListener, SelectionListener {

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
	public void focusGained(FocusEvent e) {
//		System.out.println("entered focus gained handler");
//		System.out.println("\tformPage.getFocusHolderId()=" + formPage.getFocusHolderId());
//		System.out.println("\tmodifier.getId()=" + modifier.getId());
//		System.out.println("\tformPage.isModelDirty()=" + formPage.isModelDirty());
		formPage.setFocusHolderId(modifier.getId());
		if (formPage.isModelDirty()) {
			if (e.getSource() instanceof Text) {
				formPage.refreshModel();
			} else if (e.getSource() instanceof StyledText) {
				formPage.refreshModel();
			}
		}
	}
	

	@Override
	public void focusLost(FocusEvent e) {}

	
//	@Override
//	public void keyPressed(KeyEvent e) {}
//
//	
//	@Override
//	public void keyReleased(KeyEvent e) {}


	@Override
	public void verifyKey(VerifyEvent e) {
		if (e.getSource() instanceof StyledText) {
			if (e.character == SWT.CR) {
				if ((e.stateMask == SWT.SHIFT)) {
					e.stateMask = 0;
				} else {
					if(refreshModel) {
						e.doit = false;
						formPage.refreshModel();
					}
				}
			}
		}	
	}
	

	@Override
	public void modifyText(ModifyEvent e) {
		if (e.getSource() instanceof Text) {
			Text text = (Text) e.getSource();
			modifier.setValue(text.getText());
		} else if (e.getSource() instanceof StyledText) {
			StyledText textarea = (StyledText) e.getSource();
			modifier.setValue(textarea.getText());
		}
		if (refreshModel) {
			formPage.setModelDirty(true);
		}
		editor.refreshXML();
	}


	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof Combo) {
			Combo combo = (Combo) e.getSource();
			boolean changed = false;
			if (!modifier.getValue().equals(combo.getText())) {
				modifier.setValue(combo.getText());
				changed = true;
			}
			if ((changed && refreshModel) || formPage.isModelDirty()) {
				formPage.refreshModel();
			} else {
				editor.refreshXML();
			}
		} else if(e.getSource() instanceof Button) {
			Button button = (Button) e.getSource();
			boolean changed = false;
			if ("true".equals(modifier.getValue()) ^ button.getSelection()) {
				modifier.setValue(button.getSelection() ? "true" : "false");
				changed = true;
			}
			if ((changed && refreshModel) || formPage.isModelDirty()) {
				formPage.refreshModel();
			} else {
				editor.refreshXML();
			}
		}
	}


}
