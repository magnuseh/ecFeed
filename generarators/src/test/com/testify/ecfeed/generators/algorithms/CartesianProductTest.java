package com.testify.ecfeed.generators.algorithms;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.testify.ecfeed.generators.utils.GeneratorTestUtils;
import com.testify.generators.ecfeed.algorithms.CartesianProductAlgorithm;
import com.testify.generators.ecfeed.algorithms.IAlgorithm;
import com.testify.generators.ecfeed.api.GeneratorException;
import com.testify.generators.ecfeed.api.IConstraint;

public class CartesianProductTest{
	
	final int MAX_VARIABLES = 6;
	final int MAX_PARTITIONS_PER_VARIABLE = 5;
	final IAlgorithm<String> ALGORITHM = new CartesianProductAlgorithm<String>();
	final GeneratorTestUtils utils = new GeneratorTestUtils();
	
	@Test
	public void testCorrectness(){
		try{
			for(int noOfVariables = 1; noOfVariables <= MAX_VARIABLES; noOfVariables++){
				for(int partitionsPerVariable = 1; partitionsPerVariable <= MAX_PARTITIONS_PER_VARIABLE; partitionsPerVariable++){
					List<List<String>> input = utils.prepareInput(noOfVariables, partitionsPerVariable);
					Collection<IConstraint<String>> constraints = null;
					Set<List<String>> referenceSet = referenceSet(input);
					ALGORITHM.initialize(input, constraints);
					Set<List<String>> algorithmResult = utils.algorithmResult(ALGORITHM);
					assertEquals(referenceSet.size(), algorithmResult.size());
					for(List<String> element : referenceSet){
						assertTrue(algorithmResult.contains(element));
					}
				}
			}
		}catch (GeneratorException e) {
			fail("Unexpected generator exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testConstraints(){
		try{
			for(int noOfVariables = 1; noOfVariables <= MAX_VARIABLES; noOfVariables++){
				for(int partitionsPerVariable = 1; partitionsPerVariable <= MAX_PARTITIONS_PER_VARIABLE; partitionsPerVariable++){
					List<List<String>> input = utils.prepareInput(noOfVariables, partitionsPerVariable);
					Collection<IConstraint<String>> constraints = utils.generateRandomConstraints(input);
					Set<List<String>> referenceSet = referenceSet(input);
					referenceSet = filter(referenceSet, constraints);
					ALGORITHM.initialize(input, constraints);
					Set<List<String>> algorithmResult = utils.algorithmResult(ALGORITHM);
					assertEquals(referenceSet.size(), algorithmResult.size());
					for(List<String> element : referenceSet){
						assertTrue(algorithmResult.contains(element));
					}
				}
			}
		}catch (GeneratorException e) {
			fail("Unexpected generator exception: " + e.getMessage());
		}
	}

	private Set<List<String>> filter(Set<List<String>> input,
			Collection<IConstraint<String>> constraints) {
		Set<List<String>> filtered = new HashSet<List<String>>();
		for(List<String> vector : input){
			boolean valid = true;
			for(IConstraint<String> constraint : constraints){
				if(constraint.evaluate(vector) == false){
					valid = false;
					break;
				};
			}
			if(valid == true)
				filtered.add(vector);
		}
		return filtered;
	}

	private Set<List<String>> referenceSet(List<List<String>> input) {
		List<Set<String>> referenceInput = utils.referenceInput(input);
		return Sets.cartesianProduct(referenceInput);
	}

}
