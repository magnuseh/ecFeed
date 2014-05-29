package com.testify.ecfeed.model;

import com.testify.ecfeed.model.constraint.BasicStatement;


public interface IModelConverter {
	public Object convert(PartitionNode node);
	public Object convert(BasicStatement statement);
}
