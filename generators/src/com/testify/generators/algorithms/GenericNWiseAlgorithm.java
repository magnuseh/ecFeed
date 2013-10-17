/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.generators.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.testify.ecfeed.api.IAlgorithm;
import com.testify.ecfeed.api.IAlgorithmInput;
import com.testify.ecfeed.api.IConstraint;

/**
 * Generic n-wise data generator. 
 * @author Patryk Chamuczynski
 *
 * @param <E>
 */
public class GenericNWiseAlgorithm<E> implements IAlgorithm<E> {
	private int N;

	protected class Constraint implements Predicate<List<E>>{
		IConstraint<E> fConstraint;
		
		Constraint(IConstraint<E> constraint){
			fConstraint = constraint;
		}
		
		@Override
		public boolean apply(List<E> arg) {
			return fConstraint.evaluate(arg);
		}

	}
	
	public GenericNWiseAlgorithm(int n) {
		N = n;
	}
	
	@Override
	public Set<List<E>> generate(IAlgorithmInput<E> input,
			IProgressMonitor progressMonitor){
		
		TupleGenerator<E> tupleGenerator = new TupleGenerator<E>();
		Set<List<E>> nTuples = tupleGenerator.getNTuples(input.getInput(), N);
		Set<List<E>> result = cartesianProduct(input.getInput());
		result = applyConstraints(result, input.getConstraints());

		if(N != input.getInput().size()){
			result = selectTuplesRepresentation(result, nTuples, progressMonitor);
		}
		result = convertToModifiable(result);
		
		return result;
	}

	protected Set<List<E>> cartesianProduct(List<List<E>> input) {
		List<Set<E>> cartesianProductInput = new ArrayList<Set<E>>();
		for(List<E> axis : input){
			cartesianProductInput.add(new LinkedHashSet<E>(axis));
		}
		return new HashSet<List<E>>(Sets.cartesianProduct(cartesianProductInput));
	}

	/**
	 * Filters the input set with the constraints
	 * @param input
	 * @param constraints
	 * @return
	 */
	protected Set<List<E>> applyConstraints(Set<List<E>> input, Collection<IConstraint<E>> constraints) {
		Set<Constraint> predicates = wrapConstraints(constraints);
		Set<List<E>> result = input;
		for(Predicate<List<E>> predicate : predicates){
			result = Sets.filter(result, predicate);
		}
		return result;
	}

	/**
	 * The function wraps provided constraints into guava compatible Predicate set
	 * @param constraints
	 * @return
	 */
	protected Set<Constraint> wrapConstraints(
			Collection<IConstraint<E>> constraints) {
		Set<Constraint> result = new HashSet<Constraint>();
		for(IConstraint<E> constraint : constraints){
			result.add(new Constraint(constraint));
		}
		return result;
	}
	
	/**
	 * The function optimally selects minimal amount of vectors from the input set 
	 * that cover all tuples in the nTuple set 
	 * @param input
	 * @param nTuples
	 * @param progressMonitor
	 * @return
	 */
	protected Set<List<E>> selectTuplesRepresentation(Set<List<E>> input, 
			Set<List<E>> nTuples, IProgressMonitor progressMonitor) {
		if(progressMonitor == null) progressMonitor = new ConsoleProgressMonitor();
		//for guava algorithms we need ordered sets 
		Set<LinkedHashSet<E>> convertedInput = new HashSet<LinkedHashSet<E>>();  
		Set<LinkedHashSet<E>> convertedTuples = new HashSet<LinkedHashSet<E>>();
		for(List<E> vector : input){
			convertedInput.add(new LinkedHashSet<E>(vector));
		}
		for(List<E> tuple : nTuples){
			convertedTuples.add(new LinkedHashSet<E>(tuple));
		}
		
		Set<List<E>> result = new HashSet<List<E>>();
		int elementSize = elementSize(input);
		int totalSize = nTuples.size();
		progressMonitor.beginTask("Generating test data", nTuples.size());
		int maxTuples = combinations(elementSize, N);
		int generatedTuples = 0;
		for(int t = maxTuples; t > 0; t--){
			Iterator<LinkedHashSet<E>> it = convertedInput.iterator();
			while(it.hasNext() && !progressMonitor.isCanceled()){
				LinkedHashSet<E> vector = it.next();						
				Set<LinkedHashSet<E>> usedTuples = new HashSet<LinkedHashSet<E>>(getUsedTuples(vector, convertedTuples));
				if(usedTuples.size() == t){
					convertedTuples.removeAll(usedTuples);
					result.add(new ArrayList<E>(vector));
					it.remove();
					progressMonitor.worked(usedTuples.size());
					generatedTuples += usedTuples.size();
					progressMonitor.subTask("Generated " + result.size() + " test cases with " 
							+ generatedTuples + "/" + totalSize + " " + N + "-tuples\n");
				}
			}
			if(progressMonitor.isCanceled()){
				result.clear();
			}
		}
		progressMonitor.done();
		return result;
	}

	protected int elementSize(Set<List<E>> input) {
		//return size of first element; if list is empty, return 0
		for(List<E> element : input){ 
			return element.size();
		}
		return 0;
	}

	/**
	 * Returns value of binomial coefficient of n choose k, Works for relatively small n and k 
	 * (does not handle overflow) 
	 * @param n
	 * @param k
	 * @return
	 */
	protected int combinations(int n, int k) {
		//return n!/(k!*(n-k)!);
		int coefficient = 1;
		for(int i = n - k + 1; i <= n; i++){
			coefficient *= i;
		}
		for(int i = 1; i <= k; i++){
			coefficient /= i;
		}
		return coefficient;
	}

	
	/**
	 * Returns unmodifiable set of all n-tuples from parameter tuples that appear in vector
	 * @param vector
	 * @param tuples
	 * @return
	 */
	protected Set<LinkedHashSet<E>> getUsedTuples(LinkedHashSet<E> vector, Set<LinkedHashSet<E>> tuples) {
		return Sets.intersection(tuples, Sets.powerSet(vector));
	}

	/**
	 * Returns Set of modifiable lists, used for converting results of guava algorithms that
	 * usually return non-modifiable collections 
	 * @param result
	 * @return
	 */
	private Set<List<E>> convertToModifiable(Set<List<E>> result) {
		LinkedHashSet<List<E>> modifiable = new LinkedHashSet<List<E>>();
		for(List<E> entry : result){
			modifiable.add(new ArrayList<E>(entry));
		}
		return modifiable;
	}
}
