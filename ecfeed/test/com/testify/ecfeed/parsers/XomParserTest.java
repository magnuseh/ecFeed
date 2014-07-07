package com.testify.ecfeed.parsers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.Test;

import static com.testify.ecfeed.testutils.Constants.*;

import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.parsers.xml.XomConverter;
import com.testify.ecfeed.parsers.xml.XomParser;
import com.testify.ecfeed.testutils.ModelStringifier;
import com.testify.ecfeed.testutils.RandomModelGenerator;

public class XomParserTest {
	
	private final boolean DEBUG = false;
	
	RandomModelGenerator fModelGenerator = new RandomModelGenerator();
	XomParser fParser = new XomParser();
	ModelStringifier fStringifier = new ModelStringifier();
	
//	@Test
	public void parseRootTest(){
		RootNode root = fModelGenerator.generateModel();
		
		Element rootElement = (Element)root.convert(new XomConverter());
		
		TRACE(rootElement);
		
		try {
			RootNode parsedRoot = fParser.parseRoot(rootElement);
			
			assertTrue(parsedRoot.compare(root));

			

		} catch (ParserException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
//	@Test
	public void parseClassTest(){
		ClassNode _class = fModelGenerator.generateClass();

		Element classElement = (Element)_class.convert(new XomConverter());
		
		TRACE(classElement);
		
		try {
			ClassNode parsedRoot = fParser.parseClass(classElement);
			
			assertTrue(parsedRoot.compare(_class));

		} catch (ParserException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
	}
	
	@Test
	public void parsePartitionTest(){
		for(String type: SUPPORTED_TYPES){
			System.out.println(type);
			
			PartitionNode p = fModelGenerator.generatePartition(5, 5, 10, type);
			
			Element element = (Element)p.convert(new XomConverter());
			
			PartitionNode p1;
			try {
				p1 = fParser.parsePartition(element);
				if(p.compare(p1) == false){
					fail("Parsed element differs from original\n" + fStringifier.stringify(p, 0) + "\n" + fStringifier.stringify(p1, 0));
				}
			} catch (ParserException e) {
				fail("Unexpected exception: " + e.getMessage());
			}
			
		}
	}
	
//	@Test
	public void assertTypeTest(){
		RootNode root = fModelGenerator.generateModel();
		ClassNode _class = fModelGenerator.generateClass();
		
		Element rootElement = (Element)root.convert(new XomConverter());
		Element classElement = (Element)_class.convert(new XomConverter());
		
		try {
			fParser.parseRoot(rootElement);
		} catch (ParserException e) {
			fail("Unexpected exception: " + e.getMessage());
		}

		try {
			fParser.parseClass(classElement);
		} catch (ParserException e) {
			fail("Unexpected exception: " + e.getMessage());
		}

		try {
			fParser.parseClass(rootElement);
			fail("exception expected");
		} catch (ParserException e) {
		}
	
		try {
			fParser.parseRoot(classElement);
			fail("exception expected");
		} catch (ParserException e) {
		}
	}
	
	private void TRACE(Element element){
		if(!DEBUG) return;
		
		Document document = new Document(element);
		OutputStream ostream = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(ostream);
		// Uncomment for pretty formatting. This however will affect 
		// whitespaces in the document's ... infoset
		serializer.setIndent(4);
		try {
			serializer.write(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(ostream);
	}

}
