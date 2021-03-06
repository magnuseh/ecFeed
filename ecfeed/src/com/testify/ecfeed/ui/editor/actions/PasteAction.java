/*******************************************************************************
 * Copyright (c) 2015 Testify AS..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.ui.editor.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;

import com.testify.ecfeed.model.AbstractNode;
import com.testify.ecfeed.ui.modelif.AbstractNodeInterface;
import com.testify.ecfeed.ui.modelif.IModelUpdateContext;
import com.testify.ecfeed.ui.modelif.NodeClipboard;
import com.testify.ecfeed.ui.modelif.NodeInterfaceFactory;

public class PasteAction extends ModelModifyingAction {

	private TreeViewer fTreeViewer;
	private int fIndex;

	public PasteAction(ISelectionProvider selectionProvider, IModelUpdateContext updateContext) {
		this(-1, selectionProvider, updateContext);
	}
	
	public PasteAction(int index, ISelectionProvider selectionProvider, IModelUpdateContext updateContext) {
		super(GlobalActions.PASTE.getId(), GlobalActions.PASTE.getName(), selectionProvider, updateContext);
		fIndex = index;
	}
	
	public PasteAction(TreeViewer treeViewer, IModelUpdateContext updateContext) {
		this(-1, treeViewer, updateContext);
	}

	public PasteAction(int index, TreeViewer treeViewer, IModelUpdateContext updateContext) {
		this(index, (ISelectionProvider)treeViewer, updateContext);
		fTreeViewer = treeViewer;
	}

	@Override
	public boolean isEnabled(){
		if(getSelectedNodes().size() != 1) return false;
		AbstractNodeInterface nodeIf = NodeInterfaceFactory.getNodeInterface(getSelectedNodes().get(0), getUpdateContext()); 
		if (fIndex != -1){
			return nodeIf.pasteEnabled(NodeClipboard.getContent(), fIndex);
		}
		return nodeIf.pasteEnabled(NodeClipboard.getContent());
	}
	
	@Override
	public void run(){
		AbstractNode parent = getSelectedNodes().get(0);
		AbstractNodeInterface parentIf = NodeInterfaceFactory.getNodeInterface(parent, getUpdateContext()); 
		parentIf.addChildren(NodeClipboard.getContentCopy());
		if(fTreeViewer != null){
			fTreeViewer.expandToLevel(parent, 1);
		}
	}

}
