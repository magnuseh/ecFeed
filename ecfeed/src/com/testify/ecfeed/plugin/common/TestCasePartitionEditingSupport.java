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

package com.testify.ecfeed.plugin.common;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ExpectedValueCategoryNode;
import com.testify.ecfeed.model.PartitionNode;

public class TestCasePartitionEditingSupport extends EditingSupport {
	private final TableViewer fViewer;
	private List<PartitionNode> fTestData;
	private ComboBoxViewerCellEditor fComboCellEditor = null;
	private TextCellEditor fTextCellEditor;
	private IInputChangedListener fSetValueListener;

	public TestCasePartitionEditingSupport(TableViewer viewer, List<PartitionNode> testData, IInputChangedListener setValueListener) {
		super(viewer);
		fViewer = viewer;
		fTestData = testData;
		fSetValueListener = setValueListener;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		PartitionNode partition = (PartitionNode)element;
		if(partition.getCategory() instanceof ExpectedValueCategoryNode){
			return getTextCellEditor(partition);
		}
		else{
			return getComboCellEditor(partition);
		}
	}

	private CellEditor getComboCellEditor(PartitionNode partition) {
		if(fComboCellEditor == null){
			fComboCellEditor = new ComboBoxViewerCellEditor(fViewer.getTable(), SWT.TRAIL);
			fComboCellEditor.setLabelProvider(new LabelProvider());
			fComboCellEditor.setContentProvider(new ArrayContentProvider());
			fComboCellEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_KEY_ACTIVATION | 
					ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		}
		fComboCellEditor.setInput(partition.getCategory().getPartitions());
		fComboCellEditor.setValue(partition);
		return fComboCellEditor;
	}

	private CellEditor getTextCellEditor(PartitionNode partition) {
		if(fTextCellEditor == null){
			fTextCellEditor = new TextCellEditor(fViewer.getTable(), SWT.LEFT); 
		}
		String valueString = partition.getValueString();
		fTextCellEditor.setValue(valueString);
		return fTextCellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		PartitionNode partition = (PartitionNode)element;
		if(partition.getCategory() instanceof ExpectedValueCategoryNode){
			return partition.getValueString();
		}
		return partition.toString();
	}

	@Override
	protected void setValue(Object element, Object value) {
		CategoryNode parent = (CategoryNode)((PartitionNode)element).getParent();
		if(value instanceof String && parent instanceof ExpectedValueCategoryNode){
			String valueString = (String)value;
			if(parent.validatePartitionStringValue(valueString)){
				Object newValue = parent.getPartitionValueFromString(valueString);
				((PartitionNode)element).setValue(newValue);
			}
		}
		else if(value instanceof PartitionNode){
			PartitionNode partition = (PartitionNode)value;
			int parentIndex = parent.getMethod().getCategories().indexOf(parent);
			if(parentIndex >= 0 && parentIndex <= fTestData.size()){
				fTestData.set(parentIndex, partition);
			}
		}
		
		fSetValueListener.inputChanged();
	}
}
