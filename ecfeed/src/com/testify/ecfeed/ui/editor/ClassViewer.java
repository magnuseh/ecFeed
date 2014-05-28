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
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.ui.common.Messages;
import com.testify.ecfeed.utils.ModelUtils;
import com.testify.ecfeed.ui.dialogs.TestClassSelectionDialog;

public class ClassViewer extends CheckboxTableViewerSection {
	private static final int STYLE = Section.EXPANDED | Section.TITLE_BAR;

	private RootNode fModel;
	private TableViewerColumn nameColumn;

	private class AddImplementedClassAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			IType selectedClass = selectClass();

			if(fModel != null){
				addClass(selectedClass, fModel);
			}
		}

		private void addClass(IType selectedClass, RootNode model){
			ClassNode classNode = ModelUtils.generateClassModel(selectedClass);
			if(model.getClassModel(classNode.getQualifiedName()) == null){
				model.addClass(classNode);
				modelUpdated();
			}
			else{
				MessageDialog.openError(getActiveShell(), 
						Messages.DIALOG_CLASS_EXISTS_TITLE,
						Messages.DIALOG_CLASS_EXISTS_MESSAGE);
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
	
	private class RemoveClassesAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if(MessageDialog.openConfirm(getActiveShell(), 
					Messages.DIALOG_REMOVE_CLASSES_TITLE, 
					Messages.DIALOG_REMOVE_CLASSES_MESSAGE)){
				removeClasses(getCheckedElements());
			}
		}
		
		private void removeClasses(Object[] checkedElements) {
			if(fModel != null){
				for(Object element : checkedElements){
					if(element instanceof ClassNode){
						fModel.removeClass((ClassNode)element);
					}
				}
				modelUpdated();
			}
		}
	}
	
	private class AddNewClassAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			String startName = "NewPackage.NewClass";
			String name = startName;
			int i = 1;

			while (true) {
				if (fModel.getClassModel(name) == null) {
					break;
				}
				name = startName + i;
				++i;
			}

			ClassNode classNode = new ClassNode(name);
			fModel.addClass(classNode);
			modelUpdated();
			selectElement(classNode);
			nameColumn.getViewer().editElement(classNode, 0);
		}
	}

	public ClassViewer(BasicDetailsPage parent, FormToolkit toolkit) {
		super(parent.getMainComposite(), toolkit, STYLE, parent);
		
		setText("Classes");
		addButton("Add implemented class", new AddImplementedClassAdapter());
		addButton("New test class", new AddNewClassAdapter());
		addButton("Remove selected", new RemoveClassesAdapter());
		addDoubleClickListener(new SelectNodeDoubleClickListener(parent.getMasterSection()));
	}
	
	@Override
	protected void createTableColumns(){
		nameColumn = addColumn("Class", 150, new ClassViewerColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((ClassNode)element).getLocalName();
			}
		});
		nameColumn.setEditingSupport(new ClassNameEditingSupport(this, false));
		TableViewerColumn qualifiedNameColumn = addColumn("Qualified name", 150, new ClassViewerColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((ClassNode)element).getQualifiedName();
			}
		});
		qualifiedNameColumn.setEditingSupport(new ClassNameEditingSupport(this, true));
	}
	
	public void setInput(RootNode model){
		super.setInput(model.getClasses());
		fModel = model;
	}
	
}
