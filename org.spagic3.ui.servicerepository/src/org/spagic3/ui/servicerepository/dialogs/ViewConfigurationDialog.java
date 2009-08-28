/*******************************************************************************
 * Copyright (c) {2007, 2008} Engineering Ingegneria Informatica S.p.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Andrea Zoppello (Engineering) - initial API and implementation
 *******************************************************************************/
package org.spagic3.ui.servicerepository.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ViewConfigurationDialog extends ServiceRepositoryTitleAreaDialog {
	
    private Composite aComposite = null;
    private String section = null;
    
	public ViewConfigurationDialog(Shell parentShell, String jndiXMLSection) {
		super(parentShell);
		this.section = jndiXMLSection;
	}
	
	/**
	   * @see org.eclipse.jface.window.Window#create() We complete the dialog with
	   *      a title and a message
	   */
	  public void create() {
		super.create();
	    
	    setTitle("Configuration");
	    setMessage("Configuration");
	    
	  }

	 
	  

	@Override
	protected Control createContents(Composite parent) {
		Control ctrl = super.createContents(parent);
		return ctrl;
	}

	protected Control createDialogArea(Composite parent) {	
		aComposite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 15;
    	layout.marginHeight = 10;
    	layout.numColumns = 5;
		aComposite.setLayout(layout);
		
		Text text2 = new Text(aComposite, SWT.BORDER | SWT.WRAP);

		text2.setText(section);
	    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = 500;
    	data.horizontalSpan = 5;
	    
		text2.setLayoutData(data);
		return parent;
  
		
	}
	
	protected void okPressed() {
		
		super.okPressed();
		
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}
	@Override
	protected void buttonPressed(int buttonId) {
		
		super.buttonPressed(buttonId);
	}




	
	
}
