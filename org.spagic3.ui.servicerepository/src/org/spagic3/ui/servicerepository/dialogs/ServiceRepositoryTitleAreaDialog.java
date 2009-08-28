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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;

public class ServiceRepositoryTitleAreaDialog extends TitleAreaDialog {

	public ServiceRepositoryTitleAreaDialog(Shell parentShell) {
		super(parentShell);
		
	}

	public void create() {
			super.create();
			getShell().setText("Service Resources");
	  }
}
