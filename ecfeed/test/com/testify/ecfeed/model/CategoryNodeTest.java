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

import org.junit.Test;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.MethodNode;
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
		PartitionNode p = new PartitionNode("p", 0);
		PartitionNode p1 = new PartitionNode("p1", 0);
		PartitionNode p11 = new PartitionNode("p11", 0);
		category.addPartition(p);
		p.addPartition(p1);
		p1.addPartition(p11);
		
		assertEquals(p, category.getPartition("p"));
		assertEquals(p1, category.getPartition("p:p1"));
		assertEquals(p11, category.getPartition("p:p1:p11"));
		assertEquals(null, category.getPartition("p1"));
		assertEquals(null, category.getPartition("p11"));
		assertEquals(null, category.getPartition("something"));
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
	public void getChildrenTest() {
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode partition1 = new PartitionNode("partition1", 0); 
		PartitionNode partition2 = new PartitionNode("partition2", 0); 
		category.addPartition(partition1);
		category.addPartition(partition2);
		
		List<? extends IGenericNode> children = category.getChildren();
		assertEquals(2, children.size());
		assertTrue(children.contains(partition1));
		assertTrue(children.contains(partition2));
	}

	@Test
	public void getPartitionNames() {
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode partition1 = new PartitionNode("partition1", 0); 
		PartitionNode partition2 = new PartitionNode("partition2", 0); 
		category.addPartition(partition1);
		category.addPartition(partition2);
		
		List<String> partitionNames = category.getPartitionNames();
		assertTrue(partitionNames.contains("partition1"));
		assertTrue(partitionNames.contains("partition2"));
	}

	@Test
	public void getLeafPartitionNamesTest() {
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode p1 = new PartitionNode("p1", 0); 
		PartitionNode p11 = new PartitionNode("p11", 0); 
		PartitionNode p12 = new PartitionNode("p12", 0); 
		PartitionNode p2 = new PartitionNode("p2", 0);
		p1.addPartition(p11);
		p1.addPartition(p12);
		category.addPartition(p1);
		category.addPartition(p2);
		
		List<String> leafNames = category.getLeafPartitionNames();
		assertTrue(leafNames.contains("p1:p11"));
		assertTrue(leafNames.contains("p1:p12"));
		assertTrue(leafNames.contains("p2"));
		assertFalse(leafNames.contains("p1"));
	}

	@Test
	public void getAllPartitionNamesTest(){
		CategoryNode category = new CategoryNode("category", "type");
		PartitionNode p1 = new PartitionNode("p1", 0); 
		PartitionNode p11 = new PartitionNode("p11", 0); 
		PartitionNode p12 = new PartitionNode("p12", 0); 
		PartitionNode p2 = new PartitionNode("p2", 0);
		p1.addPartition(p11);
		p1.addPartition(p12);
		category.addPartition(p1);
		category.addPartition(p2);
		
		List<String> names = category.getAllPartitionNames();
		
		assertTrue(names.contains("p1"));
		assertTrue(names.contains("p1:p11"));
		assertTrue(names.contains("p1:p12"));
		assertTrue(names.contains("p2"));
	}
	
	
	@Test
	public void getMethodTest() {
		MethodNode method = new MethodNode("method");
		CategoryNode category = new CategoryNode("category", "type");
		method.addCategory(category);
		
		assertEquals(method, category.getMethod());
	}

	@Test
	public void getLeafPartitionsTest(){
		CategoryNode category = new CategoryNode("category", "type");
		
		PartitionNode p1 = new PartitionNode("p1", 0);
		PartitionNode p2 = new PartitionNode("p1", 0);
		PartitionNode p3 = new PartitionNode("p1", 0);
		
		PartitionNode p21 = new PartitionNode("p21", 0);
		PartitionNode p22 = new PartitionNode("p22", 0);
		PartitionNode p23 = new PartitionNode("p23", 0);

		PartitionNode p31 = new PartitionNode("p31", 0);
		PartitionNode p32 = new PartitionNode("p32", 0);
		PartitionNode p33 = new PartitionNode("p33", 0);

		PartitionNode p321 = new PartitionNode("p321", 0);
		PartitionNode p322 = new PartitionNode("p322", 0);
		PartitionNode p323 = new PartitionNode("p323", 0);
		
		category.addPartition(p1);
		category.addPartition(p2);
		category.addPartition(p3);
		p2.addPartition(p21);
		p2.addPartition(p22);
		p2.addPartition(p23);
		p3.addPartition(p31);
		p3.addPartition(p32);
		p3.addPartition(p33);
		p32.addPartition(p321);
		p32.addPartition(p322);
		p32.addPartition(p323);
		
		assertTrue(category.getLeafPartitions().contains(p1));
		assertTrue(category.getLeafPartitions().contains(p21));
		assertTrue(category.getLeafPartitions().contains(p22));
		assertTrue(category.getLeafPartitions().contains(p23));
		assertTrue(category.getLeafPartitions().contains(p31));
		assertTrue(category.getLeafPartitions().contains(p321));
		assertTrue(category.getLeafPartitions().contains(p322));
		assertTrue(category.getLeafPartitions().contains(p323));
		assertTrue(category.getLeafPartitions().contains(p33));
		
		assertFalse(category.getLeafPartitions().contains(p2));
		assertFalse(category.getLeafPartitions().contains(p3));
		assertFalse(category.getLeafPartitions().contains(p32));
	}
	
}