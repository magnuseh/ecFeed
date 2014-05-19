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

package com.testify.ecfeed.ui.editor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.ui.dialogs.NewTestClassDialog;
import com.testify.ecfeed.ui.dialogs.TestClassSelectionDialog;
import com.testify.ecfeed.utils.ModelUtils;

public class ClassDetailsPage extends BasicDetailsPage {

	private ClassNode fSelectedClass;
	private MethodsViewer fMethodsSection;
	private OtherMethodsViewer fOtherMethodsSection;
	private Label fQualifiedNameLabel;
	
	private class ReassignClassSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			IType selectedClass = selectClass();

			if(selectedClass != null){
				String qualifiedName = selectedClass.getFullyQualifiedName();
				if(fSelectedClass.getRoot().getClassModel(qualifiedName) == null){
					fSelectedClass.setName(qualifiedName);
					modelUpdated(null);
				}
				else{
					MessageDialog.openInformation(getActiveShell(), 
							Messages.DIALOG_CLASS_EXISTS_TITLE, 
							Messages.DIALOG_CLASS_EXISTS_MESSAGE);
				}
			}
		}
		
		private IType selectClass() {
			TestClassSelectionDialog dialog = new TestClassSelectionDialog(Display.getDefault().getActiveShell());
			
			if (dialog.open() == IDialogConstants.OK_ID) {
				return (IType)dialog.getFirstResult();
			}
			return null;
		}
	}
	
	private class ChangeClassSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e){
			String className = selectClass();

			if (className != null){
				if (fSelectedClass.getRoot().getClassModel(className) == null){
					fSelectedClass.setName(className);
					modelUpdated(null);
				} else {
					MessageDialog.openInformation(getActiveShell(), 
						Messages.DIALOG_CLASS_EXISTS_TITLE, 
						Messages.DIALOG_CLASS_EXISTS_MESSAGE);
				}
			}
		}
		
		private class ChangeTestClassDialog extends NewTestClassDialog {
			public ChangeTestClassDialog(Shell parentShell) {
				super(parentShell);
			}
			@Override
			protected Control createDialogArea(Composite parent) {
				Control area = super.createDialogArea(parent);
				setTitle("Change test class");
				return area;
			}
		}
		
		private String selectClass() {	
			ChangeTestClassDialog dialog = new ChangeTestClassDialog(Display.getDefault().getActiveShell());
			dialog.setInputText(fSelectedClass.getQualifiedName());
			
			if (dialog.open() == IDialogConstants.OK_ID) {
				return dialog.getNewClassName();
			}
			
			return null;
		}
	}
	
	public ClassDetailsPage(ModelMasterSection masterSection) {
		super(masterSection);
	}

	@Override
	public void createContents(Composite parent){
		super.createContents(parent);

		Composite textClientComposite = getToolkit().createComposite(getMainSection());
		textClientComposite.setLayout(new RowLayout());
		
		Button refreshButton = getToolkit().createButton(textClientComposite, "Refresh", SWT.NONE);
		refreshButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				refresh();
			}
		});
		getMainSection().setTextClient(textClientComposite);

		createQualifiedNameComposite(getMainComposite());
		addForm(fMethodsSection = new MethodsViewer(this, getToolkit()));
		addForm(fOtherMethodsSection = new OtherMethodsViewer(this, getToolkit()));
		
		getToolkit().paintBordersFor(getMainComposite());
	}

	
	
	private void createQualifiedNameComposite(Composite parent) {
		Composite composite = getToolkit().createComposite(parent);
		composite.setLayout(new GridLayout(4, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		getToolkit().createLabel(composite, "Class path: ");
		fQualifiedNameLabel = getToolkit().createLabel(composite, "");
		fQualifiedNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button changeButton = getToolkit().createButton(composite, "Change", SWT.NONE);
		changeButton.addSelectionListener(new ChangeClassSelectionAdapter());
		Button button = getToolkit().createButton(composite, "Reassign", SWT.NONE);
		button.addSelectionListener(new ReassignClassSelectionAdapter());
	}

	@Override
	public void refresh(){
		if(getSelectedElement() instanceof ClassNode){
			fSelectedClass = (ClassNode)getSelectedElement();
		}
		if(fSelectedClass != null){
			String title = fSelectedClass.getLocalName();
			if (ModelUtils.isClassImplemented(fSelectedClass)) {
				title += " [implemented]";
			}
			getMainSection().setText(title);
			fQualifiedNameLabel.setText(fSelectedClass.getQualifiedName());
			fMethodsSection.setInput(fSelectedClass);
			fOtherMethodsSection.setInput(fSelectedClass);
			getMainSection().layout();
		}
	}

}
