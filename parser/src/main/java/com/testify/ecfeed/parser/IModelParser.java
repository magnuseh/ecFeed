package com.testify.ecfeed.parser;

import java.io.InputStream;

import com.testify.ecfeed.model.RootNode;

public interface IModelParser {
	public RootNode parseModel(InputStream istream) throws ParserException;
}
