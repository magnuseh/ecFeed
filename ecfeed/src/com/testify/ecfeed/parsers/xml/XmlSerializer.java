package com.testify.ecfeed.parsers.xml;

import java.io.IOException;
import java.io.OutputStream;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.IModelSerializer;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.TestCaseNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.ExpectedValueStatement;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.model.constraint.StaticStatement;
import com.testify.ecfeed.parsers.xml.xom.XomConverter;

public class XmlSerializer implements IModelSerializer {
	
	XomConverter fBuilder;
	Serializer fSerializer;

	public XmlSerializer(OutputStream ostream){
		fBuilder = new XomConverter();
		fSerializer = new Serializer(ostream);
	}


	private void serializeElement(Element element) throws IOException {
		Document document = new Document(element);

		// Uncomment for pretty formatting. This however will affect 
		// whitespaces in the document's ... infoset
		//serializer.setIndent(4);
		fSerializer.write(document);
	}


	@Override
	public void serialize(IGenericNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}

	public void serialize(RootNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}

	public void serialize(ClassNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}

	public void serialize(MethodNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}

	public void serialize(CategoryNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}

	public void serialize(PartitionNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}

	public void serialize(TestCaseNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}
	
	public void serialize(ConstraintNode node) throws IOException {
		serializeElement((Element)fBuilder.convert(node));
	}
	
	/*Not nice, but useful*/
	public void serialize(BasicStatement statement) throws IOException {
		if(statement instanceof StatementArray){
			serialize((StatementArray)statement);
		}
		else if(statement instanceof StaticStatement){
			serialize((StaticStatement)statement);
		}
		else if(statement instanceof PartitionedCategoryStatement){
			serialize((PartitionedCategoryStatement)statement);
		}
		else if(statement instanceof ExpectedValueStatement){
			serialize((ExpectedValueStatement)statement);
		}
		else{
			System.err.println("Unknown type of serialized statement");
		}
	}


	public void serialize(StatementArray statement) throws IOException {
		serializeElement((Element)fBuilder.convert(statement));
	}

	public void serialize(StaticStatement statement) throws IOException {
		serializeElement((Element)fBuilder.convert(statement));
	}

	public void serialize(ExpectedValueStatement statement) throws IOException {
		serializeElement((Element)fBuilder.convert(statement));
	}

	public void serialize(PartitionedCategoryStatement statement) throws IOException {
		serializeElement((Element)fBuilder.convert(statement));
	}
}
