/*******************************************************************************
 * Copyright (c) 2013 Testify AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html                                     
 *                                                                               
 * Contributors:                                                                 
 *     Patryk Chamuczynski (p.chamuczynski(at)radytek.com) - initial implementation
 ******************************************************************************/

package com.testify.ecfeed.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.testify.ecfeed.generators.api.IConstraint;

public class MethodNode extends GenericNode {
	private List<CategoryNode> fCategories;
	private List<TestCaseNode> fTestCases;
	private List<ConstraintNode> fConstraints;
	
	public MethodNode(String name){
		super(name);
		fCategories = new ArrayList<CategoryNode>();
		fTestCases = new ArrayList<TestCaseNode>();
		fConstraints = new ArrayList<ConstraintNode>();
	}
	
	@Override
	public String toString(){
		String result = new String(getName()) + "(";
		ArrayList<String> types = getCategoriesTypes();
		ArrayList<String> names = getCategoriesNames();
		for(int i = 0; i < types.size(); i++){
			if(getCategories().get(i).isExpected()){
				result += "[e]";
			}
			result += types.get(i);
			result += " ";
			result += names.get(i);
			if(i < types.size() - 1) result += ", ";
		}
		result += ")";
		return result;
	}

	@Override
	public ArrayList<? extends IGenericNode> getChildren(){
		ArrayList<IGenericNode> children = new ArrayList<IGenericNode>();
		children.addAll(fCategories);
		children.addAll(fConstraints);
		children.addAll(fTestCases);
		
		return children;
	}

