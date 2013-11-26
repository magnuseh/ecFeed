package com.testify.ecfeed.parser;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.testify.ecfeed.generators.RandomGenerator;
import com.testify.ecfeed.generators.api.GeneratorException;
import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.ExpectedValueCategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.Constraint;
import com.testify.ecfeed.model.constraint.Operator;
import com.testify.ecfeed.model.constraint.Relation;
import com.testify.ecfeed.model.constraint.Statement;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.model.constraint.StaticStatement;
import com.testify.ecfeed.parser.xml.XmlModelParser;
import com.testify.ecfeed.parser.xml.XmlModelSerializer;

//import org.apache.commons.lang3.RandomStringUtils;

public class XmlParserSerializerTest {
	private final int TEST_RUNS = 10;
	
	private final int MAX_CLASSES = 10;
	private final int MAX_METHODS = 10;
	private final int MAX_CATEGORIES = 5;
	private final int MAX_EXPECTED_CATEGORIES = 3;
	private final int MAX_PARTITIONS = 10;
	private final int MAX_CONSTRAINTS = 5;
	private final int MAX_TEST_CASES = 50;
	
	Random rand = new Random();
	static int nextInt = 0;
	
	private final String[] CATEGORY_TYPES = new String[]{
			Constants.TYPE_NAME_BOOLEAN, Constants.TYPE_NAME_BYTE, Constants.TYPE_NAME_CHAR, 
			Constants.TYPE_NAME_DOUBLE, Constants.TYPE_NAME_FLOAT, Constants.TYPE_NAME_INT, 
			Constants.TYPE_NAME_LONG, Constants.TYPE_NAME_SHORT, Constants.TYPE_NAME_STRING
	};

