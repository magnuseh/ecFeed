package com.testify.ecfeed.parsers.xml.xom;

import static com.testify.ecfeed.parsers.Constants.CATEGORY_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CLASS_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_CONSEQUENCE_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_EXPECTED_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_LABEL_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_PARTITION_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_PREMISE_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_STATEMENT_ARRAY_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.CONSTRAINT_STATIC_STATEMENT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.DEFAULT_EXPECTED_VALUE_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.EXPECTED_PARAMETER_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.EXPECTED_VALUE_CATEGORY_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.EXPECTED_VALUE_PARTITION_NAME;
import static com.testify.ecfeed.parsers.Constants.LABEL_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.LABEL_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.METHOD_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.NODE_NAME_ATTRIBUTE;
import static com.testify.ecfeed.parsers.Constants.NULL_VALUE_STRING_REPRESENTATION;
import static com.testify.ecfeed.parsers.Constants.PARTITION_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.PARTITION_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.RELATION_EQUAL;
import static com.testify.ecfeed.parsers.Constants.RELATION_NOT;
import static com.testify.ecfeed.parsers.Constants.RELATION_NOT_ASCII;
import static com.testify.ecfeed.parsers.Constants.ROOT_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_CATEGORY_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_EXPECTED_VALUE_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_LABEL_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_OPERATOR_AND_ATTRIBUTE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_OPERATOR_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_OPERATOR_OR_ATTRIBUTE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_PARTITION_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATEMENT_RELATION_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.STATIC_STATEMENT_FALSE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATIC_STATEMENT_TRUE_VALUE;
import static com.testify.ecfeed.parsers.Constants.STATIC_VALUE_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.Constants.TEST_CASE_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.TEST_PARAMETER_NODE_NAME;
import static com.testify.ecfeed.parsers.Constants.TEST_SUITE_NAME_ATTRIBUTE;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_ATTRIBUTE;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_BOOLEAN;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_BYTE;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_CHAR;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_DOUBLE;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_FLOAT;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_INT;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_LONG;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_SHORT;
import static com.testify.ecfeed.parsers.Constants.TYPE_NAME_STRING;
import static com.testify.ecfeed.parsers.Constants.VALUE_ATTRIBUTE;
import static com.testify.ecfeed.parsers.Constants.VALUE_ATTRIBUTE_NAME;
import static com.testify.ecfeed.parsers.xml.Messages.ELEMENT_TYPE_MISMATCH;
import static com.testify.ecfeed.parsers.xml.Messages.IO_EXCEPTION;
import static com.testify.ecfeed.parsers.xml.Messages.MALFORMED_CONSTRAINT_NODE_DEFINITION;
import static com.testify.ecfeed.parsers.xml.Messages.MISSING_ATTRIBUTE;
import static com.testify.ecfeed.parsers.xml.Messages.PARSING_EXCEPTION;
import static com.testify.ecfeed.parsers.xml.Messages.PARTITION_DOES_NOT_EXIST;
import static com.testify.ecfeed.parsers.xml.Messages.UNSUPPORTED_STATEMENT_TYPE;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_CATEGORY_NAME;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_CHILD_ELEMENT_TYPE;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_OR_MISSING_RELATION_FORMAT;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_PARTITION_NAME;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_ROOT_ELEMENT_TAG;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_STATEMENT_ARRAY_OPERATOR;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_STATIC_STATEMENT_VALUE;
import static com.testify.ecfeed.parsers.xml.Messages.WRONG_TEST_PAREMETERS_NUMBER;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.Constraint;
import com.testify.ecfeed.model.constraint.ExpectedValueStatement;
import com.testify.ecfeed.model.constraint.Operator;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement;
import com.testify.ecfeed.model.constraint.Relation;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.model.constraint.StaticStatement;
import com.testify.ecfeed.parsers.ParserException;
import com.testify.ecfeed.utils.ClassUtils;

public class XomParser {


