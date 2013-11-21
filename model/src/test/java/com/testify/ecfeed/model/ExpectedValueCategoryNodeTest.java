package com.testify.ecfeed.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExpectedValueCategoryNodeTest {

	@Test
	public void testIsExpected() {
		ExpectedValueCategoryNode category = new ExpectedValueCategoryNode("name", "type", new PartitionNode("name", null));
		assertTrue(category.isExpected());
	}

	@Test
	public void testGetDefaultValue() {
		ExpectedValueCategoryNode category = new ExpectedValueCategoryNode("name", "type", 0);
		assertEquals(0, category.getDefaultValue());
		
		category.setDefaultValue(1);
		assertEquals(1, category.getDefaultValue());
	}
	
	@Test
	public void testGetDefaultValuePartition() {
		ExpectedValueCategoryNode category = new ExpectedValueCategoryNode("name", "type", "value");
		assertEquals("value", category.getDefaultValuePartition().getValue());
	}
}
