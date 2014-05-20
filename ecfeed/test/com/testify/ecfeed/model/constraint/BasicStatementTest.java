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

package com.testify.ecfeed.model.constraint;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BasicStatementTest {

	private class StatementImplementation extends BasicStatement{
		@Override
		public String getLeftHandName() {
			return null;
		}
		
		public StatementImplementation getCopy(){
			return null;
		}
	}
	
	@Test
	public void testParent() {
		BasicStatement statement1 = new StatementImplementation();
		BasicStatement statement2 = new StatementImplementation();
		
		statement2.setParent(statement1);
		assertEquals(statement1, statement2.getParent());
	}

	@Test
	public void testGetChildren() {
		StatementArray array = new StatementArray(Operator.AND);
		BasicStatement statement2 = new StatementImplementation();
		BasicStatement statement3 = new StatementImplementation();

		array.addStatement(statement2);
		array.addStatement(statement3);
		
		List<BasicStatement> children = array.getChildren();
		assertEquals(2, children.size());
		assertTrue(children.contains(statement2));
		assertTrue(children.contains(statement3));
	}

	@Test
	public void testReplaceChild() {
		StatementArray array = new StatementArray(Operator.AND);
		BasicStatement statement2 = new StatementImplementation();
		BasicStatement statement3 = new StatementImplementation();

		array.addStatement(statement2);
		List<BasicStatement> children = array.getChildren();
		assertEquals(1, children.size());
		assertTrue(children.contains(statement2));
		
		array.replaceChild(statement2, statement3);
		children = array.getChildren();
		assertEquals(1, children.size());
		assertTrue(children.contains(statement3));
	}

	@Test
	public void testRemoveChild() {
		StatementArray array = new StatementArray(Operator.AND);
		BasicStatement statement2 = new StatementImplementation();
		BasicStatement statement3 = new StatementImplementation();

		array.addStatement(statement2);
		array.addStatement(statement3);
		List<BasicStatement> children = array.getChildren();
		assertEquals(2, children.size());
		assertTrue(children.contains(statement2));
		assertTrue(children.contains(statement3));
		
		array.removeChild(statement2);
		children = array.getChildren();
		assertEquals(1, children.size());
		assertFalse(children.contains(statement2));
		assertTrue(children.contains(statement3));
		
	}
}