	public RootNode parseModel(InputStream istream) throws ParserException{
		try {
			Builder parser = new Builder();
			Document document = parser.build(istream);
			if(document.getRootElement().getLocalName() == ROOT_NODE_NAME){
				return parseRootElement(document.getRootElement());
			}
			else{
				throw new ParserException(WRONG_ROOT_ELEMENT_TAG);
			}
		} catch (ParsingException e) {
			throw new ParserException(PARSING_EXCEPTION(e));
		} catch (IOException e) {
			throw new ParserException(IO_EXCEPTION(e));
		}
	}

	public RootNode parseRootElement(Element element) throws ParserException {
		validateType(element, ROOT_NODE_NAME);
		
		String name = getElementName(element);
		RootNode rootNode = new RootNode(name);
		for(Element child : getIterableElements(element.getChildElements())){
			if(child.getLocalName() == CLASS_NODE_NAME){
				rootNode.addClass(parseClassElement(child));
			}
			else{
				throw new ParserException(WRONG_CHILD_ELEMENT_TYPE(element, child.getLocalName()));
			}
		}
		return rootNode;
	}

	public ClassNode parseClassElement(Element element) throws ParserException {
		validateType(element, CLASS_NODE_NAME);

		String qualifiedName = getElementName(element);

		ClassNode classNode = new ClassNode(qualifiedName);
		for(Element child : getIterableElements(element.getChildElements())){
			if(child.getLocalName() == METHOD_NODE_NAME){
				classNode.addMethod(parseMethodElement(child));
			}
			else{
				throw new ParserException(WRONG_CHILD_ELEMENT_TYPE(element, child.getLocalName()));
			}
		}
		return classNode;
	}

	public MethodNode parseMethodElement(Element element) throws ParserException {
		validateType(element, METHOD_NODE_NAME);

		String name = getElementName(element);
		MethodNode methodNode = new MethodNode(name);
		for(Element child : getIterableElements(element.getChildElements())){
			if(child.getLocalName() == EXPECTED_VALUE_CATEGORY_NODE_NAME){
				methodNode.addCategory(parseCategoryElement(child));
			}
			else if(child.getLocalName() == CATEGORY_NODE_NAME){
				methodNode.addCategory(parseCategoryElement(child));
			}
			else if(child.getLocalName() == TEST_CASE_NODE_NAME){
				methodNode.addTestCase(parseTestCaseElement(child, methodNode.getCategories()));
			}
			else if(child.getLocalName() == CONSTRAINT_NODE_NAME){
				methodNode.addConstraint(parseConstraintElement(child, methodNode));
			}
			else{
				throw new ParserException(WRONG_CHILD_ELEMENT_TYPE(element, child.getLocalName()));
			}
		}
		return methodNode;
	}

	public ConstraintNode parseConstraintElement(Element element, MethodNode method) throws ParserException {
		validateType(element, CONSTRAINT_NODE_NAME);

		String name = getElementName(element);
		BasicStatement premise = null;
		BasicStatement consequence = null;
		for(Element child : getIterableElements(element.getChildElements())){
			if(child.getLocalName().equals(CONSTRAINT_PREMISE_NODE_NAME)){
				if(getIterableElements(child.getChildElements()).size() == 1){
					//there is only one statement per premise or consequence that is either
					//a single statement or statement array
					premise = parseStatement(child.getChildElements().get(0), method);
				}
				else{
					throw new ParserException(MALFORMED_CONSTRAINT_NODE_DEFINITION(method.getName(), name));
				}
			}
			else if(child.getLocalName().equals(CONSTRAINT_CONSEQUENCE_NODE_NAME)){
				if(getIterableElements(child.getChildElements()).size() == 1){
					//					if(child.getChildCount() == 0){
					Element childElement = child.getChildElements().get(0);
					consequence = parseStatement(childElement, method);
				}
				else{
					throw new ParserException(MALFORMED_CONSTRAINT_NODE_DEFINITION(method.getName(), name));
				}
			}
			else{
				throw new ParserException(MALFORMED_CONSTRAINT_NODE_DEFINITION(method.getName(), name));
			}
		}
		if(premise == null || consequence == null){
			throw new ParserException(MALFORMED_CONSTRAINT_NODE_DEFINITION(method.getName(), name));
		}
		return new ConstraintNode(name, new Constraint(premise, consequence));
	}

