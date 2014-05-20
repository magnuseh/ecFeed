package com.testify.ecfeed.ui.editor;

import com.testify.ecfeed.model.IGenericNode;
import com.testify.ecfeed.model.NodeClipboard;

public class MenuCopyOperation extends MenuOperation{
	private NodeClipboard fSource;
	private IGenericNode fTarget;

	public MenuCopyOperation(String name, IGenericNode target, NodeClipboard source){
		super(name);
		fTarget = target;
		fSource = source;
	}

	@Override
	public void operate(){
		fSource.setClipboardNode(fTarget);
	}

	@Override
	public boolean isEnabled(){
		return true;
	}
}