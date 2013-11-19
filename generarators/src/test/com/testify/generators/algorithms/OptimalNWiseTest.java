package com.testify.generators.algorithms;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.testify.generators.api.GeneratorException;
import com.testify.generators.api.IConstraint;
import com.testify.generators.algorithms.IAlgorithm;
import com.testify.generators.algorithms.OptimalNWiseAlgorithm;
import com.testify.generators.algorithms.Tuples;

public class OptimalNWiseTest extends NWiseAlgorithmTest{

	final int MAX_VARIABLES = 5;
	final int MAX_PARTITIONS_PER_VARIABLE = 5;
	
	@Test
	public void testCorrectness() {
		testCorrectness(OptimalNWiseAlgorithm.class, MAX_VARIABLES, MAX_PARTITIONS_PER_VARIABLE);
	}
	
	@Test
	public void testConstraints() {
		testConstraints(OptimalNWiseAlgorithm.class, MAX_VARIABLES, MAX_PARTITIONS_PER_VARIABLE);
	}
	
	@Test
	public void testSize(){
		try{
		for(int variables = 1; variables <= MAX_VARIABLES; variables++){
			for(int partitions = 1; partitions <= MAX_PARTITIONS_PER_VARIABLE; partitions++){
				for(int n = 1; n <= variables; n++){
					List<List<String>>input = utils.prepareInput(variables, partitions);
					Collection<IConstraint<String>> constraints = null;
					IAlgorithm<String> algorithm = new OptimalNWiseAlgorithm<String>(n);

					algorithm.initialize(input, constraints);
					int generatedDataSize = utils.algorithmResult(algorithm).size();
					int referenceDataSize = referenceResult(input, n).size();
					assertTrue(Math.abs(generatedDataSize - referenceDataSize) <= referenceDataSize / 30);
				}
			}
		}
		}catch(GeneratorException e){
			fail("Unexpected generator exception: " + e.getMessage());
		}
	}

	private Set<List<String>> referenceResult(List<List<String>> input, int n) throws GeneratorException {
		List<Set<String>> referenceInput = utils.referenceInput(input); 
		Set<List<String>> cartesianProduct = Sets.cartesianProduct(referenceInput);
		Set<List<String>> referenceResult = new HashSet<List<String>>();
		Set<List<String>> remainingTuples = getAllTuples(input, n);
		for(int k = maxTuples(input, n); k > 0; k--){
			for(List<String> vector : cartesianProduct){
				Set<List<String>> originalTuples = getTuples(vector, n);
				originalTuples.retainAll(remainingTuples);
				if(originalTuples.size() == k){
					referenceResult.add(vector);
					remainingTuples.removeAll(originalTuples);
				}
			}
		}
		return referenceResult;
	}
	
	protected int maxTuples(List<? extends List<String>> input, int n){
		return (new Tuples<List<String>>(input, n)).getAll().size();
	}

	protected Set<List<String>> getTuples(List<String> vector, int n){
		return (new Tuples<String>(vector, n)).getAll();
	}

}
