package com.testify.ecfeed.parsers.xml;

import static com.testify.ecfeed.parsers.Constants.*;

import nu.xom.Attribute;
import nu.xom.Element;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.IConverter;
import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.ExpectedValueStatement;
import com.testify.ecfeed.model.constraint.IStatementVisitor;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement.ICondition;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement.LabelCondition;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement.PartitionCondition;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.model.constraint.StaticStatement;

public class XomConverter implements IConverter, IStatementVisitor {

	@Override
	public Object convert(RootNode node) {
		Element element = createNamedElement(ROOT_NODE_NAME, node); 
				
		for(ClassNode _class : node.getClasses()){
			element.appendChild((Element)convert(_class));
		}
		
		return element;
	}

	@Override
	public Object convert(ClassNode node) {
		Element element = createNamedElement(CLASS_NODE_NAME, node);
		
		for(MethodNode method : node.getMethods()){
			element.appendChild((Element)convert(method));
		}
		return element;
	}

	@Override
	public Object convert(MethodNode node) {
		Element element = createNamedElement(METHOD_NODE_NAME, node);
		
		for(CategoryNode category : node.getCategories()){
			element.appendChild((Element)category.convert(this));
		}
		
		return element;
	}
	
	@Override
	public Object convert(CategoryNode node){
		Element element = createNamedElement(CATEGORY_NODE_NAME, node);
		element.addAttribute(new Attribute(TYPE_NAME_ATTRIBUTE, node.getType()));
		element.addAttribute(new Attribute(CATEGORY_IS_EXPECTED_ATTRIBUTE_NAME, Boolean.toString(node.isExpected())));
		element.addAttribute(new Attribute(DEFAULT_EXPECTED_VALUE_ATTRIBUTE_NAME, node.getDefaultValueString()));

		for(PartitionNode child : node.getPartitions()){
			element.appendChild((Element)child.convert(this));
		}
	
		return element;
	}
	
	@Override
	public Object convert(TestCaseNode node){
		Element element = new Element(TEST_CASE_NODE_NAME);
		element.addAttribute(new Attribute(TEST_SUITE_NAME_ATTRIBUTE, node.getName()));
		for(PartitionNode testParameter : node.getTestData()){
			if(testParameter.getCategory() != null && testParameter.getCategory().isExpected()){
				Element expectedParameterElement = new Element(EXPECTED_PARAMETER_NODE_NAME);
				Attribute expectedValueAttribute = new Attribute(VALUE_ATTRIBUTE_NAME, testParameter.getValueString());
				expectedParameterElement.addAttribute(expectedValueAttribute);
				element.appendChild(expectedParameterElement);
			}
			else{
				Element testParameterElement = new Element(TEST_PARAMETER_NODE_NAME);
				Attribute partitionNameAttribute = new Attribute(PARTITION_ATTRIBUTE_NAME, testParameter.getQualifiedName());
				testParameterElement.addAttribute(partitionNameAttribute);
				element.appendChild(testParameterElement);
			}
		}
	
		return element;
	}

	@Override
	public Object convert(ConstraintNode node){
		Element element = createNamedElement(PARTITION_NODE_NAME, node);
		BasicStatement premise = node.getConstraint().getPremise();
		BasicStatement consequence = node.getConstraint().getConsequence();
		
		
		Element premiseElement = new Element(CONSTRAINT_PREMISE_NODE_NAME);
		premiseElement.appendChild((Element)premise.accept(this));
		
		Element consequenceElement = new Element(CONSTRAINT_CONSEQUENCE_NODE_NAME);
		consequenceElement.appendChild((Element)consequence.accept(this));

		return element;
	}
	
	@Override
	public Object convert(PartitionNode node){
		Element element = createNamedElement(PARTITION_NODE_NAME, node);
		String value = node.getValueString();
		//remove disallowed XML characters
		String xml10pattern = "[^"
                + "\u0009\r\n"
                + "\u0020-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]";
		String legalValue = value.replaceAll(xml10pattern, "");

		element.addAttribute(new Attribute(VALUE_ATTRIBUTE, legalValue));
		
		for(String label : node.getLabels()){
			Element labelElement = new Element(LABEL_NODE_NAME);
			labelElement.addAttribute(new Attribute(LABEL_ATTRIBUTE_NAME, label));
			element.appendChild(labelElement);
		}
		
		for(PartitionNode child : node.getPartitions()){
			element.appendChild((Element)child.convert(this));
		}
		
		return element;
	}

	@Override
	public Object visit(StaticStatement statement) {
		Element statementElement = new Element(CONSTRAINT_STATIC_STATEMENT_NODE_NAME);
		String attrName = STATEMENT_STATIC_VALUE_ATTRIBUTE_NAME;
		String attrValue = statement.getValue()?STATIC_STATEMENT_TRUE_VALUE:
							STATIC_STATEMENT_FALSE_VALUE;
		statementElement.addAttribute(new Attribute(attrName, attrValue));
		
		return statementElement;
	}

	@Override
	public Object visit(StatementArray statement) {
		Element element = new Element(CONSTRAINT_STATEMENT_ARRAY_NODE_NAME);
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
		element.addAttribute(operatorAttribute);
		
		for(BasicStatement child : statement.getChildren()){
			element.appendChild((Element)child.accept(this));
		}
		return element;
	}

	@Override
	public Object visit(ExpectedValueStatement statement) {
		String categoryName = statement.getLeftHandName();
		PartitionNode condition = statement.getCondition();
		Attribute categoryAttribute = 
				new Attribute(STATEMENT_CATEGORY_ATTRIBUTE_NAME, categoryName);
		Attribute valueAttribute = 
				new Attribute(STATEMENT_EXPECTED_VALUE_ATTRIBUTE_NAME, condition.getExactValueString());
		
		Element statementElement = new Element(CONSTRAINT_EXPECTED_STATEMENT_NODE_NAME);
		statementElement.addAttribute(categoryAttribute);
		statementElement.addAttribute(valueAttribute);

		return statementElement;
	}

	@Override
	public Object visit(PartitionedCategoryStatement statement) {

		String categoryName = statement.getCategory().getName();
		Attribute categoryAttribute = 
				new Attribute(STATEMENT_CATEGORY_ATTRIBUTE_NAME, categoryName);
		Attribute relationAttribute = 
				new Attribute(STATEMENT_RELATION_ATTRIBUTE_NAME, statement.getRelation().toString());
		ICondition condition = statement.getCondition();
		Element statementElement = (Element)condition.accept(this);
		
		statementElement.addAttribute(categoryAttribute);
		statementElement.addAttribute(relationAttribute);
		
		return statementElement;
	}

	@Override
	public Object visit(LabelCondition condition) {
			Element element = new Element(CONSTRAINT_LABEL_STATEMENT_NODE_NAME);
			element.addAttribute(new Attribute(STATEMENT_LABEL_ATTRIBUTE_NAME, condition.getLabel()));
			return element;
	}

	@Override
	public Object visit(PartitionCondition condition) {
		PartitionNode partition = condition.getPartition();
		Element element = new Element(CONSTRAINT_PARTITION_STATEMENT_NODE_NAME);
		element.addAttribute(new Attribute(STATEMENT_PARTITION_ATTRIBUTE_NAME, partition.getQualifiedName()));
		
		return element;
	}

	private Element createNamedElement(String nodeTag, IGenericNode node){
		Element element = new Element(nodeTag);
		Attribute nameAttr = new Attribute(NODE_NAME_ATTRIBUTE, node.getName());
		element.addAttribute(nameAttr);
		return element;
	}
	
}