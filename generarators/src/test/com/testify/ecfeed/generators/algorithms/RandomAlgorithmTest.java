package com.testify.ecfeed.generators.algorithms;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.testify.ecfeed.generators.utils.GeneratorTestUtils;
import com.testify.generators.ecfeed.algorithms.IAlgorithm;
import com.testify.generators.ecfeed.algorithms.RandomAlgorithm;
import com.testify.generators.ecfeed.api.GeneratorException;

public class RandomAlgorithmTest {
	final int MAX_VARIABLES = 5;
	final int MAX_PARTITIONS_PER_VARIABLE = 5;
	final int SAMPLE_SIZE = (int) (10 * Math.pow(MAX_PARTITIONS_PER_VARIABLE, MAX_VARIABLES));

	protected GeneratorTestUtils utils = new GeneratorTestUtils(); 
	
	@Test
	public void uniformityTest(){
		for(int variables = 4; variables <= MAX_VARIABLES; variables++){
			for(int partitions = 4; partitions <= MAX_PARTITIONS_PER_VARIABLE; partitions++){
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
	
	@SuppressWarnings("unused")
	private void testUniformity(Map<List<String>, Integer> histogram) {
		Collection<Integer> values = histogram.values();
		int size = values.size();
		double expectedValue = mean(values);
		double chi2 = 0;
		for(int value : values){
			chi2 += Math.pow((value - expectedValue), 2) / expectedValue; 
		}
		double degOfFreedom = values.size() - 1;
		double redusedChi2 = chi2 / degOfFreedom;
//		for(int value : histogram.values()){
//			//assert that the number of times the combination was chosen does not differ from average more than 30%
//			double deviation = Math.abs(value - mean);
//			int histogramSize = histogram.size();
//			assertTrue(Math.abs(value - mean) < mean * 0.5);
//		}
		double variance = variance(histogram.values());
	}

	@Test
	public void finitenessTest(){
		
	}

	@Test
	public void duplicatesTest(){
		
	}
	
	public double mean(Collection<Integer> values){
		if(values.size() == 0) return 0;
		int sum = 0;
		for(Integer value: values){
			sum += value;
		}
		return (double)sum / (double)values.size(); 
	}
	
	public double variance(Collection<Integer> values){
		double mean = mean(values);
		double sum = 0;
		for(int value : values){
			sum += (mean - (double)value) * (mean - (double)value);
		}
		return sum/(double)values.size();
	}
	
	public double stdDev(Collection<Integer> values){
		return Math.sqrt(variance(values));
	}
}