	public BasicStatement parseStatement(Element element, MethodNode method) throws ParserException {
		switch(element.getLocalName()){
		case CONSTRAINT_PARTITION_STATEMENT_NODE_NAME:
			return parsePartitionStatement(element, method);
		case CONSTRAINT_LABEL_STATEMENT_NODE_NAME:
			return parseLabelStatement(element, method);
		case CONSTRAINT_STATEMENT_ARRAY_NODE_NAME:
			return parseStatementArray(element, method);
		case CONSTRAINT_STATIC_STATEMENT_NODE_NAME:
			return parseStaticStatement(element);
		case CONSTRAINT_EXPECTED_STATEMENT_NODE_NAME:
			return parseExpectedValueStatement(element, method);
		default: throw new ParserException(UNSUPPORTED_STATEMENT_TYPE(element.getLocalName()));
		}
	}

	public BasicStatement parseStatementArray(Element element, MethodNode method) throws ParserException {
		validateType(element, CONSTRAINT_STATEMENT_ARRAY_NODE_NAME);

		StatementArray statementArray = null;
		String operatorValue = getAttributeValue(element, STATEMENT_OPERATOR_ATTRIBUTE_NAME);
		switch(operatorValue){
		case STATEMENT_OPERATOR_OR_ATTRIBUTE_VALUE:
			statementArray = new StatementArray(Operator.OR);
			break;
		case STATEMENT_OPERATOR_AND_ATTRIBUTE_VALUE:
			statementArray = new StatementArray(Operator.AND);
			break;
		default: 
			throw new ParserException(WRONG_STATEMENT_ARRAY_OPERATOR(method.getName(), operatorValue));
		}
		for(Element child : getIterableElements(element.getChildElements())){
			BasicStatement childStatement = parseStatement(child, method);
			if(childStatement != null){
				statementArray.addStatement(childStatement);
			}
		}
		return statementArray;
	}

	public BasicStatement parseStaticStatement(Element element) throws ParserException {
		validateType(element, CONSTRAINT_STATIC_STATEMENT_NODE_NAME);
		
		String valueString = getAttributeValue(element, STATIC_VALUE_ATTRIBUTE_NAME);
		switch(valueString){
		case STATIC_STATEMENT_TRUE_VALUE:
			return new StaticStatement(true);
		case STATIC_STATEMENT_FALSE_VALUE:
			return new StaticStatement(false);
		default:
			throw new ParserException(WRONG_STATIC_STATEMENT_VALUE(valueString));
		}
	}

	public BasicStatement parsePartitionStatement(Element element, MethodNode method) throws ParserException {
		validateType(element, CONSTRAINT_PARTITION_STATEMENT_NODE_NAME);

		String categoryName = getAttributeValue(element, STATEMENT_CATEGORY_ATTRIBUTE_NAME);
		CategoryNode category = method.getPartitionedCategory(categoryName);
		if(category == null){
			throw new ParserException(WRONG_CATEGORY_NAME(categoryName, method.getName()));
		}
		String partitionName = getAttributeValue(element, STATEMENT_PARTITION_ATTRIBUTE_NAME);
		PartitionNode partition = category.getPartition(partitionName);
		if(partition == null){
			throw new ParserException(WRONG_PARTITION_NAME(categoryName, method.getName()));
		}

		String relationName = getAttributeValue(element, STATEMENT_RELATION_ATTRIBUTE_NAME);
		Relation relation = getRalation(relationName);

		return new PartitionedCategoryStatement(category, relation, partition); 
	}

