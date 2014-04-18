package com.testify.ecfeed.model;


public interface IModelSerializer {
	public Object serialize(IGenericNode node);
	public Object serialize(RootNode node);
	public Object serialize(ClassNode node);
	public Object serialize(MethodNode node);
	public Object serialize(TestCaseNode node);
	public Object serialize(ConstraintNode node);
	public Object serialize(PartitionedCategoryNode node);
	public Object serialize(ExpectedCategoryNode node);
	public Object serialize(PartitionNode node);
}
