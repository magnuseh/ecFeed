package com.testify.ecfeed.parsers.xml.xom;

import static com.testify.ecfeed.parsers.Constants.CATEGORY_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CLASS_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_EXPECTED_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_LABEL_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_PARTITION_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_STATEMENT_ARRAY_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_STATIC_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.EXPECTED_VALUE_CATEGORY_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.METHOD_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.NODE_NAME_ATTRIBUTE;
import static com.testify.ecfeed.parsers.Constants.PARTITION_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.ROOT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_CATEGORY_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_EXPECTED_VALUE_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_LABEL_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_OPERATOR_AND_ATTRIBUTE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_OPERATOR_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_OPERATOR_OR_ATTRIBUTE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_PARTITION_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_RELATION_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_STATIC_VALUE_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATIC_STATEMENT_FALSE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATIC_STATEMENT_TRUE_VALUE;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_UNSUPPORTED;
import nu.xom.Attribute;
import nu.xom.Element;

import com.testify.ecfeed.model.AbstractCategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.ExpectedCategoryNode;
import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.IModelConverter;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.PartitionedCategoryNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.ExpectedValueStatement;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.model.constraint.StaticStatement;
import com.testify.ecfeed.parsers.Constants;

public class XomConverter implements IModelConverter{

	public Object convert(IGenericNode node) {
		return null;
	}

	public Object convert(RootNode node) {
		Element rootElement = createNamedElement(ROOT_NODE_NAME, node.getName());
		for(ClassNode classNode : node.getClasses()){
			rootElement.appendChild((Element)classNode.convert(this));
		}
		return rootElement;
	}

	public Object convert(ClassNode node) {
		Element classElement = createNamedElement(CLASS_NODE_NAME, node.getQualifiedName());
		for(MethodNode method : node.getMethods()){
			classElement.appendChild((Element)method.convert(this));
		}
		return classElement;
	}

	public Object convert(MethodNode node) {
		Element methodElement = createNamedElement(METHOD_NODE_NAME, node.getName());
		for(AbstractCategoryNode category : node.getCategories()){
			methodElement.appendChild((Element)category.convert(this));
		}
		for(ConstraintNode constraint : node.getConstraintNodes()){
			methodElement.appendChild((Element)constraint.convert(this));
		}
		for(TestCaseNode testCase : node.getTestCases()){
			methodElement.appendChild((Element)testCase.convert(this));
		}
		return methodElement;
	}

	public Object convert(TestCaseNode node) {
		Element testCaseElement = new Element(Constants.TEST_CASE_NODE_NAME);

		Attribute testSuiteNameAttribute = new Attribute(Constants.TEST_SUITE_NAME_ATTRIBUTE, node.getName());
		testCaseElement.addAttribute(testSuiteNameAttribute);
		
		for(PartitionNode parameter : node.getTestData()){
			testCaseElement.appendChild(createTestDataElement(parameter));
		}
		
		return testCaseElement;
	}

	private Element createTestDataElement(PartitionNode parameter) {
		if(parameter.getCategory() instanceof ExpectedCategoryNode){
			return createExpectedValueElement(parameter);
		}
		return createTestParameterElement(parameter);
	}

	private Element createTestParameterElement(PartitionNode parameter) {
		Element testParameterElement = new Element(Constants.TEST_PARAMETER_NODE_NAME);
		Attribute partitionNameAttribute = new Attribute(Constants.PARTITION_ATTRIBUTE_NAME, parameter.getQualifiedName());
		testParameterElement.addAttribute(partitionNameAttribute);
		
		return testParameterElement;
	}

	private Element createExpectedValueElement(PartitionNode parameter) {
		Element testParameterElement = new Element(Constants.EXPECTED_PARAMETER_NODE_NAME);
		String valueString = getValueString(parameter.getCategory().getType(), parameter.getValue());
		Attribute partitionNameAttribute = new Attribute(Constants.VALUE_ATTRIBUTE_NAME, valueString);
		testParameterElement.addAttribute(partitionNameAttribute);

		return testParameterElement;
	}

	public Object convert(ConstraintNode node) {
		Element constraintElement = createNamedElement(CONSTRAINT_NODE_NAME, node.getName());
		BasicStatement premise = node.getConstraint().getPremise();
		BasicStatement consequence = node.getConstraint().getConsequence();
		
		Element premiseElement = new Element(Constants.CONSTRAINT_PREMISE_NODE_NAME);
		premiseElement.appendChild((Element)premise.convert(this));
		
		Element consequenceElement = new Element(Constants.CONSTRAINT_CONSEQUENCE_NODE_NAME);
		consequenceElement.appendChild((Element)consequence.convert(this));

		constraintElement.appendChild(premiseElement);
		constraintElement.appendChild(consequenceElement);

		return constraintElement;
	}

	public Object convert(PartitionedCategoryNode node) {
		Element categoryElement = createNamedElement(CATEGORY_NODE_NAME, node.getName());
		
		Attribute typeNameAttribute = new Attribute(Constants.TYPE_NAME_ATTRIBUTE, node.getType());
		categoryElement.addAttribute(typeNameAttribute);

		for(PartitionNode partition : node.getPartitions()){
			categoryElement.appendChild((Element)partition.convert(this));
		}
		return categoryElement;
	}

	public Object convert(ExpectedCategoryNode node) {
		Element categoryElement = createNamedElement(EXPECTED_VALUE_CATEGORY_NODE_NAME, node.getName());

		String type = node.getType();
		Object value = node.getDefaultValue();
		
		Attribute typeNameAttribute = new Attribute(Constants.TYPE_NAME_ATTRIBUTE, node.getType());
		Attribute expectedAttribute = new Attribute(Constants.DEFAULT_EXPECTED_VALUE_ATTRIBUTE, getValueString(type, value));
		categoryElement.addAttribute(typeNameAttribute);
		categoryElement.addAttribute(expectedAttribute);

		return categoryElement;
	}

