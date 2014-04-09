package com.testify.ecfeed.generators.algorithms.utils;

import java.util.List;

import com.testify.ecfeed.generators.algorithms.Tuples;

public class NWiseUtils<E> {
	
	public int calculateTotalTuples(List<List<E>> input, int n, int coverage) {
		int totalWork = 0;

		Tuples<List<E>> tuples = new Tuples<List<E>>(input, n);
		while (tuples.hasNext()) {
			long combinations = 1;
			List<List<E>> tuple = tuples.next();
			for (List<E> category : tuple) {
				combinations *= category.size();
			}
			totalWork += combinations;
		}

		return (int) Math.ceil(((double) (coverage * totalWork)) / 100);
	}

}