	@Test
	public void test() {
		try {
		for(int i = 0; i < TEST_RUNS; ++i){
			RootNode model = createRootNode(rand.nextInt(MAX_CLASSES) + 1);
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			XmlModelSerializer serializer = new XmlModelSerializer(ostream);
			XmlModelParser parser = new XmlModelParser();
			serializer.writeXmlDocument(model);
			System.out.println(ostream.toString());
			ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
			RootNode parsedModel = parser.parseModel(istream);
			compareModels(model, parsedModel);
			
		}
		} catch (IOException e) {
			fail("Unexpected exception");
		} catch (ParserException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}

	protected RootNode createRootNode(int classes) {
		RootNode root = new RootNode(randomName());
		for(int i = 0; i < classes; ++i){
			root.addClass(createClassNode(rand.nextInt(MAX_METHODS) + 1));
		}
		return root;
	}

	protected ClassNode createClassNode(int methods) {
		ClassNode classNode = new ClassNode("com.example." + randomName());
		for(int i = 0; i < methods; ++i){
			int numOfCategories = rand.nextInt(MAX_CATEGORIES);
			int numOfExpCategories = rand.nextInt(MAX_EXPECTED_CATEGORIES);
			if(numOfCategories + numOfExpCategories == 0){
				numOfCategories = 1;
			}
			int numOfConstraints = rand.nextInt(MAX_CONSTRAINTS) + 1;
			int numOfTestCases = rand.nextInt(MAX_TEST_CASES);
			classNode.addMethod(createMethodNode(numOfCategories, numOfExpCategories, numOfConstraints, numOfTestCases));
		}
		return classNode;
	}

	protected MethodNode createMethodNode(int numOfCategories,
			int numOfExpCategories, int numOfConstraints, int numOfTestCases) {
		MethodNode method = new MethodNode(randomName());
		List<? extends CategoryNode> categories = createCategories(numOfCategories, numOfExpCategories);
		List<ConstraintNode> constraints = createConstraints(categories, numOfConstraints);
		List<TestCaseNode> testCases = createTestCases(categories, numOfTestCases);
		
		for(CategoryNode category : categories){
			if(category instanceof ExpectedValueCategoryNode){
				method.addCategory((ExpectedValueCategoryNode)category);
			}
			else{
				method.addCategory(category);
			}
		}
		for(ConstraintNode constraint : constraints){
			method.addConstraint(constraint);
		}
		for(TestCaseNode testCase : testCases){
			method.addTestCase(testCase);
		}
		
		return method;
	}

	private List<CategoryNode> createCategories(int numOfCategories, int numOfExpCategories) {
		List<CategoryNode> categories = createCategoryList(numOfCategories);
		List<ExpectedValueCategoryNode> expCategories = createExpectedCategoriesList(numOfExpCategories);
		List<CategoryNode> result = new ArrayList<CategoryNode>();
		result.addAll(categories);
		result.addAll(expCategories);
		Collections.shuffle(result);
		return result;
	}

	private List<CategoryNode> createCategoryList(int numOfCategories) {
		List<CategoryNode> categories = new ArrayList<CategoryNode>();
		for(int i = 0; i < numOfCategories; i++){
			categories.add(createCategory(CATEGORY_TYPES[rand.nextInt(CATEGORY_TYPES.length)], rand.nextInt(MAX_PARTITIONS) + 1));
		}
		return categories;
	}

	private CategoryNode createCategory(String type, int numOfPartitions) {
		CategoryNode category = new CategoryNode(randomName(), type);
		for(int i = 0; i < numOfPartitions; i++){
			category.addPartition(createPartition(type));
		}
		return category;
	}

	private List<ExpectedValueCategoryNode> createExpectedCategoriesList(int numOfExpCategories) {
		List<ExpectedValueCategoryNode> categories = new ArrayList<ExpectedValueCategoryNode>();
		for(int i = 0; i < numOfExpCategories; i++){
			categories.add(createExpectedValueCategory(CATEGORY_TYPES[rand.nextInt(CATEGORY_TYPES.length)]));
		}
		return categories;
	}

	private ExpectedValueCategoryNode createExpectedValueCategory(String type) {
		Object defaultValue = createRandomValue(type);
		return new ExpectedValueCategoryNode(randomName(), type, defaultValue);
	}

	private Object createRandomValue(String type) {
		switch(type){
		case Constants.TYPE_NAME_BOOLEAN:
			return rand.nextBoolean();
		case Constants.TYPE_NAME_BYTE:
			return (byte)rand.nextInt();
		case Constants.TYPE_NAME_CHAR:
			return (char)rand.nextInt(255);
		case Constants.TYPE_NAME_DOUBLE:
			return rand.nextDouble();
		case Constants.TYPE_NAME_FLOAT:
			return rand.nextFloat();
		case Constants.TYPE_NAME_INT:
			return rand.nextInt();
		case Constants.TYPE_NAME_LONG:
			return rand.nextLong();
		case Constants.TYPE_NAME_SHORT:
			return (short)rand.nextInt();
		case Constants.TYPE_NAME_STRING:
			if(rand.nextInt(5) == 0){
				return null;
			}
			else{
				return RandomStringUtils.random(rand.nextInt(10), true,	true);
			}
		default:
			fail("Unexpected category type");
			return null;
		}
	}

	private PartitionNode createPartition(String type) {
		Object value = createRandomValue(type);
		return new PartitionNode(randomName(), value);
	}


	private List<ConstraintNode> createConstraints(
			List<? extends CategoryNode> categories, int numOfConstraints) {
		List<ConstraintNode> constraints = new ArrayList<ConstraintNode>();
		for(int i = 0; i < numOfConstraints; ++i){
			constraints.add(new ConstraintNode(randomName(), createConstraint(categories)));
		}
		return constraints;
	}

	private Constraint createConstraint(List<? extends CategoryNode> categories) {
		BasicStatement premise = createBasicStatement(categories);
		BasicStatement consequence = createBasicStatement(categories);
		return new Constraint(premise, consequence);
	}

	private BasicStatement createBasicStatement(List<? extends CategoryNode> categories) {
		switch(rand.nextInt(3)){
		case 0: return new StaticStatement(rand.nextBoolean());
		case 1: if(getNotExpectedCategories(categories).size() > 0){
			return createStatement(categories);
		}
		case 2: return createStatementArray(rand.nextInt(3), categories);
		}
		return null;
	}

	private BasicStatement createStatement(List<? extends CategoryNode> categories) {
		List<CategoryNode> basicCategories = getNotExpectedCategories(categories);
		if(basicCategories.size() == 0){
			fail("basicCategories.size() == 0");
		}
		CategoryNode category = basicCategories.get(rand.nextInt(basicCategories.size()));
		PartitionNode partition = category.getPartitions().get(rand.nextInt(category.getPartitions().size()));
		Relation relation;
		switch(rand.nextInt(6)){
		case 0: relation = Relation.EQUAL;
		case 1: relation = Relation.LESS;
		case 2: relation = Relation.LESS_EQUAL;
		case 3: relation = Relation.NOT;
		case 4: relation = Relation.GREATER;
		case 5: relation = Relation.GREATER_EQUAL;
		default: relation = Relation.EQUAL;
		}
		return new Statement(partition, relation);
	}

	private List<CategoryNode> getNotExpectedCategories(List<? extends CategoryNode> categories) {
		List<CategoryNode> result = new ArrayList<CategoryNode>();
		for(CategoryNode category : categories){
			if(category instanceof ExpectedValueCategoryNode == false){
				result.add(category);
			}
		}
		return result;
	}

	private BasicStatement createStatementArray(int levels, List<? extends CategoryNode> categories) {
		StatementArray array = new StatementArray(rand.nextBoolean()?Operator.AND:Operator.OR);
		for(int i = 0; i < rand.nextInt(3) + 1; ++i){
			if(levels > 0){
				array.addStatement(createStatementArray(levels - 1, categories));
			}
			else{
				if(rand.nextBoolean() && getNotExpectedCategories(categories).size() > 0){
					array.addStatement(createStatement(categories));
				}
				else{
					array.addStatement(new StaticStatement(rand.nextBoolean()));
				}
			}
		}
		return array;
	}

	private List<TestCaseNode> createTestCases(
			List<? extends CategoryNode> categories, int numOfTestCases) {
		List<TestCaseNode> result = new ArrayList<TestCaseNode>();
		RandomGenerator<PartitionNode> generator = new RandomGenerator<PartitionNode>();
		List<? extends List<PartitionNode>> input = getGeneratorInput(categories);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("Test suite size", numOfTestCases);
		parameters.put("Duplicates", true);
		
		try {
			generator.initialize(input, null, parameters);
			List<PartitionNode> next;
			while((next = generator.next()) != null){
				result.add(new TestCaseNode(randomName(), next));
			}
		} catch (GeneratorException e) {
			fail("Unexpected generator exception");
		}
		return result;
	}

	private List<? extends List<PartitionNode>> getGeneratorInput(
			List<? extends CategoryNode> categories) {
		List<List<PartitionNode>> result = new ArrayList<List<PartitionNode>>();
		for(CategoryNode category : categories){
			result.add(category.getPartitions());
		}
		return result;
	}

	protected String randomName(){
		return "name" + nextInt++;
	}

	private void compareModels(RootNode model1, RootNode model2) {
		compareNames(model1.getName(), model2.getName());
		compareSizes(model1.getClasses(), model2.getClasses());
		for(int i = 0; i < model1.getClasses().size(); ++i){
			compareClasses(model1.getClasses().get(i), model2.getClasses().get(i));
		}
	}

	private void compareClasses(ClassNode classNode1, ClassNode classNode2) {
		compareNames(classNode1.getName(), classNode2.getName());
		compareSizes(classNode1.getMethods(), classNode2.getMethods());
		
		for(int i = 0; i < classNode1.getMethods().size(); ++i){
			compareMethods(classNode1.getMethods().get(i), classNode2.getMethods().get(i));
		}
	}

	private void compareMethods(MethodNode method1, MethodNode method2) {
		compareNames(method1.getName(), method2.getName());
		compareSizes(method1.getCategories(), method2.getCategories());
		compareSizes(method1.getConstraintNodes(), method2.getConstraintNodes());
		compareSizes(method1.getTestCases(), method2.getTestCases());
		
		for(int i =0; i < method1.getCategories().size(); ++i){
			compareCategories(method1.getCategories().get(i), method2.getCategories().get(i));
		}
		for(int i =0; i < method1.getConstraintNodes().size(); ++i){
			compareConstraintNodes(method1.getConstraintNodes().get(i), method2.getConstraintNodes().get(i));
		}
		for(int i =0; i < method1.getTestCases().size(); ++i){
			compareTestCases(method1.getTestCases().get(i), method2.getTestCases().get(i));
		}
	}

	private void compareCategories(CategoryNode category1, CategoryNode category2) {
		compareNames(category1.getName(), category2.getName());
		compareNames(category1.getType(), category2.getType());
		compareSizes(category1.getPartitions(), category2.getPartitions());
		if(category1 instanceof ExpectedValueCategoryNode || category2 instanceof ExpectedValueCategoryNode){
			if((category1 instanceof ExpectedValueCategoryNode && category2 instanceof ExpectedValueCategoryNode) == false){
				fail("Either both categories must be expected value or none");
			}
		}
		for(int i = 0; i < category1.getPartitions().size(); ++i){
			comparePartitions(category1.getPartitions().get(i), category2.getPartitions().get(i));
		}
	}

	private void comparePartitions(PartitionNode partition1, PartitionNode partition2) {
		compareNames(partition1.getName(), partition2.getName());
		if(partition1.getValue().equals(partition2.getValue()) == false){
			fail("Partition values differ");
		}
	}

	private void compareConstraintNodes(ConstraintNode constraint1, ConstraintNode constraint2) {
		compareNames(constraint1.getName(), constraint2.getName());
		compareConstraints(constraint1.getConstraint(), constraint2.getConstraint());
	}


	private void compareConstraints(Constraint constraint1, Constraint constraint2) {
		compareBasicStatements(constraint1.getPremise(), constraint2.getPremise());
		compareBasicStatements(constraint1.getConsequence(), constraint2.getConsequence());
	}

	private void compareBasicStatements(BasicStatement statement1, BasicStatement statement2) {
		if(statement1 instanceof StaticStatement && statement2 instanceof StaticStatement){
			compareStaticStatements((StaticStatement)statement1, (StaticStatement)statement2);
		}
		else if(statement1 instanceof Statement && statement2 instanceof Statement){
			compareStatements((Statement)statement1, (Statement)statement2);
		}
		else if(statement1 instanceof StatementArray && statement2 instanceof StatementArray){
			compareStatementArrays((StatementArray)statement1, (StatementArray)statement2);
		}
		else{
			fail("Unknown type of statement or compared statements are of didderent types");
		}
	}

	private void compareStatementArrays(StatementArray array1, StatementArray array2) {
		if(array1.getOperator() != array2.getOperator()){
			fail("Operator of compared statement arrays differ");
		}
		compareSizes(array1.getChildren(), array2.getChildren());
		for(int i = 0; i < array1.getChildren().size(); ++i){
			compareBasicStatements(array1.getChildren().get(i), array2.getChildren().get(i));
		}
	}

	private void compareStatements(Statement statement1, Statement statement2) {
		comparePartitions(statement1.getCondition(), statement2.getCondition());
		if(statement1.getRelation() != statement2.getRelation()){
			fail("Relations in compared statements differ");
		}
	}

	private void compareStaticStatements(StaticStatement statement1, StaticStatement statement2) {
		if(statement1.getValue() != statement2.getValue()){
			fail("Static statements different");
		}
	}

	private void compareTestCases(TestCaseNode testCase1, TestCaseNode testCase2) {
		compareNames(testCase1.getName(), testCase2.getName());
		compareSizes(testCase1.getTestData(), testCase2.getTestData());
		for(int i = 0; i < testCase1.getTestData().size(); i++){
			comparePartitions(testCase1.getTestData().get(i),testCase2.getTestData().get(i));
		}
	}

	private void compareSizes(Collection<? extends Object> collection1, Collection<? extends Object> collection2) {
		if(collection1.size() != collection2.size()){
			fail("Different sizes of collections");
		}
	}

	private void compareNames(String name, String name2) {
		if(name.equals(name2) == false){
			fail("Different names: " + name + ", " + name2);
		}
	}
}
