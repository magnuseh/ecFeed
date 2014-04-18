package com.testify.ecfeed.model.constraint;

public interface IConstraintSerializer {
	public Object serialize(Constraint constraint);
	public Object serialize(BasicStatement statement);
	public Object serialize(StatementArray statement);
	public Object serialize(StaticStatement statement);
	public Object serialize(PartitionedCategoryStatement statement);
	public Object serialize(ExpectedValueStatement statement);
}
