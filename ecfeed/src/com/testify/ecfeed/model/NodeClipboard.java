package com.testify.ecfeed.model;

public class NodeClipboard{
	
	private IGenericNode fClipboardNode = null;
	private IGenericNode fOriginalNode = null;
	
	public IGenericNode getClipboardNode(){
		return fClipboardNode;
	}
	
	public IGenericNode getOriginalNode(){
		return fOriginalNode;
	}
	
	public void setClipboardNode(IGenericNode node){
		fClipboardNode = node.getCopy();
		fOriginalNode = node;
	}
	
}
