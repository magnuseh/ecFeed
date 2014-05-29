package com.testify.ecfeed.testutils;

import static com.testify.ecfeed.utils.Constants.PARTITION_LABEL_REGEX;
import static com.testify.ecfeed.utils.Constants.PARTITION_NAME_REGEX;

import java.util.Random;

import nl.flotsam.xeger.Xeger;

import org.junit.Test;

import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.utils.ClassUtils;

public class RandomModelGenerator {
	Random rand = new Random();

	private enum Type{
		INT,
		LONG,
		SHORT,
		DOUBLE,
		FLOAT,
		CHAR,
		STRING,
		BOOLEAN,
		BYTE
	}
	
	public PartitionNode generatePartitionNode(String typeSignature, int depth, int maxSubpartitions, int labelsCount){
		Object value = generatePartitionValue(typeSignature);
		PartitionNode partitionNode = new PartitionNode(generateString(PARTITION_NAME_REGEX), value.toString());
		
		for(int i = 0; i < labelsCount; i++){
			partitionNode.addLabel(generateString(PARTITION_LABEL_REGEX));
		}
		
		if(depth != 0){
			for(int i = 0; i < rand.nextInt(maxSubpartitions + 1); i++){
				partitionNode.addPartition(generatePartitionNode(typeSignature, depth - 1, maxSubpartitions, labelsCount));
			}
		}
		
		return partitionNode;
	}
	
	protected Object generatePartitionValue(String typeSignature){
		switch(typeSignature){
		case "boolean":
			return rand.nextBoolean(); 
		case "byte":
			return (byte)rand.nextInt(); 
		case "char":
			return (char)rand.nextInt(); 
		case "double":
			return rand.nextDouble(); 
		case "float":
			return rand.nextFloat(); 
		case "int":
			return rand.nextInt(); 
		case "long":
			return rand.nextLong(); 
		case "short":
			return (short)rand.nextInt(65536) - Short.MAX_VALUE; 
		case "String":
			if(rand.nextInt(5) == 0) return null;
			return generateString("[A-Za-z0-9_ ]*"); 
		default:
			return null;
		}
	}
	
	protected String generateString(String regex){
		Xeger generator = new Xeger(regex);
		return generator.generate();
	}
	
	@Test
	public void generatePartitionTest(){
		generatePartitionNode("int", 3, 5, 5);
		generatePartitionNode("String", 3, 5, 5);
	}
}
