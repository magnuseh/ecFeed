package com.testify.ecfeed.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.Constraint;
import com.testify.ecfeed.model.constraint.Relation;
import com.testify.ecfeed.model.constraint.Statement;
import com.testify.ecfeed.model.constraint.StaticStatement;

public class ConstraintNodeTest {

	@Test
	public void testGetChildren(){
		Constraint constraint = new Constraint(new StaticStatement(true), new StaticStatement(true));
		ConstraintNode node = new ConstraintNode("name", constraint);
		assertEquals(0, node.getChildren().size());
	}
	
	@Test
	public void testGetConstraint() {
		Constraint constraint = new Constraint(new StaticStatement(true), new StaticStatement(true));
		ConstraintNode node = new ConstraintNode("name", constraint);
		assertEquals(constraint, node.getConstraint());
	}

	@Test
	public void testGetMethod() {
		MethodNode method = new MethodNode("method");
		Constraint constraint = new Constraint(new StaticStatement(true), new StaticStatement(true));
		ConstraintNode node = new ConstraintNode("name", constraint);
		method.addConstraint(node);
		assertEquals(method, node.getMethod());
	}

	@Test
	public void testEvaluate() {
		List<PartitionNode> values = new ArrayList<PartitionNode>();
		
		Constraint trueConstraint = new Constraint(new StaticStatement(true), new StaticStatement(true));
		Constraint falseConstraint = new Constraint(new StaticStatement(true), new StaticStatement(false));
		
		ConstraintNode trueNode = new ConstraintNode("true constraint node", trueConstraint);
		ConstraintNode falseNode = new ConstraintNode("false constraint node", falseConstraint);
		
		assertTrue(trueNode.evaluate(values));
		assertFalse(falseNode.evaluate(values));
	}

	@Test
	public void testMentions() {
		PartitionNode partition = new PartitionNode("partition", null);
		CategoryNode category = new CategoryNode("category", "type");
		category.addPartition(partition);
		
		BasicStatement mentioningStatement = new Statement(partition, Relation.EQUAL);
		BasicStatement notMentioningStatement = new StaticStatement(false);
		
		Constraint notMentioningConstraint = new Constraint(notMentioningStatement, notMentioningStatement);
		Constraint mentioningConstraint = new Constraint(mentioningStatement, notMentioningStatement);
		
		ConstraintNode mentioningNode = new ConstraintNode("name", mentioningConstraint);
		ConstraintNode notMentioningNode = new ConstraintNode("name", notMentioningConstraint);
		
		assertTrue(mentioningNode.mentions(category));
		assertTrue(mentioningNode.mentions(partition));
		
		assertFalse(notMentioningNode.mentions(partition));
		assertFalse(notMentioningNode.mentions(category));
	}

}
