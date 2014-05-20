package com.testify.ecfeed.ui.editor;

import java.util.Collection;

import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.TestCaseNode;

public class MenuPasteSuiteOperation extends MenuPasteOperation{

	public MenuPasteSuiteOperation(String name, IGenericNode target, IGenericNode source, ModelMasterSection model){
		super(name, target, source, model);
	}

	@Override
	public void operate(){
		if(fSource != null && fTarget != null){
			if(fTarget instanceof MethodNode && fSource instanceof TestCaseNode){
				TestCaseNode tcase = (TestCaseNode)fSource;
				MethodNode targetmethod = (MethodNode)fTarget;
				Collection<TestCaseNode> testsuite = tcase.getMethod().getTestCases(tcase.getName());
				for(TestCaseNode tcnode : testsuite){
					TestCaseNode copy = tcnode.getCopy();
					targetmethod.addTestCase(copy);
					copy.updateReferences();
				}
			}
			fModel.markDirty();
			fModel.refresh();
		}
	}

	@Override
	public boolean isEnabled(){
		if(fSource != null && fTarget != null && fTarget instanceof MethodNode && fSource instanceof TestCaseNode){
			TestCaseNode tcase = (TestCaseNode)fSource;
			if(tcase.getMethod() != null){
				Collection<TestCaseNode> testsuite = tcase.getMethod().getTestCases(tcase.getName());
				for(TestCaseNode tcnode : testsuite){
					fSource = tcnode;
					if(!super.isEnabled()) return false;
				}
				return true;
			}
		}
		return false;
	}

}
