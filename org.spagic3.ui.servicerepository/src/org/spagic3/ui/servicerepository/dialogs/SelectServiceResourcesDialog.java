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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.stp.im.resources.IImResource;
import org.eclipse.stp.im.resources.ui.providers.IImResourceContentProvider;
import org.eclipse.stp.im.resources.ui.providers.IImResourceLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SelectServiceResourcesDialog extends TitleAreaDialog {
	
 
	
    private SashForm internalSash  = null; 
    
    private Table tableResources = null;
    private TableViewer tbvResources = null; 
    private List<IImResource> availableResources = null;
   
    private List<IImResource> selectedImResources = null;
    private String title = null;
 
	public SelectServiceResourcesDialog(Shell parentShell, String title, List<IImResource> dataSourcesList) {
		super(parentShell);
		this.title=title;
		this.availableResources = dataSourcesList;
	}
	
	
				

	/**
	   * @see org.eclipse.jface.window.Window#create() We complete the dialog with
	   *      a title and a message
	   */
	  public void create() {
		super.create();
		setTitle(this.title);
	    setMessage("Select Resources To Import In Local Resource Store ");
	  }

	 
	  

	@Override
	protected Control createContents(Composite parent) {
		Control ctrl = super.createContents(parent);
		return ctrl;
	}

	protected Control createDialogArea(Composite parent) {
		
        this.internalSash = new SashForm(parent,  SWT.VERTICAL | SWT.FLAT);
        this.internalSash.setLayoutData(new GridData(GridData.FILL_BOTH));
       	    	
        this.tableResources = new Table(internalSash, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        this.tableResources.setLinesVisible(true);
        this.tableResources.setHeaderVisible(true);
        TableColumn column = null;
		column = new TableColumn(tableResources, SWT.LEFT);
		column.setText("Service Id");
		column.setWidth(200);
		column = new TableColumn(tableResources, SWT.LEFT);
		column.setText("Name");
		column.setWidth(200);
		column = new TableColumn(tableResources, SWT.LEFT);
		column.setText("Target");
		column.setWidth(200);
        this.tbvResources = new TableViewer(tableResources);
        tbvResources.setContentProvider(new IImResourceContentProvider());
        tbvResources.setLabelProvider(new IImResourceLabelProvider());
        tbvResources.setInput(this.availableResources);    

        return parent;
	}
	@Override
	protected void okPressed() {
		
		TableItem[] selection = this.tableResources.getSelection();
		if (selection != null && selection.length > 0){
			this.selectedImResources = new ArrayList<IImResource>();
			for (int i=0; i < selection.length; i++){
				this.selectedImResources.add((IImResource)selection[i].getData());
			}
		}
		super.okPressed();
	}
	
	
	@Override
	protected void cancelPressed() {
		
		this.selectedImResources = null;
		super.cancelPressed();
	}



	protected void createButtonsForButtonBar(Composite parent) {
	
		
		createButton(parent, IDialogConstants.OK_ID, "Import In Local Resource Store",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,
				true);
	}




	public List<IImResource> getSelectedImResources() {
		return selectedImResources;
	}




	public void setSelectedImResources(List<IImResource> selectedImResources) {
		this.selectedImResources = selectedImResources;
	}




	
	




	




	
	
}
