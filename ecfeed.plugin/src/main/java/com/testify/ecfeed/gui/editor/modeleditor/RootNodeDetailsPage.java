/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.gui.editor.modeleditor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.TableViewerColumnSorter;

import com.testify.ecfeed.gui.common.Messages;
import com.testify.ecfeed.gui.dialogs.RenameModelDialog;
import com.testify.ecfeed.gui.dialogs.TestClassSelectionDialog;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.plugin.utils.EcModelUtils;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.layout.GridData;

public class RootNodeDetailsPage extends GenericNodeDetailsPage{

	private RootNode fSelectedRoot;
	private CheckboxTableViewer fClassesViewer;
	private Section fMainSection;

	private class AddTestClassButtonSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			IType selectedClass = selectClass();

			if(selectedClass != null){
				ClassNode classNode = EcModelUtils.generateClassModel(selectedClass);
				if(fSelectedRoot.getClassModel(classNode.getQualifiedName()) == null){
					fSelectedRoot.addClass(classNode);
					updateModel(fSelectedRoot);
				}
				else{
					MessageDialog infoDialog = new MessageDialog(getActiveShell(), 
							Messages.DIALOG_CLASS_EXISTS_TITLE, Display.getDefault().getSystemImage(SWT.ICON_INFORMATION), 
							Messages.DIALOG_CLASS_EXISTS_MESSAGE, MessageDialog.INFORMATION, 
							new String[] {IDialogConstants.OK_LABEL}, 0);
					infoDialog.open();
				}
			}
		}

		private IType selectClass() {
			TestClassSelectionDialog dialog = new TestClassSelectionDialog(getActiveShell());
			
			if (dialog.open() == IDialogConstants.OK_ID) {
				return (IType)dialog.getFirstResult();
			}
			return null;
		}
	}

	private class RemoveClassesButtonSelectionAdapter extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			MessageDialog infoDialog = new MessageDialog(getActiveShell(), 
					Messages.DIALOG_REMOVE_CLASSES_TITLE, 
					Display.getDefault().getSystemImage(SWT.ICON_WARNING), 
					Messages.DIALOG_REMOVE_CLASSES_MESSAGE, 
					MessageDialog.QUESTION_WITH_CANCEL, 
					new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
					IDialogConstants.OK_ID);
			if(infoDialog.open() == 0){
				removeClasses(fClassesViewer.getCheckedElements());
			}
		}

		private void removeClasses(Object[] checkedElements) {
			for(Object element : checkedElements){
				if(element instanceof ClassNode){
					fSelectedRoot.removeClass((ClassNode)element);
				}
			}
			updateModel(fSelectedRoot);
		}
	}


	public RootNodeDetailsPage(ModelMasterDetailsBlock parentBlock){
		super(parentBlock);
	}
	
	/**
	 * Create contents of the details page.
	 * @param parent
	 */
	public void createContents(Composite parent) {
		parent.setLayout(new FillLayout());
		fMainSection = fToolkit.createSection(parent, Section.TITLE_BAR);

		Composite mainComposite = fToolkit.createComposite(fMainSection, SWT.NONE);
		fToolkit.paintBordersFor(mainComposite);
		fMainSection.setClient(mainComposite);
		mainComposite.setLayout(new GridLayout(1, true));
		
		createClassListViewer(mainComposite);
		createBottomButtons(mainComposite);
		createTextClientComposite(fMainSection);
	}

	private void createClassListViewer(Composite composite) {
		Label classesLabel = new Label(composite, SWT.BOLD);
		classesLabel.setText("Test classes");

		fClassesViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION |SWT.FILL);
		fClassesViewer.setContentProvider(new ArrayContentProvider());
		fClassesViewer.addDoubleClickListener(new ChildrenViewerDoubleClickListener());
		
		Table table = fClassesViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		fToolkit.paintBordersFor(table);

		TableViewerColumn classViewerColumn = 
				createTableViewerColumn(fClassesViewer, "Class", 150, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((ClassNode)element).getLocalName();
			}
		});
		new TableViewerColumnSorter(classViewerColumn) {
			protected Object getValue(Object o) {
				return ((ClassNode)o).getLocalName();
			}
		};

		TableViewerColumn qualifiedNameViewerColumn = 
				createTableViewerColumn(fClassesViewer, "Qualified name", 150, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((ClassNode)element).getQualifiedName();
			}
		});
		new TableViewerColumnSorter(qualifiedNameViewerColumn) {
			protected Object getValue(Object o) {
				return ((ClassNode)o).getLocalName();
			}
		};
	}

	private void createBottomButtons(Composite composite) {
		Composite bottomButtonsComposite = fToolkit.createComposite(composite, SWT.FILL);
		bottomButtonsComposite.setLayout(new GridLayout(2, false));
		bottomButtonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		createButton(bottomButtonsComposite, "Add Test Class...", new AddTestClassButtonSelectionAdapter());
		createButton(bottomButtonsComposite, "Remove selected classes", new RemoveClassesButtonSelectionAdapter());
	}

	private void createTextClientComposite(Section parentSection) {
		Composite textClient = new Composite(parentSection, SWT.NONE);
		textClient.setLayout(new FillLayout());
		createButton(textClient, "Rename...", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				RenameModelDialog dialog = new RenameModelDialog(Display.getDefault().getActiveShell(), fSelectedRoot);
				if(dialog.open() == IDialogConstants.OK_ID){
					fSelectedRoot.setName(dialog.getNewName());
					updateModel(fSelectedRoot);
				}
			}
		});
		parentSection.setTextClient(textClient);
	}

	@Override
	public void refresh() {
		if(fSelectedRoot == null){
			return;
		}
		fClassesViewer.setInput(fSelectedRoot.getClasses());
		fMainSection.setText(fSelectedRoot.toString());
	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		fSelectedRoot = (RootNode)fSelectedNode;
		refresh();
	}

	@Override
	public void modelUpdated(RootNode model) {
		fSelectedRoot = model;
		refresh();
	}

}
