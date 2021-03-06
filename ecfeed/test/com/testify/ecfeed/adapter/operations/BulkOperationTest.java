package com.testify.ecfeed.adapter.operations;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.testify.ecfeed.adapter.IModelOperation;
import com.testify.ecfeed.adapter.ModelOperationException;
import com.testify.ecfeed.adapter.ModelOperationManager;

public class BulkOperationTest {

	private class DummyPrimitiveOperation extends AbstractModelOperation{

		public DummyPrimitiveOperation(String name) {
			super(name);
		}

		@Override
		public void execute() throws ModelOperationException {
//			System.out.println("executed " + getName());
		}

		@Override
		public IModelOperation reverseOperation() {
			return new DummyPrimitiveOperation("reverse " + getName());
		}
		
	}
	
	private class DummyBulkOperation extends BulkOperation{

		public DummyBulkOperation(String name) {
			super(name, false);
			addOperation(new DummyPrimitiveOperation("op1"));	
			addOperation(new DummyPrimitiveOperation("op2"));	
		}
	}
	
	@Test
	public void undoRedoTest(){
		ModelOperationManager opManager = new ModelOperationManager();
		
		IModelOperation operation = new DummyBulkOperation("operation");
		try{
			opManager.execute(operation);
//			System.out.println("Undo:");
			opManager.undo();
//			System.out.println("Redo:");
			opManager.redo();
		}catch(ModelOperationException e){
			fail("Unexpected exception: " + e.getMessage());
		}
	}
}
