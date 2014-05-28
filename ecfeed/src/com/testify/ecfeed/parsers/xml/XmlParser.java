package com.testify.ecfeed.parsers.xml;

import static com.testify.ecfeed.parsers.Constants.*;
import static com.testify.ecfeed.parsers.xml.Messages.*;

import java.io.IOException;
import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.ExpectedCategoryNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.PartitionedCategoryNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.parsers.IModelParser;
import com.testify.ecfeed.parsers.ParserException;
import com.testify.ecfeed.parsers.xml.xom.XomParser;

public class XmlParser implements IModelParser{
	
	Builder fBuilder;
	XomParser fParser;
	
	public XmlParser(){
		fBuilder = new Builder();
		fParser = new XomParser();
	}
	
	public RootNode parseModel(InputStream istream) throws ParserException{
		try {
			Document document = fBuilder.build(istream);
			return fParser.parseRootElement(document.getRootElement());
		} catch (ParsingException e) {
			throw new ParserException(PARSING_EXCEPTION(e));
		} catch (IOException e) {
			throw new ParserException(IO_EXCEPTION(e));
		}
	}

	@Override
	public ClassNode parseClass(InputStream istream) throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MethodNode parseMethod(InputStream istream) throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExpectedCategoryNode parseExpectedCategory(InputStream istream)
			throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PartitionedCategoryNode parsePartitionedCategory(InputStream istream)
			throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PartitionNode parsePartition(InputStream istream)
			throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestCaseNode parseTestCase(InputStream istream)
			throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConstraintNode parseConstraint(InputStream istream)
			throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatementArray parseStatement(InputStream istream)
			throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}
}
