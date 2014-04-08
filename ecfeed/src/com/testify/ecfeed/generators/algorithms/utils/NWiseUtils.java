package com.testify.ecfeed.generators.algorithms.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.testify.ecfeed.generators.CartesianProductGenerator;
import com.testify.ecfeed.generators.algorithms.Tuples;
import com.testify.ecfeed.generators.api.GeneratorException;

public class NWiseUtils<E> {

	public Set<List<E>> getAllTuples(List<List<E>> inputDomain, int n) throws GeneratorException {
		Set<List<E>> result = new HashSet<List<E>>();
		Tuples<List<E>> categoryTuples = new Tuples<List<E>>(inputDomain, n);
		while (categoryTuples.hasNext()) {
			List<List<E>> next = categoryTuples.next();
			CartesianProductGenerator<E> generator = new CartesianProductGenerator<E>();
			generator.initialize(next, null, null);
			List<E> tuple;
			while ((tuple = generator.next()) != null) {
				result.add(tuple);
			}
		}
		return result;
	}

	public Set<List<E>> getTuples(List<E> vector, int n) {
		return (new Tuples<E>(vector, n)).getAll();
	}

	public int calculateCoveredTuples(List<List<E>> testcases, int n) {
		HashSet<List<E>> covered = new HashSet<List<E>>();

		List<List<E>> tcases = testcases;
		for (List<E> tcase : tcases) {
			Tuples<E> tuples = new Tuples<E>(tcase, n);

			covered.addAll(tuples.getAll());
		}

		return covered.size();
	}
	
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
