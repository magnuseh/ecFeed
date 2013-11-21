package com.testify.ecfeed.model.constraint;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;

public class StatementTest {

	private static MethodNode fMethod;
	private static CategoryNode fCategory;
	private static PartitionNode fPartition1;
	private static PartitionNode fPartition2;
	private static PartitionNode fPartition3;
	private static List<PartitionNode> fList1;
	private static List<PartitionNode> fList2;
	private static List<PartitionNode> fList3;
	
	@BeforeClass
	public static void prepareModel(){
		fMethod = new MethodNode("method");
		fCategory = new CategoryNode("category", "type");
		fPartition1 = new PartitionNode("partition1", null);
		fPartition2 = new PartitionNode("partition2", null);
		fPartition3 = new PartitionNode("partition3", null);
		fCategory.addPartition(fPartition1);
		fCategory.addPartition(fPartition2);
		fCategory.addPartition(fPartition3);
		fMethod.addCategory(fCategory);
		
		fList1 = new ArrayList<PartitionNode>();
		fList1.add(fPartition1);
		fList2 = new ArrayList<PartitionNode>();
		fList2.add(fPartition2);
		fList3 = new ArrayList<PartitionNode>();
		fList3.add(fPartition3);
	}
	
	
	@Test
	public void testEvaluate() {

		Statement statement1 = new Statement(fPartition2, Relation.EQUAL);
		assertFalse(statement1.evaluate(fList1));
		assertTrue(statement1.evaluate(fList2));
		assertFalse(statement1.evaluate(fList3));

		Statement statement2 = new Statement(fPartition2, Relation.LESS);
		assertTrue(statement2.evaluate(fList1));
		assertFalse(statement2.evaluate(fList2));
		assertFalse(statement2.evaluate(fList3));

		Statement statement3 = new Statement(fPartition2, Relation.LESS_EQUAL);
		assertTrue(statement3.evaluate(fList1));
		assertTrue(statement3.evaluate(fList2));
		assertFalse(statement3.evaluate(fList3));
		
		Statement statement4 = new Statement(fPartition2, Relation.NOT);
		assertTrue(statement4.evaluate(fList1));
		assertFalse(statement4.evaluate(fList2));
		assertTrue(statement4.evaluate(fList3));
		
		Statement statement5 = new Statement(fPartition2, Relation.GREATER);
		assertFalse(statement5.evaluate(fList1));
		assertFalse(statement5.evaluate(fList2));
		assertTrue(statement5.evaluate(fList3));
		
		Statement statement6 = new Statement(fPartition2, Relation.GREATER_EQUAL);
		assertFalse(statement6.evaluate(fList1));
		assertTrue(statement6.evaluate(fList2));
		assertTrue(statement6.evaluate(fList3));
}

	@Test
	public void testMentionsPartitionNode() {
		Statement statement = new Statement(fPartition2, Relation.EQUAL);
		assertTrue(statement.mentions(fPartition2));
		assertFalse(statement.mentions(fPartition1));
	}

	@Test
	public void testMentionsCategoryNode() {
		Statement statement = new Statement(fPartition2, Relation.EQUAL);
		CategoryNode category = new CategoryNode("name", "type");
		assertTrue(statement.mentions(fCategory));
		assertFalse(statement.mentions(category));
	}

	@Test
	public void testGetCondition() {
		Statement statement = new Statement(fPartition2, Relation.EQUAL);
		assertEquals(fPartition2, statement.getCondition());
	}

	@Test
	public void testGetRelation() {
		Statement statement = new Statement(fPartition2, Relation.EQUAL);
		assertEquals(Relation.EQUAL, statement.getRelation());
		assertNotEquals(Relation.LESS, statement.getRelation());
	}

}