	public BasicStatement parseLabelStatement(Element element, MethodNode method) throws ParserException {
		validateType(element, CONSTRAINT_LABEL_STATEMENT_NODE_NAME);

		String categoryName = getAttributeValue(element, STATEMENT_CATEGORY_ATTRIBUTE_NAME);
		String label = getAttributeValue(element, STATEMENT_LABEL_ATTRIBUTE_NAME);
		String relationName = getAttributeValue(element, STATEMENT_RELATION_ATTRIBUTE_NAME);

		CategoryNode category = method.getPartitionedCategory(categoryName);
		if(category == null){
			throw new ParserException(WRONG_CATEGORY_NAME(categoryName, method.getName()));
		}
		Relation relation = getRalation(relationName);

		return new PartitionedCategoryStatement(category, relation, label);
	}

	public BasicStatement parseExpectedValueStatement(Element element, MethodNode method) throws ParserException {
		validateType(element, CONSTRAINT_EXPECTED_STATEMENT_NODE_NAME);

		String categoryName = getAttributeValue(element, STATEMENT_CATEGORY_ATTRIBUTE_NAME);
		String valueString = getAttributeValue(element, STATEMENT_EXPECTED_VALUE_ATTRIBUTE_NAME);
		CategoryNode category = method.getExpectedCategory(categoryName);
		if(category == null){
			throw new ParserException(WRONG_CATEGORY_NAME(categoryName, method.getName()));
		}
//		Object conditionValue = parseValue(valueString, category.getType());
		PartitionNode condition = new PartitionNode("expected", valueString);
		condition.setParent(category);

		return new ExpectedValueStatement(category, condition);
	}

	protected Relation getRalation(String relationName) throws ParserException{
		Relation relation = null;
		switch(relationName){
		case RELATION_EQUAL:
			relation = Relation.EQUAL;
			break;
		case RELATION_NOT:
		case RELATION_NOT_ASCII:
			relation = Relation.NOT;
			break;
		default:
			throw new ParserException(WRONG_OR_MISSING_RELATION_FORMAT(relationName));
		}
		return relation;
	}

	public TestCaseNode parseTestCaseElement(Element element, List<CategoryNode> categories) throws ParserException {
		validateType(element, TEST_CASE_NODE_NAME);
		
		String testSuiteName = getAttributeValue(element, TEST_SUITE_NAME_ATTRIBUTE);
		ArrayList<PartitionNode> testData = new ArrayList<PartitionNode>();
		List<Element> parameterElements = getIterableElements(element.getChildElements());

		if(categories.size() != parameterElements.size()){
			throw new ParserException(WRONG_TEST_PAREMETERS_NUMBER(testSuiteName));
		}

		for(int i = 0; i < parameterElements.size(); i++){
			Element testParameterElement = parameterElements.get(i);
			CategoryNode category = categories.get(i);

			PartitionNode testValue = null;
			if(testParameterElement.getLocalName().equals(TEST_PARAMETER_NODE_NAME)){
				String partitionName = getAttributeValue(testParameterElement, PARTITION_ATTRIBUTE_NAME);
				testValue = category.getPartition(partitionName);
				if(testValue == null){
					throw new ParserException(PARTITION_DOES_NOT_EXIST(category.getName(), partitionName));
				}
			}
			else if(testParameterElement.getLocalName().equals(EXPECTED_PARAMETER_NODE_NAME)){
				String valueString = getAttributeValue(testParameterElement, VALUE_ATTRIBUTE_NAME);
				testValue = new PartitionNode(EXPECTED_VALUE_PARTITION_NAME, valueString);
				testValue.setParent(category);
			}
			testData.add(testValue);
		}
		return new TestCaseNode(testSuiteName, testData);
	}

