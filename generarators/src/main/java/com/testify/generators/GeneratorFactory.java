package com.testify.generators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.testify.generators.api.GeneratorException;
import com.testify.generators.api.IGenerator;

public class GeneratorFactory<E> {
	
	private Map<String, Class<? extends IGenerator<E>>> fAvailableGenerators;
	
	public GeneratorFactory(){
		fAvailableGenerators = new LinkedHashMap<String, Class<? extends IGenerator<E>>>();
		registerGenerator("N-wise generator", NWiseGenerator.class);
		registerGenerator("Cartesian Product generator", CartesianProductGenerator.class);
		registerGenerator("Random generator", RandomGenerator.class);
	}

	public Set<String> availableGenerators(){
		return fAvailableGenerators.keySet();
	}
	
	public IGenerator<E> getGenerator(String name) throws GeneratorException{
		try {
			return fAvailableGenerators.get(name).newInstance();
		} catch (Exception e) {
			throw new GeneratorException("Cannot instantiate " + name + ": " + e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerGenerator(String name, Class generatorClass) {
		fAvailableGenerators.put(name, generatorClass);
	}
}