	public Object convert(PartitionNode node) {
		Element partitionElement = createNamedElement(PARTITION_NODE_NAME, node.getName());

		String type = TYPE_NAME_UNSUPPORTED;
		if(node.getCategory() != null){
			type = node.getCategory().getType();
		}
		String valueString = getValueString(type, node.getValue());
	
		Attribute valueAttribute = new Attribute(Constants.VALUE_ATTRIBUTE, valueString);
		partitionElement.addAttribute(valueAttribute);

		for(String label : node.getLabels()){
			Element labelElement = new Element(Constants.LABEL_NODE_NAME);
			labelElement.addAttribute(new Attribute(Constants.LABEL_ATTRIBUTE_NAME, label));
			partitionElement.appendChild(labelElement);
		}
		
		for(PartitionNode partition : node.getPartitions()){
			partitionElement.appendChild((Element)partition.convert(this));
		}
		
		return partitionElement;
}

	public Object convert(BasicStatement statement) {
		return null;
	}

	public Object convert(StatementArray statement) {
		Element statementArrayElement = new Element(CONSTRAINT_STATEMENT_ARRAY_NODE_NAME);
		Attribute operatorAttribute = null;
		switch(statement.getOperator()){
		case AND:
			operatorAttribute = new Attribute(STATEMENT_OPERATOR_ATTRIBUTE_NAME, 
					STATEMENT_OPERATOR_AND_ATTRIBUTE_VALUE);
			break;
		case OR:
			operatorAttribute = new Attribute(STATEMENT_OPERATOR_ATTRIBUTE_NAME, 
					STATEMENT_OPERATOR_OR_ATTRIBUTE_VALUE);
			break;
		}
		statementArrayElement.addAttribute(operatorAttribute);
		for(BasicStatement child : statement.getChildren()){
			statementArrayElement.appendChild((Element)child.convert(this));
		}
		
		return statementArrayElement;
	}

	public Object convert(StaticStatement statement) {

		Element statementElement = new Element(CONSTRAINT_STATIC_STATEMENT_NODE_NAME);
		String attrName = STATEMENT_STATIC_VALUE_ATTRIBUTE_NAME;
		String attrValue = statement.getValue() ? STATIC_STATEMENT_TRUE_VALUE : STATIC_STATEMENT_FALSE_VALUE;
		statementElement.addAttribute(new Attribute(attrName, attrValue));

		return statementElement;
	}

	public Object convert(PartitionedCategoryStatement statement) {
		String categoryName = statement.getCategory().getName();
		Attribute categoryAttribute = new Attribute(STATEMENT_CATEGORY_ATTRIBUTE_NAME, categoryName);
		Attribute relationAttribute = new Attribute(STATEMENT_RELATION_ATTRIBUTE_NAME, statement.getRelation().toString());
		
		Object condition = statement.getConditionValue();
		
		Element statementElement = null;
		if(condition instanceof String){
			String label = (String)condition;
			statementElement = new Element(CONSTRAINT_LABEL_STATEMENT_NODE_NAME);
			statementElement.addAttribute(new Attribute(STATEMENT_LABEL_ATTRIBUTE_NAME, label));
		}
		else if(condition instanceof PartitionNode){
			PartitionNode partition = (PartitionNode)condition;
			statementElement = new Element(CONSTRAINT_PARTITION_STATEMENT_NODE_NAME);
			statementElement.addAttribute(new Attribute(STATEMENT_PARTITION_ATTRIBUTE_NAME, partition.getQualifiedName()));
		}
//TODO implement exceptions for unsupported types			
//		else{
//			throw new ParserException("Unknown statement condition type");
//		}
		statementElement.addAttribute(categoryAttribute);
		statementElement.addAttribute(relationAttribute);
		
		return statementElement;
	}

	public Object convert(ExpectedValueStatement statement) {
		ExpectedCategoryNode category = statement.getCategory();
		PartitionNode condition = statement.getCondition();
		Element statementElement = new Element(CONSTRAINT_EXPECTED_STATEMENT_NODE_NAME);

		Attribute categoryAttribute = new Attribute(STATEMENT_CATEGORY_ATTRIBUTE_NAME, category.getName());
		Attribute valueAttribute = new Attribute(STATEMENT_EXPECTED_VALUE_ATTRIBUTE_NAME, 
						getValueString(category.getType(), condition.getValue()));
		
		statementElement.addAttribute(categoryAttribute);
		statementElement.addAttribute(valueAttribute);

		return statementElement;
	}
	
	protected Element createNamedElement(String type, String name){
		Element element = new Element(type);
		element.addAttribute(new Attribute(NODE_NAME_ATTRIBUTE, name));
		return element;
	}
	
	protected String getValueString(String type, Object value) {
		String valueString;
		switch(type){
		case Constants.TYPE_NAME_STRING:
			if(value == null){
				valueString = Constants.NULL_VALUE_STRING_REPRESENTATION;
			}
			else{
				valueString = String.valueOf(value);
			}
			break;
		case Constants.TYPE_NAME_CHAR:
			Character character = (Character)value;
			int representation = (int)character;
			valueString = String.valueOf(representation);
			break;
		default:
			if (value.getClass().isEnum()) {
				valueString = ((Enum<?>) value).name();
			} else {
				valueString = value.toString();	
			}
			break;
		}
		return valueString;
	}

}