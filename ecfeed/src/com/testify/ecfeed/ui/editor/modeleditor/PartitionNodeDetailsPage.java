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

package com.testify.ecfeed.ui.editor.modeleditor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.constants.Constants;
import com.testify.ecfeed.constants.DialogStrings;
import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.ui.common.ColorConstants;
import com.testify.ecfeed.ui.common.ColorManager;
import com.testify.ecfeed.ui.dialogs.PartitionSettingsDialog;
import com.testify.ecfeed.utils.EcModelUtils;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;

public class PartitionNodeDetailsPage extends GenericNodeDetailsPage{

	private PartitionNode fSelectedPartition;
	private Section fMainSection;
	private Text fPartitionNameText;
	private Text fPartitionValueText;
	private Button fChangeValueButton;
	private Button fChangeNameButton;
	private CheckboxTableViewer fPartitionsViewer;
	private Table fPartitionsTable;
	private ColorManager fColorManager;
	
	public class PartitionNameEditingSupport extends EditingSupport{
		private TextCellEditor fNameCellEditor;

		public PartitionNameEditingSupport(ColumnViewer viewer) {
			super(viewer);
			fNameCellEditor = new TextCellEditor(fPartitionsTable);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return fNameCellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((PartitionNode)element).getName();
		}

		@Override
		protected void setValue(Object element, Object value) {
			if(!EcModelUtils.validatePartitionName((String)value, fSelectedPartition.getCategory(), (PartitionNode)element)){
				MessageDialog dialog = new MessageDialog(getActiveShell(), 
						DialogStrings.DIALOG_PARTITION_NAME_PROBLEM_TITLE, 
						Display.getDefault().getSystemImage(SWT.ICON_ERROR), 
						DialogStrings.DIALOG_PARTITION_NAME_PROBLEM_MESSAGE,
						MessageDialog.ERROR, 
						new String[] {IDialogConstants.OK_LABEL}, IDialogConstants.OK_ID);
				dialog.open();
			}
			else{
				((PartitionNode)element).setName((String)value);
				updateModel((RootNode)((PartitionNode)element).getRoot());
			}
		}
	}
	
	public class PartitionValueEditingSupport extends EditingSupport{
		private TextCellEditor fValueCellEditor;
		
		public PartitionValueEditingSupport(ColumnViewer viewer) {
			super(viewer);
			fValueCellEditor = new TextCellEditor(fPartitionsTable);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return fValueCellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return ((PartitionNode)element).isAbstract();
		}

		@Override
		protected Object getValue(Object element) {
			return ((PartitionNode)element).getValueString();
		}

		@Override
		protected void setValue(Object element, Object value) {
			String valueString = (String)value;
			if(!EcModelUtils.validatePartitionStringValue(valueString, fSelectedPartition.getCategory())){
				MessageDialog dialog = new MessageDialog(getActiveShell(), 
						DialogStrings.DIALOG_PARTITION_VALUE_PROBLEM_TITLE, 
						Display.getDefault().getSystemImage(SWT.ICON_ERROR), 
						DialogStrings.DIALOG_PARTITION_VALUE_PROBLEM_MESSAGE,
						MessageDialog.ERROR, 
						new String[] {IDialogConstants.OK_LABEL}, IDialogConstants.OK_ID);
				dialog.open();
			}
			else{
				Object newValue = EcModelUtils.getPartitionValueFromString(valueString, fSelectedPartition.getCategory().getType());
				((PartitionNode)element).setValue(newValue);
				updateModel(fSelectedPartition);
			}
		}
	}

	/**
	 * Create the details page.
	 */
	public PartitionNodeDetailsPage(ModelMasterDetailsBlock parentBlock) {
		super(parentBlock);
		fColorManager = new ColorManager();
	}

