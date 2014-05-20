package com.testify.ecfeed.ui.editor;

import java.util.Collection;

import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.TestCaseNode;

public class MenuPasteSuiteOperation extends MenuOperation{
	private IGenericNode fSource;
	private IGenericNode fTarget;
	private ModelMasterSection fModel;

	public MenuPasteSuiteOperation(String name, IGenericNode target, IGenericNode source, ModelMasterSection model){
		super(name);
		fTarget = target;
		fSource = source;
		fModel = model;
	}

	@Override
	public void operate(){
		if(fSource != null && fTarget != null){
			if(fTarget instanceof MethodNode && fSource instanceof TestCaseNode){
				TestCaseNode tcase = (TestCaseNode)fSource;
				MethodNode targetmethod = (MethodNode)fTarget;
				Collection<TestCaseNode> testsuite = tcase.getMethod().getTestCases(tcase.getName());
				for(TestCaseNode tcnode : testsuite){
					targetmethod.addTestCase(tcnode.getCopy());
				}
			}
			fModel.markDirty();
			fModel.refresh();
		}
	}

	@Override
	public boolean isEnabled(){
		if(fSource != null && fTarget != null){
			if(fTarget instanceof MethodNode){
				// add checking if adaptation is possible AND ask about TestSUITE node...
				if(fSource instanceof TestCaseNode){
					return true;
				}
			}
		}
		return false;
	}

}
