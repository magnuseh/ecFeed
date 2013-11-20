package com.testify.generators.ecfeed.algorithms;

import java.util.Collection;
import java.util.List;

import com.testify.generators.ecfeed.api.GeneratorException;
import com.testify.generators.ecfeed.api.IConstraint;

public interface IAlgorithm<E> {
	public void initialize(List<? extends List<E>> input, 
			Collection<? extends IConstraint<E>> constraints) throws GeneratorException;
	public List<E> getNext() throws GeneratorException;
	public void reset();
	public int totalWork();
	public int totalProgress();
	public int workProgress();
}
