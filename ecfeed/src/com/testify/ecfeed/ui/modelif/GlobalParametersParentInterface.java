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

import java.util.Collection;
import java.util.List;

import com.testify.ecfeed.adapter.operations.ReplaceMethodParametersWithGlobalOperation;
import com.testify.ecfeed.model.AbstractParameterNode;
import com.testify.ecfeed.model.GlobalParameterNode;
import com.testify.ecfeed.model.GlobalParametersParentNode;
import com.testify.ecfeed.model.MethodParameterNode;
import com.testify.ecfeed.ui.common.EclipseModelBuilder;
import com.testify.ecfeed.ui.common.Messages;

public class GlobalParametersParentInterface extends ParametersParentInterface {

	public GlobalParametersParentInterface(IModelUpdateContext updateContext) {
		super(updateContext);
	}

	@Override
	public AbstractParameterNode addNewParameter() {
		EclipseModelBuilder modelBuilder = new EclipseModelBuilder();
		GlobalParameterNode parameter = new GlobalParameterNode(generateNewParameterName(), generateNewParameterType());
		parameter.addChoices(modelBuilder.defaultChoices(parameter.getType()));
		if(addParameter(parameter, getTarget().getParameters().size())){
			return parameter;
		}
		return null;
	}

	public boolean removeGlobalParameters(Collection<GlobalParameterNode> parameters){
		return super.removeParameters(parameters);
	}

	@Override
	protected GlobalParametersParentNode getTarget(){
		return (GlobalParametersParentNode)super.getTarget();
	}

	public boolean replaceMethodParametersWithGlobal(List<MethodParameterNode> originalParameters) {
		return execute(new ReplaceMethodParametersWithGlobalOperation(getTarget(), originalParameters, getAdapterProvider()), Messages.DIALOG_REPLACE_PARAMETERS_WITH_LINKS_TITLE);
	}
}
