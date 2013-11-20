package com.testify.generators.algorithms;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.testify.generators.api.GeneratorException;
import com.testify.generators.algorithms.IAlgorithm;
import com.testify.generators.algorithms.RandomAlgorithm;
import com.testify.generators.utils.TestUtils;

public class RandomAlgorithmTest {
	final int MAX_VARIABLES = 5;
	final int MAX_PARTITIONS_PER_VARIABLE = 5;
	final int SAMPLE_SIZE = (int) (100 * Math.pow(MAX_VARIABLES, MAX_PARTITIONS_PER_VARIABLE));

	protected TestUtils utils = new TestUtils(); 
	
	@Test
	public void uniformityTest(){
		for(int variables = 1; variables <= MAX_VARIABLES; variables++){
			for(int partitions = 1; partitions <= MAX_PARTITIONS_PER_VARIABLE; partitions++){
				uniformityTest(variables, partitions);
			}
		}
	}
	
	protected void uniformityTest(int variables, int partitions) {
		Map<List<String>, Integer> histogram = new HashMap<List<String>, Integer>();
		List<List<String>> input = utils.prepareInput(variables, partitions);
		IAlgorithm<String> algorithm = new RandomAlgorithm<String>((int)(SAMPLE_SIZE), true);
		try {
			algorithm.initialize(input, null);
			List<String> next;
			while((next = algorithm.getNext()) != null){
				if(histogram.containsKey(next)){
					histogram.put(next, histogram.get(next) + 1);
				}
				else{
					histogram.put(next, 1);
				}
			}
		} catch (GeneratorException e) {
			fail("Unexpected generator exception: " + e.getMessage());
		}

		//assert that every combination was chosen at least once
		double expectedSize = Math.pow(partitions, variables); 
		assertEquals((int)expectedSize, histogram.size());
		testUniformity(histogram);
	}
	
	private void testUniformity(Map<List<String>, Integer> histogram) {
		int sum = 0;
		for(int value : histogram.values()){
			sum += value;
		}
		double average = (double)sum / (double)histogram.values().size();
		for(int value : histogram.values()){
			//assert that the number of times the combination was chosen does not differ from average more than 30%
			assertTrue(Math.abs(value - average) < average * 0.5);
		}
	}

	@Test
	public void finitenessTest(){
		
	}

	@Test
	public void duplicatesTest(){
		
	}
}
