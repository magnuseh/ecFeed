/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.testify.ecfeed.model.PartitionNode;

public class PartitionNodeTest{
	@Test
	public void testValue() {
		PartitionNode partition = new PartitionNode("name", "value");
		assertEquals("value", partition.getValue());
		partition.setValue("new value");
		assertEquals("new value", partition.getValue());
	}
}
