/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.testify.ecfeed.generators.api.GeneratorException;
import com.testify.ecfeed.generators.api.IConstraint;
import com.testify.ecfeed.generators.api.IGenerator;
import com.testify.ecfeed.generators.api.IGeneratorParameter;
import com.testify.ecfeed.junit.annotations.Constraints;
import com.testify.ecfeed.junit.annotations.Generator;
import com.testify.ecfeed.junit.annotations.GeneratorParameter;
import com.testify.ecfeed.junit.annotations.GeneratorParameterNames;
import com.testify.ecfeed.junit.annotations.GeneratorParameterValues;
import com.testify.ecfeed.model.ChoiceNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.MethodParameterNode;
import com.testify.ecfeed.runner.Messages;
import com.testify.ecfeed.runner.RunnerException;

public class OnlineRunner extends AbstractJUnitRunner {

	public OnlineRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected List<FrameworkMethod> generateTestMethods() throws RunnerException {
		List<FrameworkMethod> methods = new ArrayList<FrameworkMethod>();
		for(FrameworkMethod method : getTestClass().getAnnotatedMethods(Test.class)){
			if(method.getMethod().getParameterTypes().length == 0){
				//standard jUnit test
				methods.add(method);
			} else{
				MethodNode methodModel = getMethodModel(getModel(), method);
				if(methodModel == null){
					continue;
				}
				IGenerator<ChoiceNode> generator = getGenerator(method);
				List<List<ChoiceNode>> input = getInput(methodModel);
				Collection<IConstraint<ChoiceNode>> constraints = getConstraints(method, methodModel);
				Map<String, Object> parameters = getGeneratorParameters(generator, method);
				try {
					generator.initialize(input, constraints, parameters);
				} catch (GeneratorException e) {
					throw new RunnerException(Messages.GENERATOR_INITIALIZATION_PROBLEM(e.getMessage()));
				}
				methods.add(new RuntimeMethod(method.getMethod(), generator, getLoader()));
			}
		}
		return methods;
	}

	protected Collection<IConstraint<ChoiceNode>> getConstraints(
			FrameworkMethod method, MethodNode methodModel) {
		Collection<String> constraintsNames = constraintsNames(method);
		Collection<IConstraint<ChoiceNode>> constraints = new HashSet<IConstraint<ChoiceNode>>();

		if(constraintsNames != null){
			if(constraintsNames.contains(Constraints.ALL)){
				constraintsNames = methodModel.getConstraintsNames();
			}
			else if(constraintsNames.contains(Constraints.NONE)){
				constraintsNames.clear();
			}

			for(String name : constraintsNames){
				constraints.addAll(methodModel.getConstraints(name));
			}
		}

		return constraints;
	}

	protected List<List<ChoiceNode>> getInput(MethodNode methodModel) {
		List<List<ChoiceNode>> result = new ArrayList<List<ChoiceNode>>();
		for(MethodParameterNode parameter : methodModel.getMethodParameters()){
			if(parameter.isExpected()){
				ChoiceNode choice = new ChoiceNode("expected", parameter.getDefaultValue());
				choice.setParent(parameter);
				result.add(Arrays.asList(new ChoiceNode[]{choice}));
			}
			else{
				result.add(parameter.getLeafChoices());
			}
		}
		return result;
	}

	protected IGenerator<ChoiceNode> getGenerator(FrameworkMethod method) throws RunnerException {
		IGenerator<ChoiceNode> generator = getGenerator(method.getAnnotations());
		if(generator == null){
			generator = getGenerator(getTestClass().getAnnotations());
		}
		if(generator == null){
			throw new RunnerException(Messages.NO_VALID_GENERATOR(method.getName()));
		}
		return generator;
	}

	protected Set<String> constraintsNames(FrameworkMethod method) {
		Set<String> names = constraintsNames(method.getAnnotations());
		if(names == null){
			names = constraintsNames(getTestClass().getAnnotations());
		}
		return names;
	}

	private Map<String, Object> getGeneratorParameters(
			IGenerator<ChoiceNode> generator, FrameworkMethod method) throws RunnerException {
		List<IGeneratorParameter> parameters = generator.parameters();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String>	parsedParameters = parseParameters(method.getAnnotations());{
			if(parsedParameters.size() == 0){
				parsedParameters = parseParameters(getTestClass().getAnnotations());
			}
		}
		for(IGeneratorParameter parameter : parameters){
			Object value = getParameterValue(parameter, parsedParameters);
			if(value == null && parameter.isRequired()){
				throw new RunnerException(Messages.MISSING_REQUIRED_PARAMETER(parameter.getName()));
			}
			else if(value != null){
				result.put(parameter.getName(), value);
			}
		}
		return result;
	}

	private Object getParameterValue(IGeneratorParameter parameter,
			Map<String, String> parsedParameters) throws RunnerException {
		String valueString = parsedParameters.get(parameter.getName());
		if(valueString != null){
			try{
				switch (parameter.getType()) {
				case BOOLEAN:
					return Boolean.parseBoolean(valueString);
				case DOUBLE:
					return Double.parseDouble(valueString);
				case INTEGER:
					return Integer.parseInt(valueString);
				case STRING:
					return valueString;
				}
			}
			catch(Throwable e){
				throw new RunnerException(Messages.WRONG_PARAMETER_TYPE(parameter.getName(), e.getMessage()));
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IGenerator<ChoiceNode> getGenerator(Annotation[] annotations) throws RunnerException{
		IGenerator<ChoiceNode> generator = null;
		for(Annotation annotation : annotations){
			if(annotation instanceof Generator){
				try {
					Class<? extends IGenerator> generatorClass = ((Generator)annotation).value();
					generatorClass.getTypeParameters();
					Constructor<? extends IGenerator> constructor = generatorClass.getConstructor(new Class<?>[]{});
					generator = (constructor.newInstance(new Object[]{}));
				} catch (Exception e) {
					throw new RunnerException(Messages.CANNOT_INSTANTIATE_GENERATOR(e.getMessage()));
				}
			}
		}
		return generator;
	}

	private Map<String, String> parseParameters(Annotation[] annotations) throws RunnerException {
		Map<String, String> result = new HashMap<String, String>();

		String[] parameterNames = null;
		String[] parameterValues = null;
		for(Annotation annotation : annotations){
			if(annotation instanceof GeneratorParameter){
				GeneratorParameter parameter = (GeneratorParameter)annotation;
				result.put(parameter.name(), parameter.value());
			}
			else if(annotation instanceof GeneratorParameterNames){
				parameterNames = ((GeneratorParameterNames)annotation).value();
			}
			else if(annotation instanceof GeneratorParameterValues){
				parameterValues = ((GeneratorParameterValues)annotation).value();
			}
		}
		if(parameterNames != null && parameterValues != null){
			if(parameterNames.length != parameterValues.length){
				throw new RunnerException(Messages.PARAMETERS_ANNOTATION_LENGTH_ERROR);
			}
			for(int i = 0; i < parameterNames.length; i++){
				result.put(parameterNames[i], parameterValues[i]);
			}
		}
		else if(parameterNames != null || parameterValues != null){
			throw new RunnerException(Messages.MISSING_PARAMETERS_ANNOTATION);
		}
		return result;
	}

	private Set<String> constraintsNames(Annotation[] annotations) {
		for(Annotation annotation : annotations){
			if(annotation instanceof Constraints){
				String[] constraints = ((Constraints)annotation).value();
				return new HashSet<String>(Arrays.asList(constraints));
			}
		}
		return null;
	}
}
