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

import java.util.Collection;

import com.testify.ecfeed.adapter.ModelOperationException;
import com.testify.ecfeed.adapter.java.Constants;
import com.testify.ecfeed.model.TestCaseNode;

public class MethodOperationRenameTestCases extends BulkOperation {

	public MethodOperationRenameTestCases(Collection<TestCaseNode> testCases, String newName) throws ModelOperationException {
		super(OperationNames.RENAME_TEST_CASE, false);
		if(newName.matches(Constants.REGEX_TEST_CASE_NODE_NAME) == false){
			throw new ModelOperationException(Messages.TEST_CASE_NAME_REGEX_PROBLEM);
		}
		for(TestCaseNode testCase : testCases){
			addOperation(FactoryRenameOperation.getRenameOperation(testCase, newName));
		}
	}
}
