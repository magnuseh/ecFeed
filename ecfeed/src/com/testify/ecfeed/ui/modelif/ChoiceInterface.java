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

package com.testify.ecfeed.ui.modelif;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.adapter.EImplementationStatus;
import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.java.JavaUtils;
import com.testify.ecfeed.adapter.operations.ChoiceOperationAddLabel;
import com.testify.ecfeed.adapter.operations.ChoiceOperationAddLabels;
import com.testify.ecfeed.adapter.operations.ChoiceOperationRemoveLabels;
import com.testify.ecfeed.adapter.operations.ChoiceOperationRenameLabel;
import com.testify.ecfeed.adapter.operations.ChoiceOperationSetValue;
import com.testify.ecfeed.model.AbstractParameterNode;
import com.testify.ecfeed.model.ChoiceNode;
import com.testify.ecfeed.ui.common.Constants;
import com.testify.ecfeed.ui.common.EclipseTypeAdapterProvider;
import com.testify.ecfeed.ui.common.JavaModelAnalyser;
import com.testify.ecfeed.ui.common.Messages;

public class ChoiceInterface extends ChoicesParentInterface{

	public ChoiceInterface(IModelUpdateContext updateContext) {
		super(updateContext);
	}

	public void setValue(String newValue){
		IModelOperation operation = new ChoiceOperationSetValue(getTarget(), newValue, new EclipseTypeAdapterProvider());
		execute(operation, Messages.DIALOG_SET_CHOICE_VALUE_PROBLEM_TITLE);
	}

	public String getValue() {
		return getTarget().getValueString();
	}

	@Override
	public AbstractParameterNode getParameter() {
		return getTarget().getParameter();
	}

	public boolean removeLabels(Collection<String> labels) {
		boolean removeMentioningConstraints = false;
		for(String label : labels){
			if(getTarget().getParameter().mentioningConstraints(label).size() > 0 && getTarget().getParameter().getLabeledChoices(label).size() == 1){
				removeMentioningConstraints = true;
			}
		}
		if(removeMentioningConstraints){
				if(MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					Messages.DIALOG_REMOVE_LABELS_WARNING_TITLE,
					Messages.DIALOG_REMOVE_LABELS_WARNING_MESSAGE) == false){
				return false;
			}
		}
		return execute(new ChoiceOperationRemoveLabels(getTarget(), labels), Messages.DIALOG_REMOVE_LABEL_PROBLEM_TITLE);
	}

	public String addNewLabel() {
		String newLabel = Constants.DEFAULT_NEW_PARTITION_LABEL;
		int i = 1;
		while(getTarget().getLeafLabels().contains(newLabel)){
			newLabel = Constants.DEFAULT_NEW_PARTITION_LABEL + "(" + i + ")";
			i++;
		}
		if(addLabel(newLabel)){
			return newLabel;
		}
		return null;
	}

	public boolean addLabels(List<String> labels) {
		IModelOperation operation = new ChoiceOperationAddLabels(getTarget(), labels);
		return execute(operation, Messages.DIALOG_ADD_LABEL_PROBLEM_TITLE);
	}

	public boolean addLabel(String newLabel) {
		IModelOperation operation = new ChoiceOperationAddLabel(getTarget(), newLabel);
		return execute(operation, Messages.DIALOG_ADD_LABEL_PROBLEM_TITLE);
	}

	public boolean isLabelInherited(String label) {
		return getTarget().getInheritedLabels().contains(label);
	}

	public boolean renameLabel(String label, String newValue) {
		if(label.equals(newValue)){
			return false;
		}
		if(getTarget().getInheritedLabels().contains(newValue)){
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					Messages.DIALOG_RENAME_LABELS_ERROR_TITLE,
					Messages.DIALOG_LABEL_IS_ALREADY_INHERITED);
				return false;
		}
		if(getTarget().getLeafLabels().contains(newValue)){
			if(MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					Messages.DIALOG_RENAME_LABELS_WARNING_TITLE,
					Messages.DIALOG_DESCENDING_LABELS_WILL_BE_REMOVED_WARNING_TITLE) == false){
				return false;
			}
		}

		IModelOperation operation = new ChoiceOperationRenameLabel(getTarget(), label, newValue);
		return execute(operation, Messages.DIALOG_CHANGE_LABEL_PROBLEM_TITLE);
	}

	@Override
	public boolean goToImplementationEnabled(){
		if(JavaUtils.isPrimitive(getTarget().getParameter().getType())){
			return false;
		}
		if(getTarget().isAbstract()){
			return false;
		}
		return super.goToImplementationEnabled();
	}

	@Override
	public void goToImplementation(){
		try{
			IType type = JavaModelAnalyser.getIType(getParameter().getType());
			if(type != null && getTarget().isAbstract() == false){
				for(IField field : type.getFields()){
					if(field.getElementName().equals(getTarget().getValueString())){
						JavaUI.openInEditor(field);
						break;
					}
				}
			}
		}catch(Exception e){}
	}

	@Override
	protected ChoiceNode getTarget(){
		return (ChoiceNode)super.getTarget();
	}

	@Override
	public boolean commentsImportExportEnabled(){
		return super.commentsImportExportEnabled() && getImplementationStatus() != EImplementationStatus.NOT_IMPLEMENTED;
	}
}
