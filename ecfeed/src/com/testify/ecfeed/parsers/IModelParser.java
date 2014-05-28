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

package com.testify.ecfeed.parsers;

import java.io.InputStream;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.ExpectedCategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.PartitionedCategoryNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.StatementArray;

public interface IModelParser {
	public RootNode parseModel(InputStream istream) throws ParserException;
	public ClassNode parseClass(InputStream istream) throws ParserException;
	public MethodNode parseMethod(InputStream istream) throws ParserException;
	public ExpectedCategoryNode parseExpectedCategory(InputStream istream) throws ParserException;
	public PartitionedCategoryNode parsePartitionedCategory(InputStream istream) throws ParserException;
	public PartitionNode parsePartition(InputStream istream) throws ParserException;
	public TestCaseNode parseTestCase(InputStream istream) throws ParserException;
	public ConstraintNode parseConstraint(InputStream istream) throws ParserException;
	
	public StatementArray parseStatement(InputStream istream) throws ParserException;
}
