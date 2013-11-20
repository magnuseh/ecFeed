package com.testify.ecfeed.generators.algorithms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.inference.TestUtils;
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
		for(int variables = 1; variables <= MAX_VARIABLES; variables++){
			for(int partitions = 2; partitions <= MAX_PARTITIONS_PER_VARIABLE; partitions++){
				uniformityTest(variables, partitions);
			}
		}
	}
	
	protected void uniformityTest(int variables, int partitions) {
		Map<List<String>, Long> histogram = new HashMap<List<String>, Long>();
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
					histogram.put(next, 1l);
				}
			}
		} catch (GeneratorException e) {
			fail("Unexpected generator exception: " + e.getMessage());
		}
		testUniformity(histogram.values());
	}
	
	private void testUniformity(Collection<Long> values) {
		double expectedValue = mean(values);
		List<Double> expectedDistribution = new ArrayList<Double>();
		for(int i = 0; i < values.size(); i++){
			expectedDistribution.add(expectedValue);
		}
		double[] expected = ArrayUtils.toPrimitive(expectedDistribution.toArray(new Double[]{}));
		long[] observed = ArrayUtils.toPrimitive(values.toArray(new Long[]{}));
		boolean notUniform = TestUtils.chiSquareTest(expected, observed, 0.01);
		assertFalse(notUniform);
	}

	@Test
	public void finitenessTest(){
		
	}

	@Test
	public void duplicatesTest(){
		
	}
	
	public double mean(Collection<Long> values){
		if(values.size() == 0) return 0;
		int sum = 0;
		for(long value: values){
			sum += value;
		}
		return (double)sum / (double)values.size(); 
	}
	
	public double variance(Collection<Long> values){
		double mean = mean(values);
		double sum = 0;
		for(long value : values){
			sum += (mean - (double)value) * (mean - (double)value);
		}
		return sum/(double)values.size();
	}
	
	public double stdDev(Collection<Long> values){
		return Math.sqrt(variance(values));
	}
}
