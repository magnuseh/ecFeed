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

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.PartitionNode;

public class CategoryNodeTest{
	
	@Test
	public void addPartitionTest() {
		CategoryNode category = new CategoryNode("category", "type");
		
		assertEquals(0, category.getPartitions().size());
		
		PartitionNode partition = new PartitionNode("partition", 0); 
		category.addPartition(partition);

		assertEquals(1, category.getPartitions().size());
	}
	
	@Test
	public void getPartitionTest(){
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode partition = new PartitionNode("partition", 0); 
		category.addPartition(partition);
		
		assertEquals(partition, category.getPartition("partition"));
	}

	@Test
	public void getPartitionsTest() {
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode partition1 = new PartitionNode("partition1", 0); 
		PartitionNode partition2 = new PartitionNode("partition2", 0); 
		category.addPartition(partition1);
		category.addPartition(partition2);
		
		List<PartitionNode> partitions = category.getPartitions();
		assertEquals(2, partitions.size());
		assertTrue(partitions.contains(partition1));
		assertTrue(partitions.contains(partition2));
	}

	@Test
	public void getPartitionNames() {
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode partition1 = new PartitionNode("partition1", 0); 
		PartitionNode partition2 = new PartitionNode("partition2", 0); 
		category.addPartition(partition1);
		category.addPartition(partition2);
		
		Set<String> partitionNames = category.getPartitionNames();
		assertTrue(partitionNames.contains("partition1"));
		assertTrue(partitionNames.contains("partition2"));
	}

	@Test
	public void getMethodTest() {
		MethodNode method = new MethodNode("method");
		CategoryNode category = new CategoryNode("category", "type");
		method.addCategory(category);
		
		assertEquals(method, category.getMethod());
	}

	@Test
	public void isExpectedTest() {
		CategoryNode category = new CategoryNode("category", "type");
		assertFalse(category.isExpected());
	}

}
