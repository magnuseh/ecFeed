package com.testify.ecfeed.ui.editor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.ui.common.Messages;

public class ConstraintNameEditingSupport extends EditingSupport{

	private TextCellEditor fNameCellEditor;
	BasicSection fSection;

	public ConstraintNameEditingSupport(ConstraintsListViewer viewer){
		super(viewer.getTableViewer());
		fSection = viewer;
		fNameCellEditor = new TextCellEditor(viewer.getTable());
	}

	@Override
	protected CellEditor getCellEditor(Object element){
		return fNameCellEditor;
	}

	@Override
	protected boolean canEdit(Object element){
		return true;
	}

	@Override
	protected Object getValue(Object element){
		return ((ConstraintNode)element).getName();
	}

	@Override
	protected void setValue(Object element, Object value){
		String newName = (String)value;
		ConstraintNode ConstraintNode = (ConstraintNode)element;
		if(newName == ConstraintNode.getName())
			return;

		boolean validName = ConstraintNode.getMethod().validateConstraintName(newName);
		if(!validName){
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.DIALOG_CONSTRAINT_NAME_PROBLEM_TITLE,
					Messages.DIALOG_CONSTRAINT_NAME_PROBLEM_MESSAGE);
		} else{
			ConstraintNode.setName(newName);
			fSection.modelUpdated();
		}
	}
	
}