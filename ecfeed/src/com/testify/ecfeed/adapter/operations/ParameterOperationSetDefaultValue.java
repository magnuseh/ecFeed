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

package com.testify.ecfeed.adapter.operations;

import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.ITypeAdapter;
import com.testify.ecfeed.adapter.ModelOperationException;
import com.testify.ecfeed.model.MethodParameterNode;

public class ParameterOperationSetDefaultValue extends AbstractModelOperation {

	private MethodParameterNode fTarget;
	private ITypeAdapter fTypeAdapter;
	private String fNewValue;
	private String fOriginalValue;

	public ParameterOperationSetDefaultValue(MethodParameterNode target, String newValue, ITypeAdapter typeAdapter) {
		super(OperationNames.SET_DEFAULT_VALUE);
		fTarget = target;
		fNewValue = newValue;
		fOriginalValue = target.getDefaultValue();
		fTypeAdapter = typeAdapter;
	}

	@Override
	public void execute() throws ModelOperationException {
		String convertedValue = fTypeAdapter.convert(fNewValue);
		if(convertedValue == null){
			throw new ModelOperationException(Messages.CATEGORY_DEFAULT_VALUE_REGEX_PROBLEM);
		}
		fTarget.setDefaultValueString(convertedValue);
		markModelUpdated();
	}

	@Override
	public IModelOperation reverseOperation() {
		return new ParameterOperationSetDefaultValue(fTarget, fOriginalValue, fTypeAdapter);
	}

}
