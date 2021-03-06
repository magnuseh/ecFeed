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
import com.testify.ecfeed.adapter.ModelOperationException;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.MethodNode;

public class ClassOperationRemoveMethod extends AbstractModelOperation {

	private ClassNode fTarget;
	private MethodNode fMethod;
	private int fCurrentIndex;

	public ClassOperationRemoveMethod(ClassNode target, MethodNode method) {
		super(OperationNames.REMOVE_METHOD);
		fTarget = target;
		fMethod = method;
		fCurrentIndex = fMethod.getIndex();
	}
	
	@Override
	public void execute() throws ModelOperationException {
		fCurrentIndex = fMethod.getIndex();
		if(fTarget.removeMethod(fMethod) == false){
			throw new ModelOperationException(Messages.UNEXPECTED_PROBLEM_WHILE_REMOVING_ELEMENT);
		}
		markModelUpdated();
	}

	@Override
	public IModelOperation reverseOperation() {
		return new ClassOperationAddMethod(fTarget, fMethod, fCurrentIndex);
	}

}
