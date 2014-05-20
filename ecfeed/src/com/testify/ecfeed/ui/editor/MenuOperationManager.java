package com.testify.ecfeed.ui.editor;

import java.util.ArrayList;
import java.util.List;

import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.NodeClipboard;
import com.testify.ecfeed.model.TestCaseNode;

public class MenuOperationManager{

	private NodeClipboard fClipboard;
	private ModelMasterSection fModel;

	public List<MenuOperation> getOperations(IGenericNode target){
		ArrayList<MenuOperation> operations = new ArrayList<>();

		operations.add(new MenuCopyOperation("Copy", target, fClipboard));
		if(fClipboard.getClipboardNode() != null){
			operations.add(new MenuPasteOperation("Paste", target, fClipboard.getClipboardNode().getCopy(), fModel));
		} else{
			operations.add(new MenuPasteOperation("Paste", target, null, fModel));
		}
		if(target instanceof MethodNode && fClipboard.getClipboardNode() instanceof TestCaseNode){
			operations.add(new MenuPasteSuiteOperation("Paste test suite", target, fClipboard.getOriginalNode(), fModel));
		}

		return operations;
	}

	public MenuOperationManager(ModelMasterSection model){
		fClipboard = new NodeClipboard();
		fModel = model;
	}

	public void setClipboard(NodeClipboard clipboard){
		fClipboard = clipboard;
	}

}
