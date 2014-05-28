package com.testify.ecfeed.model;

import java.io.IOException;

import com.testify.ecfeed.model.constraint.BasicStatement;


public interface IModelSerializer {
	public void serialize(IGenericNode node) throws IOException;
//	public void serialize(RootNode node) throws IOException;
//	public void serialize(ClassNode node) throws IOException;
//	public void serialize(MethodNode node) throws IOException;
//	public void serialize(TestCaseNode node) throws IOException;
//	public void serialize(ConstraintNode node) throws IOException;
//	public void serialize(PartitionedCategoryNode node) throws IOException;
//	public void serialize(ExpectedCategoryNode node) throws IOException;
//	public void serialize(PartitionNode node) throws IOException;
//
//	public void serialize(Constraint constraint) throws IOException;
	public void serialize(BasicStatement statement) throws IOException;
//	public void serialize(StatementArray statement) throws IOException;
//	public void serialize(StaticStatement statement) throws IOException;
//	public void serialize(PartitionedCategoryStatement statement) throws IOException;
//	public void serialize(ExpectedValueStatement statement) throws IOException;
}
