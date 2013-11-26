package com.testify.ecfeed.parser.xml;

import java.io.IOException;

import com.testify.ecfeed.parser.Constants;

import nu.xom.Element;
import nu.xom.ParsingException;

public class Messages {
	
	public static final String WRONG_ROOT_ELEMENT_TAG = 
			"Parsing error: The XML root element must be " + Constants.ROOT_NODE_NAME;

	public static final String PARSING_EXCEPTION(ParsingException e) {
		return "Parsing exception in line " + e.getLineNumber() + ": " + e.getMessage();
	}

	public static String IO_EXCEPTION(IOException e) {
		return "IO exception: " + e.getMessage();
	}

	public static String MISSING_ATTRIBUTE(Element element, String attribute) {
		return "Tag " + element.getLocalName() + " does not contain expected " + attribute + " attribute";
	}

	public static String WRONG_CHILD_ELEMENT_TYPE(Element element, String childLocalName) {
		return "Tag " + element.getLocalName() + " mustn't contain " + childLocalName + " children";
	}

	public static String MALFORMED_CONSTRAINT_NODE_DEFINITION(String methodName, String constraintName) {
		return "Error while parsing constraint " + constraintName + " in method " + methodName 
				+ "Constraint tag must contain Premise and Consequence children, that consist of single statement.";
	}

	public static String WRONG_STATEMENT_ARRAY_OPERATOR(String methodName, String operator) {
		return "Error while parsing statement array in method " + methodName 
				+ ". Operator " + operator + " is not allowed";
	}

	public static String WRONG_STATIC_STATEMENT_VALUE(String value) {
		return "Forbidden value of static statement: " + value;
	}

	public static String WRONG_OR_MISSING_RELATION_FORMAT(String methodName, String relation) {
		return "Forbidden relation value in statement for method " + methodName + ": " + relation;
	}

	public static String WRONG_CATEGORY_NAME(String categoryName, String methodName) {
		return "Category " + categoryName + " does not exist in method " + methodName;
	}

	public static String WRONG_PARTITION_NAME(String partitionName, String categoryName) {
		return "Partition " + partitionName + " does not exist in method " + categoryName;
	}

	public static String WRONG_TEST_PAREMETERS_NUMBER(String testSuiteName){
		return "Number of test parameters in test case of " + testSuiteName 
				+ "suite is different than number of parameters in parent method";
	}

	public static String TEST_VALUE_NAME_ATTRIBUTE_MISSING(String testSuiteName) {
		return "Missing test value attribute in a test case in " + testSuiteName + " suite";
	}

	public static String PARTITION_DOES_NOT_EXIST(String categoryName, String partitionName) {
		return "Partition " + partitionName + " is not defined for category " + categoryName;
	}
}