	@Override
	public boolean hasChildren(){
		return(fCategories.size() != 0 || fConstraints.size() != 0 || fTestCases.size() != 0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void moveChild(IGenericNode child, boolean moveUp){
		List childrenArray = null;
		if(child instanceof CategoryNode){
			childrenArray = fCategories;
		}
		if(child instanceof ConstraintNode){
			childrenArray = fConstraints;
		}
		if(child instanceof TestCaseNode){
			childrenArray = fTestCases;
		}
		if(childrenArray == null){
			return;
		}
		
		int childIndex = childrenArray.indexOf(child);
		if(moveUp && childIndex > 0){
			Collections.swap(childrenArray, childIndex, childIndex - 1);
		}
		if(!moveUp && childIndex < childrenArray.size() - 1){
			Collections.swap(childrenArray, childIndex, childIndex + 1);
		}
	}

	public void addCategory(CategoryNode category){
		fCategories.add(category);
		category.setParent(this);
	}

	public void addConstraint(ConstraintNode constraint) {
		constraint.setParent(this);
		fConstraints.add(constraint);
	}
	
	public void addTestCase(TestCaseNode testCase){
		fTestCases.add(testCase);
		testCase.setParent(this);
	}
	
	public ClassNode getClassNode() {
		return (ClassNode)getParent();
	}

	public List<CategoryNode> getCategories(){
		return fCategories;
	}

	public CategoryNode getCategory(String categoryName) {
		for(CategoryNode category : fCategories){
			if(category.getName().equals(categoryName)){
				return category;
			}
		}
		return null;
	}
	
	public CategoryNode getExpectedCategory(String categoryName) {
		for(CategoryNode category : fCategories){
			if(category.isExpected() && category.getName().equals(categoryName)){
				return category;
			}
		}
		return null;
	}

	public CategoryNode getPartitionedCategory(String categoryName) {
		for(CategoryNode category : fCategories){
			if((!category.isExpected()) && category.getName().equals(categoryName)){
				return category;
			}
		}
		return null;
	}

	public List<CategoryNode> getCategories(boolean expected) {
		ArrayList<CategoryNode> categories = new ArrayList<>();
		for(CategoryNode category : fCategories){
			if(category.isExpected() == expected){
				categories.add(category);
			}
		}
		return categories;
	}

	public ArrayList<String> getCategoriesTypes() {
		ArrayList<String> types = new ArrayList<String>();
		for(CategoryNode category : fCategories){
			types.add(category.getType());
		}
		return types;
	}

	public ArrayList<String> getCategoriesNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(CategoryNode category : fCategories){
			names.add(category.getName());
		}
		return names;
	}

	public ArrayList<String> getCategoriesNames(boolean expected) {
		ArrayList<String> names = new ArrayList<String>();
		for(CategoryNode category : fCategories){
			if(category.isExpected() == expected){
				names.add(category.getName());
			}
		}
		return names;
	}

	public List<ConstraintNode> getConstraintNodes(){
		return fConstraints;
	}

	public List<IConstraint<PartitionNode>> getAllConstraints(){
		List<IConstraint<PartitionNode>> constraints = new ArrayList<IConstraint<PartitionNode>>();
		for(ConstraintNode node : fConstraints){
			constraints.add(node.getConstraint());
		}
		return constraints;
	}

	public List<IConstraint<PartitionNode>> getConstraints(String name) {
		List<IConstraint<PartitionNode>> constraints = new ArrayList<IConstraint<PartitionNode>>();
		for(ConstraintNode node : fConstraints){
			if(node.getName().equals(name)){
				constraints.add(node.getConstraint());
			}
		}
		return constraints;
	}

	public Set<String> getConstraintsNames() {
		Set<String> names = new HashSet<String>();
		for(ConstraintNode constraint : fConstraints){
			names.add(constraint.getName());
		}
		return names;
	}

	public List<TestCaseNode> getTestCases(){
		return fTestCases;
	}
	
	public Collection<TestCaseNode> getTestCases(String testSuite) {
		ArrayList<TestCaseNode> testCases = new ArrayList<TestCaseNode>();
		for(TestCaseNode testCase : getTestCases()){
			if(testSuite.equals(testCase.getName())){
				testCases.add(testCase);
			}
		}
		return testCases;
	}

	public Set<String> getTestSuites(){
		Set<String> testSuites = new HashSet<String>();
		for(TestCaseNode testCase : getTestCases()){
			testSuites.add(testCase.getName());
		}
		return testSuites;
	}

	public boolean removeCategory(CategoryNode category){
		category.setParent(null);
		if(fCategories.remove(category)){
			removeMentioningConstraints(category);
			return true;
		}
		return false;
	}

	public boolean removeTestCase(TestCaseNode testCase){
		testCase.setParent(null);
		return fTestCases.remove(testCase);
	}

	public boolean removeConstraint(ConstraintNode constraint) {
		constraint.setParent(null);
		return fConstraints.remove(constraint);
	}

	public void removeTestSuite(String suiteName) {
		Iterator<TestCaseNode> iterator = getTestCases().iterator();
		while(iterator.hasNext()){
			TestCaseNode testCase = iterator.next();
			if(testCase.getName().equals(suiteName)){
				iterator.remove();
			}
		}
	}
	
	public void replaceCategoryOfSameType(int index, CategoryNode newCategory){		
		CategoryNode oldCategory = fCategories.get(index);
		if(removeCategory(oldCategory)){
			newCategory.setParent(this);
			fCategories.add(index, newCategory);
			if(!oldCategory.isExpected()){
				for(TestCaseNode testCase : fTestCases){
					testCase.replaceValue(index, newCategory.getDefaultValuePartition().getCopy());
				}			
			}
			else fTestCases.clear();
		}
	}
	
	public void changeCategoryExpectedStatus(CategoryNode category){
		int index = fCategories.indexOf(category);
		if(index < 0) return;
		else{
			// from expected to partitioned
			if(category.isExpected()){
				fTestCases.clear();
				removeMentioningConstraints(category);
				if(category.getOrdinaryPartitions().isEmpty()){
					category.addPartition(category.getDefaultValuePartition());
				}
			}
			// from partitioned to expected
			else{
				if(category.getDefaultValue() == null)
					category.setDefaultValue(category.getPartitions().get(0).getValue());
				for(TestCaseNode testCase : fTestCases){
					testCase.replaceValue(index, category.getDefaultValuePartition().getCopy());
				}
				for(PartitionNode partition : category.getPartitions()){
					removeMentioningConstraints(partition);
				}
			}
		}
	}

	public void partitionRemoved(PartitionNode partition){
		removeMentioningConstraints(partition);
		removeMentioningTestCases(partition);
	}

	public boolean validateConstraintName(String name) {
		return super.validateNodeName(name);
	}

	protected void removeMentioningConstraints(PartitionNode partition) {
		Iterator<ConstraintNode> iterator = fConstraints.iterator();
		while(iterator.hasNext()){
			if(iterator.next().mentions(partition)){
				iterator.remove();
			}
		}
	}
	
	public void removeMentioningConstraints(CategoryNode category){
		Iterator<ConstraintNode> it = fConstraints.iterator();
		while(it.hasNext()){
			ConstraintNode constraint = it.next();
			if(constraint.mentions(category)){
				it.remove();
			}
		}
	}

	protected void removeMentioningTestCases(PartitionNode partition) {
		Iterator<TestCaseNode> iterator = fTestCases.iterator();
		while(iterator.hasNext()){
			if(iterator.next().mentions(partition)){
				iterator.remove();
			}
		}
	}
}
