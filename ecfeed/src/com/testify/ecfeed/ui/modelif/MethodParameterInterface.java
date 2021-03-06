/*******************************************************************************
 * Copyright (c) 2015 Testify AS..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.ui.modelif;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.testify.ecfeed.adapter.EImplementationStatus;
import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.java.JavaUtils;
import com.testify.ecfeed.adapter.operations.MethodParameterOperationSetLink;
import com.testify.ecfeed.adapter.operations.MethodParameterOperationSetLinked;
import com.testify.ecfeed.adapter.operations.MethodParameterOperationSetType;
import com.testify.ecfeed.adapter.operations.ParameterOperationSetDefaultValue;
import com.testify.ecfeed.adapter.operations.ParameterOperationSetExpected;
import com.testify.ecfeed.model.ChoiceNode;
import com.testify.ecfeed.model.GlobalParameterNode;
import com.testify.ecfeed.model.GlobalParametersParentNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.MethodParameterNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.ui.common.Messages;

public class MethodParameterInterface extends AbstractParameterInterface {

	public MethodParameterInterface(IModelUpdateContext updateContext) {
		super(updateContext);
	}

	public boolean isExpected() {
		return getTarget().isExpected();
	}

	public String getDefaultValue() {
		return getTarget().getDefaultValue();
	}

	public boolean setExpected(boolean expected){
		if(expected != getTarget().isExpected()){
			MethodNode method = getTarget().getMethod();
			if(method != null){
				boolean testCases = method.getTestCases().size() > 0;
				boolean constraints = method.mentioningConstraints(getTarget()).size() > 0;
				if(testCases || constraints){
					String message = "";
					if(testCases){
						if(expected){
							message += Messages.DIALOG_SET_CATEGORY_EXPECTED_TEST_CASES_ALTERED + "\n";
						}
						else{
							message += Messages.DIALOG_SET_CATEGORY_EXPECTED_TEST_CASES_REMOVED + "\n";
						}
					}
					if(constraints){
						message += Messages.DIALOG_SET_CATEGORY_EXPECTED_CONSTRAINTS_REMOVED;
					}
					if(MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
							Messages.DIALOG_SET_CATEGORY_EXPECTED_WARNING_TITLE, message) == false){
						return false;
					}
				}
			}
			return execute(new ParameterOperationSetExpected(getTarget(), expected), Messages.DIALOG_SET_CATEGORY_EXPECTED_PROBLEM_TITLE);
		}
		return false;
	}

	public boolean setDefaultValue(String valueString) {
		if(getTarget().getDefaultValue().equals(valueString) == false){
			IModelOperation operation = new ParameterOperationSetDefaultValue(getTarget(), valueString, getAdapterProvider().getAdapter(getTarget().getType()));
			return execute(operation, Messages.DIALOG_SET_DEFAULT_VALUE_PROBLEM_TITLE);
		}
		return false;
	}

	public String[] defaultValueSuggestions(){
		Set<String> items = new HashSet<String>(getSpecialValues());
		if(JavaUtils.isPrimitive(getType()) == false){
			for(ChoiceNode p : getTarget().getLeafChoices()){
				items.add(p.getValueString());
			}
			if(items.contains(getTarget().getDefaultValue())== false){
				items.add(getTarget().getDefaultValue());
			}
		}
		return items.toArray(new String[]{});
	}

	public boolean setLinked(boolean linked) {
		MethodParameterOperationSetLinked operation = new MethodParameterOperationSetLinked(getTarget(), linked);
		MethodNode method = getTarget().getMethod();
		if(linked){
			GlobalParameterNode link = getTarget().getLink();
			if(link == null || method.getAvailableGlobalParameters().contains(link) == false || method.checkDuplicate(getTarget().getIndex(), link.getType())){
				GlobalParameterNode newLink = findNewLink();
				if(newLink == null){
					MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.DIALOG_SET_PARAMETER_LINKED_PROBLEM_TITLE, Messages.DIALOG_NO_VALID_LINK_AVAILABLE_PROBLEM_MESSAGE);
				}
				operation.addOperation(0, new MethodParameterOperationSetLink(getTarget(), newLink));
			}
		}else{
			//check the type of the unlinked parameter. If it causes collision, set new type
			if(method.checkDuplicate(getTarget().getIndex(), getTarget().getRealType())){
				operation.addOperation(0, new MethodParameterOperationSetType(getTarget(), getTarget().getType(), getAdapterProvider()));
			}
		}

		return execute(operation, Messages.DIALOG_SET_PARAMETER_LINKED_PROBLEM_TITLE);
	}

	public boolean isLinked() {
		return getTarget().isLinked();
	}

	public boolean setLink(GlobalParameterNode link) {
		IModelOperation operation = new MethodParameterOperationSetLink(getTarget(), link);
		return execute(operation, Messages.DIALOG_SET_PARAMETER_LINK_PROBLEM_TITLE);
	}


	public GlobalParameterNode getGlobalParameter(String path) {
		String parameterName = path;
		GlobalParametersParentNode parametersParent;
		if(path.indexOf(":") != -1){
			String parentName = path.substring(0, path.indexOf(":"));
			parameterName = path.substring(path.indexOf(":") + 1);
			parametersParent = getTarget().getMethod().getClassNode();
			if(parametersParent.getName().equals(parentName) == false){
				return null;
			}
		}
		else{
			parametersParent = (RootNode)getTarget().getRoot();
		}
		return parametersParent.getGlobalParameter(parameterName);
	}

	public GlobalParameterNode getLink() {
		return getTarget().getLink();
	}

	public List<GlobalParameterNode> getAvailableLinks() {
		List<GlobalParameterNode> result = new ArrayList<GlobalParameterNode>();
		result.addAll(((RootNode)getTarget().getRoot()).getGlobalParameters());
		result.addAll(getTarget().getMethod().getClassNode().getGlobalParameters());
		return result;
	}

	@Override
	protected MethodParameterNode getTarget(){
		return (MethodParameterNode)super.getTarget();
	}

	@Override
	protected IModelOperation setTypeOperation(String type) {
		return new MethodParameterOperationSetType(getTarget(), type, getAdapterProvider());
	}

	@Override
	public boolean commentsImportExportEnabled(){
		if(getImplementationStatus(getTarget().getMethod()) != EImplementationStatus.NOT_IMPLEMENTED){
			return true;
		}
		if(getImplementationStatus() != EImplementationStatus.NOT_IMPLEMENTED){
			return true;
		}
		return false;
	}

	@Override
	public boolean exportAllComments(){
		boolean result = super.exportAllComments();
		exportTypeJavadocComments();
		return result;
	}

	@Override
	public boolean importTypeCommentsEnabled(){
		return getTarget().isLinked() == false;
	}

	private GlobalParameterNode findNewLink() {
		MethodNode method = getTarget().getMethod();
		int index = getTarget().getIndex();
		for(GlobalParameterNode parameter : method.getAvailableGlobalParameters()){
			if(method.checkDuplicate(index, parameter.getType()) == false){
				return parameter;
			}
		}
		return null;
	}

}
