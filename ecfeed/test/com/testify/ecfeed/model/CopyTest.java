package com.testify.ecfeed.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CopyTest{

	RootNode fRoot;

	ClassNode fClass1;
	ClassNode fClass2;
	MethodNode fMethod1;
	MethodNode fMethod2;
	PartitionedCategoryNode fPartCat1;
	PartitionedCategoryNode fPartCat2;
	ExpectedCategoryNode fExCat1;
	ExpectedCategoryNode fExCat2;
	PartitionNode fPartition1;
	PartitionNode fPartition2;
	PartitionNode fPartition3;

	String fLabel1;
	String fLabel2;

	// ConstraintNode fConNode1;
	// ConstraintNode fConNode2;

	@Before
	public void setup(){
		fRoot = new RootNode("Model");
		fClass1 = new ClassNode("com.testify.ecfeed.model.Class1");
		fClass2 = new ClassNode("com.testify.ecfeed.model.Class2");
		fMethod1 = new MethodNode("firstMethod");
		fMethod2 = new MethodNode("secondMethod");
		fPartCat1 = new PartitionedCategoryNode("pcat1", "type");
		fPartCat2 = new PartitionedCategoryNode("pcat2", "type2");
		fExCat1 = new ExpectedCategoryNode("ecat1", "type", "value1");
		fExCat2 = new ExpectedCategoryNode("ecat2", "type", "value2");
		fPartition1 = new PartitionNode("p1", "value1");
		fPartition2 = new PartitionNode("p2", "value2");
		fPartition3 = new PartitionNode("p3", "value3");
		fLabel1 = "label1";
		fLabel2 = "label2";

		fRoot.addClass(fClass1);
		fRoot.addClass(fClass2);
		fClass1.addMethod(fMethod1);
		fClass2.addMethod(fMethod2);
		fMethod1.addCategory(fPartCat1);
		fMethod1.addCategory(fExCat1);
		fMethod2.addCategory(fPartCat2);
		fMethod2.addCategory(fExCat2);
		fPartCat1.addPartition(fPartition1);
		fPartCat2.addPartition(fPartition3);
		fPartition1.addPartition(fPartition2);
		fPartition1.addLabel(fLabel1);
		fPartition2.addLabel(fLabel2);
	}
	
	public void testNode(IGenericNode node, IGenericNode copy){
		assertTrue(node.getClass().isInstance(copy));
		assertNotEquals(node, copy);
		assertEquals(node.getName(), copy.getName());
	}
	
	public void testParent(IGenericNode node, IGenericNode parent, boolean isParent){
		if(isParent)
			assertEquals(node.getParent(), parent);
		else
			assertNotEquals(node.getParent(), parent);
	}

	public void testPartitions(PartitionNode partition, PartitionNode copy){
		testNode(partition, copy);
		assertEquals(partition.getValue(), copy.getValue());
	}
	
	public void testPartitionLabels(PartitionNode partition, PartitionNode copy){
		assertEquals(partition.getLabels().size(), copy.getLabels().size());
		assertEquals(partition.getChildren().size(), copy.getChildren().size());
		// contains all and no more labels?
		assertTrue(copy.getLabels().containsAll(partition.getLabels()));
		assertTrue(partition.getLabels().containsAll(copy.getLabels()));
	}

	public void testPartitionChildrenLabels(PartitionNode childcopy, String parentlabel, String childlabel){
		assertTrue(childcopy.getLabels().contains(childlabel));
		assertTrue(childcopy.getAllLabels().contains(parentlabel));
	}

	@Test
	public void partitionCopyTest(){
		// single partition copied properly?
		PartitionNode copy = fPartition3.getCopy();
		testPartitions(fPartition3, copy);
		testParent(copy, fPartition3.getParent(), true);
		// hierarchical partition copy tests
		// labels copied properly?
		copy = fPartition1.getCopy();
		testPartitionLabels(fPartition1, fPartition1.getCopy());
		// children copied properly?
		PartitionNode childcopy = (PartitionNode)copy.getChild(fPartition2.getName());
		testPartitions(fPartition2, childcopy);
		testParent(childcopy, copy,true);
		testParent(fPartition2, childcopy.getParent(), false);

		// children labels copied properly?
		testPartitionChildrenLabels(childcopy, fLabel1, fLabel2);
	}
	
	public void testPartitionedCategories(PartitionedCategoryNode category, PartitionedCategoryNode copy,
			String parentlabel, String childlabel){
		testNode(category, copy);
		assertEquals(category.getChildren().size(), copy.getChildren().size());
		assertEquals(category.getAllPartitionNames().size(), copy.getAllPartitionNames().size());
		
		// partitions copied properly?
		PartitionNode partition = category.getPartitions().get(0);
		PartitionNode partitioncopy = copy.getPartition(partition.getName());
		testPartitions(partitioncopy, partition);
		testParent(partitioncopy, copy, true);
		// labels copied properly?
		assertTrue(copy.getAllPartitionLabels().contains(parentlabel));
		assertTrue(copy.getAllPartitionLabels().contains(childlabel));
		testPartitionLabels(partition, partitioncopy);
		// children partitions copied properly?
		PartitionNode partitionChild = partition.getPartitions().get(0);
		PartitionNode partitioncopyChild = partitioncopy.getPartition(partitionChild.getName());
		testPartitions(partitioncopyChild, partitionChild);
		testParent(partitioncopyChild, partitioncopy, true);
		// children partition labels copied properly?
		testPartitionChildrenLabels(partitioncopyChild, parentlabel, childlabel);
	}

	@Test
	public void partitionedCategoryCopyTest(){
		PartitionedCategoryNode copy = fPartCat1.getCopy();
		// categories copied properly?
		testPartitionedCategories(fPartCat1, copy, fLabel1, fLabel2);
		testParent(copy, fPartCat1.getParent(), true);
	}
	
	public void testExpectedCategories(ExpectedCategoryNode category, ExpectedCategoryNode copy){
		testNode(category, copy);
		PartitionNode partition = category.getDefaultValuePartition();
		PartitionNode partitioncopy = copy.getDefaultValuePartition();
		testPartitions(partition, partitioncopy);
		testParent(partition, partitioncopy.getParent(), false);
	}
	
	@Test
	public void expectedCategoryCopyTest(){
		ExpectedCategoryNode copy = fExCat1.getCopy();
		testExpectedCategories(fExCat1, copy);
		testParent(copy, fExCat1.getParent(), true);
	}
	
	public void testMethods(MethodNode method, MethodNode copy, String parentlabel, String childlabel){
		testNode(method, copy);
		//	Test partitioned category
		PartitionedCategoryNode partcat = method.getPartitionedCategories().get(0);
		PartitionedCategoryNode copypartcat = copy.getPartitionedCategory(partcat.getName());
		testPartitionedCategories(partcat, copypartcat, parentlabel, childlabel);
		testParent(copypartcat, copy, true);
		// Test expected category
		ExpectedCategoryNode expcat = method.getExpectedCategories().get(0);
		ExpectedCategoryNode copyexpcat = copy.getExpectedCategory(expcat.getName());
		testExpectedCategories(fExCat1, copyexpcat);
		testParent(copyexpcat, copy, true);		
	}
	
	@Test
	public void methodCopyTest(){
		MethodNode copy = fMethod1.getCopy();
		testMethods(fMethod1, copy, fLabel1, fLabel2);
		testParent(fMethod1, copy.getParent(), true);
	}
	
	public void testClasses(ClassNode classnode, ClassNode copy, String parentlabel, String childlabel){
		testNode(classnode, copy);
		
		MethodNode method = classnode.getMethods().get(0);
		MethodNode copymeth = copy.getMethod(method.getName(), method.getCategoriesTypes());
		
		testMethods(method, copymeth, parentlabel, childlabel);
		testParent(copymeth, copy, true);
	}
	
	@Test
	public void classCopyTest(){
		ClassNode copy = fClass1.getCopy();
		testClasses(fClass1, copy, fLabel1, fLabel2);
		testParent(copy, fClass1.getParent(), true);
	}
	
	public void testRoots(RootNode root, RootNode copy, String parentlabel, String childlabel){
		testNode(root, copy);
		
		ClassNode classnode = root.getClasses().get(0);
		ClassNode copyclass = copy.getClassModel(classnode.getName());
		
		testClasses(classnode, copyclass, parentlabel, childlabel);
		testParent(copyclass, copy, true);
	}
	
	@Test
	public void rootCopyTest(){
		RootNode copy = fRoot.getCopy();
		testRoots(fRoot, copy, fLabel1,fLabel2);
		this.testParent(copy, null, true);	
	}	

}