	public CategoryNode parseExpectedValueCategoryElement(Element element) throws ParserException {
		validateType(element, EXPECTED_VALUE_CATEGORY_NODE_NAME);

		String name = getElementName(element);
		String type = getAttributeValue(element, TYPE_NAME_ATTRIBUTE);
		String defaultValueString = getAttributeValue(element, DEFAULT_EXPECTED_VALUE_ATTRIBUTE_NAME);
//		Object defaultValue = parseValue(defaultValueString, type);
		return new CategoryNode(name, type, true);
	}

	public CategoryNode parseCategoryElement(Element element) throws ParserException {
		validateType(element, CATEGORY_NODE_NAME);

		String name = getElementName(element);
		String type = getAttributeValue(element, TYPE_NAME_ATTRIBUTE);

		CategoryNode categoryNode = new CategoryNode(name, type, false);
		for(Element child : getIterableElements(element.getChildElements())){
			if(child.getLocalName() == PARTITION_NODE_NAME){
				categoryNode.addPartition(parsePartitionElement(child));
			}
		}
		return categoryNode;
	}

	public PartitionNode parsePartitionElement(Element element) throws ParserException {
		validateType(element, PARTITION_NODE_NAME);

		String name = getElementName(element);
		String value = getAttributeValue(element, VALUE_ATTRIBUTE);
		PartitionNode partition = new PartitionNode(name, value);
		
		for(Element child : getIterableElements(element.getChildElements())){
			if(child.getLocalName() == PARTITION_NODE_NAME){
				partition.addPartition(parsePartitionElement(child));
			}
			if(child.getLocalName() == LABEL_NODE_NAME){
				partition.addLabel(child.getAttributeValue(LABEL_ATTRIBUTE_NAME));
			}
		}
		return partition;
	}

//	protected Object parseValue(String valueString, String type) {
//		switch(type){
//		case TYPE_NAME_BOOLEAN:
//			return Boolean.parseBoolean(valueString);
//		case TYPE_NAME_BYTE:
//			return Byte.parseByte(valueString);
//		case TYPE_NAME_CHAR:
//			if (valueString.length() <= 0){
//				return null;
//			}
//			int intValue = Integer.parseInt(valueString);
//			return (char)intValue;
//		case TYPE_NAME_DOUBLE:
//			return Double.parseDouble(valueString);
//		case TYPE_NAME_FLOAT:
//			return Float.parseFloat(valueString);
//		case TYPE_NAME_INT:
//			return Integer.parseInt(valueString);
//		case TYPE_NAME_LONG:
//			return Long.parseLong(valueString);
//		case TYPE_NAME_SHORT:
//			return Short.parseShort(valueString);
//		case TYPE_NAME_STRING:
//			return valueString.equals(NULL_VALUE_STRING_REPRESENTATION)?null:valueString;
//		default:
//			return ClassUtils.parseEnumValue(valueString, type, ClassUtils.getClassLoader(true, getClass().getClassLoader()));
//		}		
//	}
//
	protected List<Element> getIterableElements(Elements elements){
		ArrayList<Element> list = new ArrayList<Element>();
		for(int i = 0; i < elements.size(); i++){
			Node node = elements.get(i);
			if(node instanceof Element){
				list.add((Element)node);
			}
		}
		return list;
	}

	protected String getElementName(Element element) throws ParserException {
		String name = element.getAttributeValue(NODE_NAME_ATTRIBUTE);
		if(name == null){
			throw new ParserException(MISSING_ATTRIBUTE(element, NODE_NAME_ATTRIBUTE));
		}
		return name;
	}

	protected String getAttributeValue(Element element, String attributeName) throws ParserException{
		String value = element.getAttributeValue(attributeName);
		if(value == null){
			throw new ParserException(MISSING_ATTRIBUTE(element, attributeName));
		}
		return value;
	}

	private void validateType(Element node, String nodeName) throws ParserException {
		if(node.getLocalName().equals(nodeName) == false){
			throw new ParserException(ELEMENT_TYPE_MISMATCH(nodeName, node.getLocalName()));
		}
	}

}
