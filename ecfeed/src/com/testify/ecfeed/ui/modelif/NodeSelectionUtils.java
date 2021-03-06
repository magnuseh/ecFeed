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

package com.testify.ecfeed.ui.modelif;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.testify.ecfeed.model.AbstractNode;

public class NodeSelectionUtils {
	
	private ISelectionProvider fSelectionProvider;

	public NodeSelectionUtils(ISelectionProvider selectionProvider){
		fSelectionProvider = selectionProvider;
	}
	
	public SelectionInterface getSelectionInterface(IModelUpdateContext context){
		SelectionInterface selectionIf = new SelectionInterface(context);
		selectionIf.setTarget(getSelectedNodes());
		return selectionIf;
	}
	
	public boolean isSelectionSibling(){
		List<AbstractNode> nodes = getSelectedNodes();
		if(nodes.isEmpty()) return false;
		AbstractNode parent = nodes.get(0).getParent();
		for(AbstractNode node : nodes){
			if(node.getParent() != parent) return false;
		}
		return true;
	}
	
	public boolean isSelectionSingleType(){
		List<AbstractNode> nodes = getSelectedNodes();
		if(nodes.isEmpty()) return false;
		Class<?> type = nodes.get(0).getClass();
		for(AbstractNode node : nodes){
			if(node.getClass().equals(type) == false) return false;
		}
		return true;
	}

	public List<AbstractNode> getSelectedNodes() {
		List<AbstractNode> result = new ArrayList<>();
		IStructuredSelection selection = (IStructuredSelection)fSelectionProvider.getSelection();
		for(Object o : selection.toList()){
			if(o instanceof AbstractNode){
				result.add((AbstractNode)o);
			}
		}
		return result;
	}

}
