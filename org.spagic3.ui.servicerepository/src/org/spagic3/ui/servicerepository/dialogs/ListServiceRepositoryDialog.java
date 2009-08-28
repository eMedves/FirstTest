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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.stp.im.resources.ui.providers.IImResourceContentProvider;
import org.eclipse.stp.im.resources.ui.providers.IImResourceLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.spagic3.ui.servicerepository.ServiceRepositoryActivator;
import org.spagic3.ui.servicerepository.IServiceResource;

public class ListServiceRepositoryDialog extends ServiceRepositoryTitleAreaDialog {
	
    private Composite aComposite = null;
	
    private SashForm internalSash  = null; 
    
    private Table tableServices = null;
    private TableViewer tbvServices = null; 
    private List<IServiceResource> theServices = null;
//    private CCombo exportersCombo = null;
//    private CCombo importersCombo = null;

    
	public ListServiceRepositoryDialog(Shell parentShell) {
		
		super(parentShell);

		this.theServices = new ArrayList<IServiceResource>();

	}
	
	
				

	/**
	   * @see org.eclipse.jface.window.Window#create() We complete the dialog with
	   *      a title and a message
	   */
	  public void create() {
		super.create();
		setTitle(" Services Repository ");
	    setMessage(" Service Definitions ");
	   
	  }

	 
	  

	@Override
	protected Control createContents(Composite parent) {
		Control ctrl = super.createContents(parent);
		return ctrl;
	}

	protected Control createDialogArea(Composite parent) {
		
        this.internalSash = new SashForm(parent,  SWT.VERTICAL | SWT.FLAT);
        this.internalSash.setLayoutData(new GridData(GridData.FILL_BOTH));
       	
    	final ListServiceRepositoryDialog theDialog = this;
    	
    	this.theServices = ServiceRepositoryActivator.getServices(); 
    	
        this.tableServices = new Table(internalSash, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        this.tableServices.setLinesVisible(true);
        this.tableServices.setHeaderVisible(true);
        TableColumn column = null;
		column = new TableColumn(tableServices, SWT.LEFT);
		column.setText("Service Id");
		column.setWidth(200);
		column = new TableColumn(tableServices, SWT.LEFT);
		column.setText("Name");
		column.setWidth(200);
		column = new TableColumn(tableServices, SWT.LEFT);
		column.setText("Target");
		column.setWidth(200);
		
        this.tbvServices = new TableViewer(tableServices);
        tbvServices.setContentProvider(new IImResourceContentProvider());
        tbvServices.setLabelProvider(new IImResourceLabelProvider());
        tbvServices.setInput(this.theServices);    
    	
        this.tableServices.addKeyListener(new KeyListener(){
        	public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
        		if (e.keyCode == 127){
        			TableItem[] tis = theDialog.tableServices.getSelection();
        			ServiceRepositoryResource selected = (ServiceRepositoryResource)tis[0].getData();
        			boolean confirm = MessageDialog.openConfirm(
    						getShell(),
    						"Service Definition",
    						"This will remove service "+ selected.getId()+ " from repository. Are you sure ? ");
        			if (confirm){
        				ServiceRepositoryActivator.deleteService(selected);
        				theDialog.tbvServices.setInput(ServiceRepositoryActivator.getServices());
        				theDialog.tbvServices.refresh();
        			}
        			
        		}
        	};
        	public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
        		
        	};
        	
        });
       
       
        
        aComposite = new Composite(internalSash, SWT.NONE);
		final GridLayout layout = new GridLayout();
    	layout.marginWidth = 15;
    	layout.marginHeight = 10;
    	layout.numColumns = 2;
    	aComposite.setLayout(layout);
    	aComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
    	/*
    	String imgPathImpSmx = "icons" + File.separator + "database_imp_smx.png";
    	Image impSmxImage = ImResourcesActivator.getImageDescriptor(
    			imgPathImpSmx).createImage();
    	
    	
    	
    	String imgPathExportJNDI = "icons" + File.separator + "database_export.png";
    	Image exportJNDIImage = ImResourcesActivator.getImageDescriptor(
    			imgPathExportJNDI).createImage();
    	*/
    	
    	
    	
    	// 1 - row
    	
    	
    	
    	CLabel aLabel = new CLabel(aComposite, SWT.LEFT);
        aLabel.setText(" Add a new service ");
    	GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	data.horizontalSpan = 1;
    	aLabel.setLayoutData(data);
    	
    	Button btnAddService = new Button(aComposite, SWT.FLAT);
    	btnAddService.setText(" Add Service ");
    	data = new GridData(GridData.FILL_HORIZONTAL);

    	data.horizontalSpan = 1;
    	btnAddService.setLayoutData(data);
    	
    	
    	btnAddService.addListener(SWT.Selection,new Listener() {
        	public void handleEvent(Event event) {
        		DetailServiceRepositoryDialog dsrDialog = new DetailServiceRepositoryDialog(getShell());
        		dsrDialog.open();
        		ServiceRepositoryResource serviceBean = dsrDialog.getServiceBean();
        		if (serviceBean != null){
        			ServiceRepositoryActivator.addService(serviceBean);
        			theDialog.tbvServices.setInput(ServiceRepositoryActivator.getServices());
    				theDialog.tbvServices.refresh();
        		}
        	}
        });
            	
    	internalSash.setWeights(new int[] {70,30});
        return parent;
	}

	
	protected void okPressed() {
		super.okPressed();
		
	}
	



	protected void createButtonsForButtonBar(Composite parent) {
		
		
	}
	
}
