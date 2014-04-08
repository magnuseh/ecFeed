/*******************************************************************************
 * Copyright (c) 2013 Testify AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html                                     
 *                                                                               
 * Contributors:                                                                 
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package ObsoleteNWise;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.ui.common.Messages;

public class ObsoleteCalculateCoverageDialog extends ObsoleteCoverageSetupDialog {
	private static final int CONTENT = TEST_CASE_COMPOSITE | COVERAGE_GRAPH_COMPOSITE;

	public ObsoleteCalculateCoverageDialog(Shell parentShell, MethodNode method, IContentProvider contentProvider, IBaseLabelProvider labelProvider) {

		super(parentShell, method, CONTENT, Messages.DIALOG_CALCULATE_COVERAGE_TITLE, Messages.DIALOG_CALCULATE_COVERAGE_MESSAGE,
				contentProvider, labelProvider);
	}

	@Override
	public Point getInitialSize() {
		return new Point(600, 800);
	}

	@Override
	public void okPressed() {
		super.okPressed();
	}

}
