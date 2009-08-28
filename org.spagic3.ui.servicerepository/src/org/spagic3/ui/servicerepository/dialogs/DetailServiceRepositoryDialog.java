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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DetailServiceRepositoryDialog extends ServiceRepositoryTitleAreaDialog {

//	final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	

    private Composite aComposite = null;
    private CLabel serviceIdLabel = null;
    private Text serviceIdTextField = null;
    
    private CLabel serviceNameLabel = null;
    private Text serviceNameTextField = null;
    
    private CLabel targetLabel = null;
    private Text targetTextField = null;
    
    private ServiceRepositoryResource serviceBean = null;
    
    

    
    
	public DetailServiceRepositoryDialog(Shell parentShell) {
		super(parentShell);

	}
	
	
				

	/**
	   * @see org.eclipse.jface.window.Window#create() We complete the dialog with
	   *      a title and a message
	   */
	  public void create() {
		super.create();
	    
	    setTitle("New Service");
	    setMessage("New Service");
	    
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
    	aComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
    	
    	
// Riga 1
    	serviceIdLabel = new CLabel(aComposite, SWT.RIGHT);
    	serviceIdLabel.setText(" Service Id ");
    	serviceIdTextField = new Text(aComposite, SWT.BORDER | SWT.FLAT );
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = 200;
    	data.horizontalSpan = 4;
    	serviceIdTextField.setLayoutData(data);    	
    	
    	
 // Riga 2
    	serviceNameLabel = new CLabel(aComposite, SWT.RIGHT);
    	serviceNameLabel.setText(" Service Name ");
    	serviceNameTextField = new Text(aComposite, SWT.BORDER | SWT.FLAT );
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = 200;
    	data.horizontalSpan = 4;
    	serviceNameTextField.setLayoutData(data);
    	
// Riga 3
    	targetLabel = new CLabel(aComposite, SWT.RIGHT);
    	targetLabel.setText(" Target ");
    	targetTextField = new Text(aComposite, SWT.BORDER | SWT.FLAT );
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	data.widthHint = 200;
    	data.horizontalSpan = 4;
    	targetTextField.setLayoutData(data);
    	
    	
        return parent; 
	
	}
	
	protected void okPressed() {
		
		this.serviceBean = new ServiceRepositoryResource(serviceIdTextField.getText());
		serviceBean.setProperty(ServiceRepositoryResource.NAME, serviceNameTextField.getText());
		serviceBean.setProperty(ServiceRepositoryResource.TARGET, targetTextField.getText());

		close();
	}




	public ServiceRepositoryResource getServiceBean() {
		return serviceBean;
	}
	
	
	
	




	
	
}
