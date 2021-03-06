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

import java.util.List;

import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.java.JavaUtils;
import com.testify.ecfeed.adapter.operations.GlobalParameterOperationSetType;
import com.testify.ecfeed.model.GlobalParameterNode;
import com.testify.ecfeed.model.MethodParameterNode;

public class GlobalParameterInterface extends AbstractParameterInterface {

	public GlobalParameterInterface(IModelUpdateContext updateContext) {
		super(updateContext);
	}

	public List<MethodParameterNode> getLinkers(){
		return getTarget().getLinkers();
	}

	@Override
	protected GlobalParameterNode getTarget(){
		return (GlobalParameterNode)super.getTarget();
	}

	@Override
	protected IModelOperation setTypeOperation(String type) {
		return new GlobalParameterOperationSetType(getTarget(), type, getAdapterProvider());
	}

	@Override
	public boolean commentsImportExportEnabled(){
		return super.commentsImportExportEnabled() && JavaUtils.isUserType(getType());
	}

	@Override
	public boolean importTypeCommentsEnabled(){
		return true;
	}

}
