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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryNode extends GenericNode {
	
	private final String fType;
	protected final List<PartitionNode> fPartitions;
	
	public CategoryNode(String name, String type) {
		super(name);
		fType = type;
		fPartitions = new ArrayList<PartitionNode>();
	}

	public String getType() {
		return fType;
	}

	public List<? extends IGenericNode> getChildren(){
		return fPartitions;
	}
	
	public void addPartition(PartitionNode partition) {
		fPartitions.add(partition);
		partition.setParent(this);
	}
	
	public PartitionNode getPartition(String name){
		for(PartitionNode partition : fPartitions){
			if(partition.getName().equals(name)){
				return partition;
			}
		}
		return null;
	}
	
	public boolean removePartition(PartitionNode partition){
		if(fPartitions.contains(partition) && fPartitions.remove(partition)){
			MethodNode parent = getMethod();
			if(parent != null){
				parent.partitionRemoved(partition);
			}
		}
		return false;
	}

	public String toString(){
		return new String(getName() + ": " + getType());
	}

	public List<PartitionNode> getPartitions() {
		return fPartitions;
	}

	public Set<String> getPartitionNames() {
		Set<String> names = new HashSet<String>();
		for(PartitionNode partition : getPartitions()){
			names.add(partition.getName());
		}
		return names;
	}

	public MethodNode getMethod() {
		return (MethodNode)getParent();
	}

	public boolean isExpected() {
		return false;
	}
}