	/**
	 * Create contents of the details page.
	 * @param parent
	 */
	public void createContents(Composite parent) {
		parent.setLayout(new FillLayout());
		fMainSection = fToolkit.createSection(parent,
				ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);

		Composite mainComposite = fToolkit.createComposite(fMainSection, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		fToolkit.paintBordersFor(mainComposite);
		fMainSection.setClient(mainComposite);

		createNameValueComposite(mainComposite);
		
		createChildrenComposite(mainComposite);
	}

	private void createNameValueComposite(Composite parent) {
		Composite nameAndValueComposite = fToolkit.createComposite(parent, SWT.NONE);
		nameAndValueComposite.setLayout(new GridLayout(3, false));
		nameAndValueComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fToolkit.paintBordersFor(nameAndValueComposite);

		createNameEdit(nameAndValueComposite);
		
		createValueEdit(nameAndValueComposite);
	}

	private void createNameEdit(Composite parent) {
		fToolkit.createLabel(parent, "Partition name");
		fPartitionNameText = fToolkit.createText(parent, null);
		fPartitionNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fPartitionNameText.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
					renamePartition(fPartitionNameText.getText());
				}
			}
		});
		fChangeNameButton = createButton(parent, "Change", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				renamePartition(fPartitionNameText.getText());
			}
		});
	}

	private void createValueEdit(Composite parent) {
		fToolkit.createLabel(parent, "Partition value");
		fPartitionValueText = fToolkit.createText(parent, null);
		fPartitionValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fPartitionValueText.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR){
					changePartitionValue(fPartitionValueText.getText());
				}
			}
		});
		fChangeValueButton = createButton(parent, "Change", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				changePartitionValue(fPartitionValueText.getText());
			}
		});
	}

	private void createChildrenComposite(Composite parent) {
		Composite childrenComposite = fToolkit.createComposite(parent, SWT.NONE);
		childrenComposite.setLayout(new GridLayout(1, false));
		childrenComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fToolkit.createLabel(childrenComposite, "Children");
		
		createChildrenViewer(childrenComposite);
		createChildrenViewerButtons(childrenComposite);
	}

	private void createChildrenViewer(Composite parent) {
		fPartitionsViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.FULL_SELECTION);
		fPartitionsViewer.setContentProvider(new ArrayContentProvider());
		fPartitionsViewer.addDoubleClickListener(new ChildrenViewerDoubleClickListener());
		fPartitionsTable = fPartitionsViewer.getTable();
		fPartitionsTable.setLinesVisible(true);
		fPartitionsTable.setHeaderVisible(true);
		fPartitionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		fToolkit.paintBordersFor(fPartitionsTable);
		
		TableViewerColumn nameViewerColumn = createTableViewerColumn(fPartitionsViewer, "Partition name", 
				190, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				return ((PartitionNode)element).getName();
			}
			
			@Override
			public Color getForeground(Object element){
				return getColor(element);
			}

		});
		nameViewerColumn.setEditingSupport(new PartitionNameEditingSupport(fPartitionsViewer));
		
		TableViewerColumn valueViewerColumn = createTableViewerColumn(fPartitionsViewer, "Value", 
				100, new ColumnLabelProvider(){
			@Override
			public String getText(Object element){
				PartitionNode partition = (PartitionNode)element;
				if(partition.isAbstract()){
					return "ABSTRACT";
				}
				Object partitionValue = partition.getValueString();
				if(partitionValue != null){
					return partitionValue.toString();
				}
				return Constants.NULL_VALUE_STRING_REPRESENTATION;
			}
			
			@Override
			public Color getForeground(Object element){
				return getColor(element);
			}
		});
		valueViewerColumn.setEditingSupport(new PartitionValueEditingSupport(fPartitionsViewer));
	}

	private Color getColor(Object element){
		if(element instanceof PartitionNode){
			PartitionNode partition = (PartitionNode)element;
			if(partition.isAbstract()){
				return fColorManager.getColor(ColorConstants.ABSTRACT_PARTITION);
			}
		}
		return null;
	}

	private void createChildrenViewerButtons(Composite parent) {
		Composite buttonsComposite = fToolkit.createComposite(parent, SWT.NONE);
		buttonsComposite.setLayout(new RowLayout());
		buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		createButton(buttonsComposite, "Add Partition...", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				PartitionSettingsDialog dialog = new PartitionSettingsDialog(getActiveShell(), 
						fSelectedPartition.getCategory(), null);
				if(dialog.open() == Window.OK){
					String partitionName = dialog.getPartitionName();
					Object partitionValue = dialog.getPartitionValue();
					PartitionNode newPartition = new PartitionNode(partitionName, partitionValue);
					fSelectedPartition.addPartition(newPartition);
					CategoryNode category = fSelectedPartition.getCategory(); 
					MethodNode method = category.getMethod();
					int categoryIndex = method.getCategories().indexOf(category);
					//replace the current partition (that is abstract now) by newly created partition
					for(TestCaseNode testCase : method.getTestCases()){
						if(testCase.getTestData().get(categoryIndex) == fSelectedPartition){
							testCase.getTestData().set(categoryIndex, newPartition);
						}
					}
					
					updateModel(fSelectedPartition);
				}
			}
		});

		createButton(buttonsComposite, "Remove Selected", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				MessageDialog dialog = new MessageDialog(getActiveShell(), 
						DialogStrings.DIALOG_REMOVE_PARTITIONS_TITLE, 
						Display.getDefault().getSystemImage(SWT.ICON_WARNING), 
						DialogStrings.DIALOG_REMOVE_PARTITIONS_MESSAGE,
						MessageDialog.QUESTION_WITH_CANCEL, 
						new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
						IDialogConstants.OK_ID);
				if (dialog.open() == Window.OK) {
					for(Object partition : fPartitionsViewer.getCheckedElements()){
						EcModelUtils.removeReferences((PartitionNode)partition);
						fSelectedPartition.removePartition((PartitionNode)partition);
						updateModel(fSelectedPartition);
					}
				}
			}
		});

	}

	private void renamePartition(String name) {
		CategoryNode parent = (CategoryNode)fSelectedPartition.getParent();
		if(EcModelUtils.validatePartitionName(name, parent, fSelectedPartition)){
			fSelectedPartition.setName(name);
			updateModel(fSelectedPartition);
		}
		else{
			MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), 
					DialogStrings.DIALOG_PARTITION_NAME_PROBLEM_TITLE, 
					Display.getDefault().getSystemImage(SWT.ICON_ERROR), 
					DialogStrings.DIALOG_PARTITION_NAME_PROBLEM_MESSAGE,
					MessageDialog.ERROR, new String[] {"OK"}, 0);
			dialog.open();
			fPartitionNameText.setText(fSelectedPartition.getName());
		}
	}

	private void changePartitionValue(String valueString) {
		CategoryNode parent = (CategoryNode)fSelectedPartition.getParent();
		if(EcModelUtils.validatePartitionStringValue(valueString, parent)){
			fSelectedPartition.setValue(EcModelUtils.getPartitionValueFromString(valueString, parent.getType()));
			updateModel(fSelectedPartition);
		}
		else{
			MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), 
					DialogStrings.DIALOG_PARTITION_VALUE_PROBLEM_TITLE, 
					Display.getDefault().getSystemImage(SWT.ICON_ERROR), 
					DialogStrings.DIALOG_PARTITION_VALUE_PROBLEM_MESSAGE,
					MessageDialog.ERROR, new String[] {"OK"}, 0);
			dialog.open();
			fPartitionValueText.setText(fSelectedPartition.getValueString());
		}
	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		fSelectedPartition = (PartitionNode)fSelectedNode;
		refresh();
	}
	
	public void refresh() {
		if(fSelectedPartition == null){
			return;
		}
		fMainSection.setText(fSelectedPartition.toString());
		fPartitionNameText.setText(fSelectedPartition.getName());
		fPartitionValueText.setText(fSelectedPartition.getValueString());
		if(fSelectedPartition.isAbstract()){
			fPartitionNameText.setEnabled(false);
			fChangeNameButton.setEnabled(false);
			fPartitionValueText.setEnabled(false);
			fChangeValueButton.setEnabled(false);
		}
		else{
			fPartitionNameText.setEnabled(true);
			fChangeNameButton.setEnabled(true);
			fPartitionValueText.setEnabled(true);
			fChangeValueButton.setEnabled(true);
		}
		fPartitionsViewer.setInput(fSelectedPartition.getPartitions());
	}

}