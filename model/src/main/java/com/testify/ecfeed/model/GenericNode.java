/*******************************************************************************
 * Copyright (c) 2013 Testify AS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Patryk Chamuczynski (p.chamuczynski(at)gmail.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GenericNode implements IGenericNode{
	private String fName;
	private IGenericNode fParent;
	private final int fId;
	private static int fLastId = 0;
	protected final ArrayList<IGenericNode> EMPTY_CHILDREN_ARRAY = new ArrayList<IGenericNode>();

	public GenericNode(String name){
		fId = ++fLastId;
		this.fName = name;
	}
	
	public int getId(){
		return fId;
	}
	
	@Override
	public String getName() {
		return fName;
	}

	@Override
	public void setName(String name) {
		this.fName = name;
	}
	
	@Override
	public void setParent(IGenericNode newParent) {
		fParent = newParent;
	}

	@Override
	public List<? extends IGenericNode> getChildren() {
		return EMPTY_CHILDREN_ARRAY;
	}
	
	@Override
	public boolean hasChildren(){
		if(getChildren() != null){
			return (getChildren().size() > 0);
		}
		return false;
	}
	
	@Override
	public IGenericNode getParent(){
		return fParent;
	}
	
	@Override
	public IGenericNode getRoot(){
		if(getParent() == null){
			return this;
		}
		return getParent().getRoot();
	}

	@Override
	@Deprecated
	public boolean removeChild(IGenericNode child) {
		boolean result = getChildren().remove(child);
		if(result){
			child.setParent(null);
		}
		return result;
	}
	
	@Override
	@Deprecated
	public boolean removeChildren(Collection<IGenericNode> children){
		return getChildren().removeAll(children);
	}
	
	@Override
	@Deprecated
	public boolean isParent(IGenericNode potentialChild){
		return getChildren().contains(potentialChild);
	}

	//TODO unit tests
	@Override
	public IGenericNode getChild(String name) {
		for(IGenericNode child : getChildren()){
			if (name.equals(child.getName())){
				return child;
			}
		}
		return null;
	}

	//TODO unit tests
	@Override
	public void moveChild(IGenericNode child, boolean moveUp) {
		int childIndex = getChildren().indexOf(child);
		if(moveUp && childIndex > 0){
			Collections.swap(getChildren(), childIndex, childIndex - 1);
		}
		if(!moveUp && childIndex < getChildren().size() - 1){
			Collections.swap(getChildren(), childIndex, childIndex + 1);
		}
	}
	
	@Override
	public int subtreeSize(){
		int size = 1;
		for(IGenericNode child : getChildren()){
			size += child.subtreeSize();
		}
		return size;
	}

	@Override
	public String toString(){
		return getName();
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof GenericNode){
			return ((GenericNode)obj).getId() == fId;
		}
		return false;
	}
}
