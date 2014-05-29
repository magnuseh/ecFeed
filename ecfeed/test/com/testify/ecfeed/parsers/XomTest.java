package com.testify.ecfeed.parsers;

import static org.junit.Assert.*;
import nu.xom.Element;

import org.junit.Test;

import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.parsers.xml.xom.XomConverter;
import com.testify.ecfeed.parsers.xml.xom.XomParser;
import com.testify.ecfeed.testutils.RandomModelGenerator;

public class XomTest {
	RandomModelGenerator fModelGenerator = new RandomModelGenerator();
	XomConverter fXomConverter = new XomConverter();
	XomParser fXomParser = new XomParser();
	
	@Test
	public void partitionTest(){
		String[] types = {"int", "short", "long", "byte", "double", "float", "byte", "String"};
		
		for(String type : types){
			PartitionNode partition = fModelGenerator.generatePartitionNode(type, 3, 3, 3);
			Element partitionElement = (Element)partition.convert(fXomConverter);
			
			try {
				PartitionNode parsedPartition = fXomParser.parsePartitionElement(partitionElement);
				
				assertTrue(partition.compare(parsedPartition));
			} catch (ParserException e) {
				fail("Unexpected exception: " + e.getMessage());
			} 
		}
	}
}
