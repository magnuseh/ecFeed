package com.testify.ecfeed.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestCaseNodeTest {

	@Test
	public void testGetTestData() {
		List<PartitionNode> testData = new ArrayList<PartitionNode>();
		TestCaseNode testCase = new TestCaseNode("name", testData);
		assertEquals(testData, testCase.getTestData());
	}

	@Test
	public void testMentions() {
		PartitionNode partition1 = new PartitionNode("name", 0);
		PartitionNode partition2 = new PartitionNode("name", 0);
		
		List<PartitionNode> testData = new ArrayList<PartitionNode>();
		testData.add(partition1);
		
		TestCaseNode testCase = new TestCaseNode("name", testData);
		
		assertTrue(testCase.mentions(partition1));
		assertFalse(testCase.mentions(partition2));
	}

	@Test
	public void testReplaceValue() {
		PartitionNode partition1 = new PartitionNode("name", 0);
		PartitionNode partition2 = new PartitionNode("name", 0);
		
		List<PartitionNode> testData = new ArrayList<PartitionNode>();
		testData.add(partition1);
		
		TestCaseNode testCase = new TestCaseNode("name", testData);
		
		testCase.replaceValue(0, partition2);
		
		assertTrue(testCase.getTestData().contains(partition2));
		assertFalse(testCase.getTestData().contains(partition1));
	}
}
