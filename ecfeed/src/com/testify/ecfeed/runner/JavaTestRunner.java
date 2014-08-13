package com.testify.ecfeed.runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.modelif.java.JavaUtils;
import com.testify.ecfeed.modelif.java.ModelClassLoader;
import com.testify.ecfeed.modelif.java.PartitionValueParser;

public class JavaTestRunner {

	private ModelClassLoader fLoader;
	private MethodNode fTarget;
	private Class<?> fTestClass;
	private Method fTestMethod;
	
	public JavaTestRunner(ModelClassLoader loader){
		fLoader = loader;
	}
	
	public void runTestCase(TestCaseNode testCase) throws RunnerException{
		validateTestData(testCase.getTestData());
		try {
			fTestMethod.invoke(fTestClass.newInstance(), getArguments(testCase));
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			throw new RunnerException(Messages.CANNOT_INVOKE_TEST_METHOD(fTarget.toString(), testCase.toString()));
		}
	}
	
	protected Object[] getArguments(TestCaseNode testCase) {
		List<Object> args = new ArrayList<Object>();
		PartitionValueParser parser = new PartitionValueParser(fLoader);
		for(PartitionNode p : testCase.getTestData()){
			args.add(parser.parseValue(p));
		}
		return args.toArray();
	}

	private void validateTestData(List<PartitionNode> testData) throws RunnerException {
		List<String> dataTypes = new ArrayList<String>();
		for(PartitionNode parameter : testData){
			dataTypes.add(parameter.getCategory().getType());
		}
		if(dataTypes.equals(fTarget.getCategoriesTypes()) == false){
			throw new RunnerException(Messages.WRONG_TEST_METHOD_SIGNATURE(fTarget.toString()));
		}
	}

	public void setTarget(MethodNode target) throws RunnerException{
		fTarget = target;
		ClassNode testClassModel = fTarget.getClassNode();
		fTestClass = getTestClass(testClassModel.getQualifiedName());
		fTestMethod = getTestMethod(fTestClass, fTarget);
	}
	
	private Class<?> getTestClass(String qualifiedName) throws RunnerException {
		Class<?> testClass = fLoader.loadClass(qualifiedName);
		if(testClass == null){
			throw new RunnerException(Messages.CANNOT_LOAD_CLASS(qualifiedName));
		}
		return testClass;
	}

	protected Method getTestMethod(Class<?> testClass, MethodNode methodModel) throws RunnerException {
		for(Method method : testClass.getMethods()){
			if(isModel(method, methodModel)){
				return method;
			}
		}
		throw new RunnerException(Messages.METHOD_NOT_FOUND(methodModel.toString()));
	}

	protected boolean isModel(Method method, MethodNode methodModel) {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		List<String> types = new ArrayList<String>();
		for(Class<?> type : parameterTypes){
			types.add(JavaUtils.getTypeName(type.getCanonicalName()));
		}
		return methodName.equals(methodModel.getName()) || types.equals(methodModel.getCategoriesTypes());
	}

}
