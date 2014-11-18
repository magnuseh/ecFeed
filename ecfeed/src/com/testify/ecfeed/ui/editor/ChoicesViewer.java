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

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.forms.widgets.Section;

import com.testify.ecfeed.adapter.java.JavaUtils;
import com.testify.ecfeed.model.ParameterNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.PartitionedNode;
import com.testify.ecfeed.ui.common.NodeNameColumnLabelProvider;
import com.testify.ecfeed.ui.editor.actions.DeleteAction;
import com.testify.ecfeed.ui.editor.actions.ModelViewerActionProvider;
import com.testify.ecfeed.ui.modelif.ParameterInterface;
import com.testify.ecfeed.ui.modelif.ChoiceInterface;
import com.testify.ecfeed.ui.modelif.IModelUpdateContext;
import com.testify.ecfeed.ui.modelif.ModelNodesTransfer;
import com.testify.ecfeed.ui.modelif.PartitionedNodeInterface;

public class ChoicesViewer extends TableViewerSection {

	private final static int STYLE = Section.EXPANDED | Section.TITLE_BAR;

	private PartitionedNodeInterface fParentIf;
	private ChoiceInterface fTableItemIf;

	private TableViewerColumn fNameColumn;
	private TableViewerColumn fValueColumn;

	private class ChoiceNameEditingSupport extends EditingSupport{

		private TextCellEditor fNameCellEditor;

		public ChoiceNameEditingSupport() {
			super(getTableViewer());
			fNameCellEditor = new TextCellEditor(getTable());
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
			String newName = (String)value;
			PartitionNode partition = (PartitionNode)element;

			if(newName.equals(partition.getName()) == false){
				fTableItemIf.setTarget(partition);
				fTableItemIf.setName(newName);
			}
		}
	}

	private class ChoiceValueEditingSupport extends EditingSupport {
		private ComboBoxViewerCellEditor fCellEditor;

		public ChoiceValueEditingSupport(TableViewerSection viewer) {
			super(viewer.getTableViewer());
			fCellEditor = new ComboBoxViewerCellEditor(viewer.getTable(), SWT.TRAIL);
			fCellEditor.setLabelProvider(new LabelProvider());
			fCellEditor.setContentProvider(new ArrayContentProvider());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			PartitionNode node = (PartitionNode)element;
			ParameterNode parameter = node.getCategory();
			if(ParameterInterface.hasLimitedValuesSet(node.getCategory())){
				fCellEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_KEY_ACTIVATION);
			} else {
				fCellEditor.setActivationStyle(SWT.NONE);
			}
			List<String> items = ParameterInterface.getSpecialValues(node.getCategory().getType());
			if(JavaUtils.isUserType(parameter.getType())){
				Set<String> usedValues = parameter.getLeafPartitionValues();
				usedValues.removeAll(items);
				items.addAll(usedValues);
			}
			if(items.contains(node.getValueString()) == false){
				items.add(node.getValueString());
			}
			fCellEditor.setInput(items);
			fCellEditor.getViewer().getCCombo().setEditable(ParameterInterface.isBoolean(node.getCategory().getType()) == false);
			return fCellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return ((PartitionNode)element).isAbstract() == false;
		}

		@Override
		protected Object getValue(Object element) {
			return ((PartitionNode)element).getValueString();
		}

		@Override
		protected void setValue(Object element, Object value) {
			String valueString = null;
			if(value instanceof String){
				valueString = (String)value;
			} else if(value == null){
				valueString = fCellEditor.getViewer().getCCombo().getText();
			}
			fTableItemIf.setTarget((PartitionNode)element);
			fTableItemIf.setValue(valueString);
		}
	}

	private class ChoiceValueLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element){
			if(element instanceof PartitionNode){
				PartitionNode partition = (PartitionNode)element;
				return partition.isAbstract()?"[ABSTRACT]":partition.getValueString();
			}
			return "";
		}
	}

	private class AddChoiceAdapter extends SelectionAdapter{

		@Override
		public void widgetSelected(SelectionEvent e){
			PartitionNode added = fParentIf.addNewPartition();
			if(added != null){
				getTable().setSelection(added.getIndex());
			}
		}
	}

	public ChoicesViewer(ISectionContext sectionContext, IModelUpdateContext updateContext) {
		super(sectionContext, updateContext, STYLE);

		fParentIf = new ParameterInterface(this);
		fTableItemIf = new ChoiceInterface(this);

		fNameColumn.setEditingSupport(new ChoiceNameEditingSupport());
		fValueColumn.setEditingSupport(new ChoiceValueEditingSupport(this));

		getSection().setText("Choices");
		addButton("Add choice", new AddChoiceAdapter());
		addButton("Remove selected", new ActionSelectionAdapter(new DeleteAction(getViewer(), this)));

		addDoubleClickListener(new SelectNodeDoubleClickListener(sectionContext.getMasterSection()));
		setActionProvider(new ModelViewerActionProvider(getTableViewer(), this));
		getViewer().addDragSupport(DND.DROP_COPY|DND.DROP_MOVE, new Transfer[]{ModelNodesTransfer.getInstance()}, new ModelNodeDragListener(getViewer()));
		getViewer().addDropSupport(DND.DROP_COPY|DND.DROP_MOVE, new Transfer[]{ModelNodesTransfer.getInstance()}, new ModelNodeDropListener(getViewer(), this));
	}

	public void setInput(PartitionedNode parent){
		super.setInput(parent.getPartitions());
		fParentIf.setTarget(parent);
	}

	public void setVisible(boolean visible){
		this.getSection().setVisible(visible);
	}

	@Override
	protected void createTableColumns() {
		fNameColumn = addColumn("Name", 150, new NodeNameColumnLabelProvider());
		fValueColumn = addColumn("Value", 150, new ChoiceValueLabelProvider());
	}
}
