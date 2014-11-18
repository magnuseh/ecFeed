package com.testify.ecfeed.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class DecomposedNode extends GenericNode{

	private List<ChoiceNode> fPartitions;

	public DecomposedNode(String name) {
		super(name);
		fPartitions = new ArrayList<ChoiceNode>();
	}

	public abstract ParameterNode getParameter();

	@Override
	public List<? extends GenericNode> getChildren(){
		return fPartitions;
	}

	public List<ChoiceNode> getPartitions() {
		return fPartitions;
	}

	public void addPartition(ChoiceNode partition) {
		addPartition(partition, fPartitions.size());
	}

	public void addPartition(ChoiceNode partition, int index) {
			fPartitions.add(index, partition);
			partition.setParent(this);
	}

	public ChoiceNode getPartition(String qualifiedName) {
		return (ChoiceNode)getChild(qualifiedName);
	}

	public boolean removePartition(ChoiceNode partition) {
		if(fPartitions.contains(partition) && fPartitions.remove(partition)){
			partition.setParent(null);
			return true;
		}
		return false;
	}

	public void replacePartitions(List<ChoiceNode> newPartitions) {
		fPartitions.clear();
		fPartitions.addAll(newPartitions);
		for(ChoiceNode p : newPartitions){
			p.setParent(this);
		}
	}

	public List<ChoiceNode> getLeafPartitions() {
		List<ChoiceNode> result = new ArrayList<ChoiceNode>();
		for(ChoiceNode p : fPartitions){
			if(p.isAbstract() == false){
				result.add(p);
			}
			result.addAll(p.getLeafPartitions());
		}
		return result;
	}

	public Set<ChoiceNode> getAllPartitions() {
		Set<ChoiceNode> result = new LinkedHashSet<ChoiceNode>();
		for(ChoiceNode p : fPartitions){
			result.add(p);
			result.addAll(p.getAllPartitions());
		}
		return result;
	}

	public Set<String> getAllPartitionNames() {
		Set<String> result = new LinkedHashSet<String>();
		for(ChoiceNode p : fPartitions){
			result.add(p.getQualifiedName());
			result.addAll(p.getAllPartitionNames());
		}
		return result;
	}

	public Set<String> getPartitionNames() {
		Set<String> result = new LinkedHashSet<String>();
		for(ChoiceNode p : fPartitions){
			result.add(p.getName());
		}
		return result;
	}

	public Set<ChoiceNode> getLabeledPartitions(String label) {
		Set<ChoiceNode> result = new LinkedHashSet<ChoiceNode>();
		for(ChoiceNode p : fPartitions){
			if(p.getLabels().contains(label)){
				result.add(p);
			}
			result.addAll(p.getLabeledPartitions(label));
		}
		return result;
	}

	public Set<String> getLeafLabels() {
		Set<String> result = new LinkedHashSet<String>();
		for(ChoiceNode p : getLeafPartitions()){
			result.addAll(p.getAllLabels());
		}
		return result;
	}

	public Set<String> getLeafPartitionValues(){
		Set<String> result = new LinkedHashSet<String>();
		for(ChoiceNode p : getLeafPartitions()){
			result.add(p.getValueString());
		}
		return result;
	}

	public Set<String> getLeafPartitionNames(){
		Set<String> result = new LinkedHashSet<String>();
		for(ChoiceNode p : getLeafPartitions()){
			result.add(p.getQualifiedName());
		}
		return result;
	}
}