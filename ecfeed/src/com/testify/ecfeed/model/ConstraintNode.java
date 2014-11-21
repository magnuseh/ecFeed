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

import java.util.List;

public class ConstraintNode extends AbstractNode{

	private Constraint fConstraint;

	@Override
	public int getIndex(){
		if(getMethod() == null){
			return -1;
		}
		return getMethod().getConstraintNodes().indexOf(this);
	}

	@Override
	public String toString(){
		return getName() + ": " + getConstraint().toString();
	}

	@Override
	public ConstraintNode getCopy(){
		return new ConstraintNode(getName(), fConstraint.getCopy());
	}

	public ConstraintNode(String name, Constraint constraint) {
		super(name);
		fConstraint = constraint;
	}

	public Constraint getConstraint(){
		return fConstraint;
	}

	public MethodNode getMethod() {
		if(getParent() != null && getParent() instanceof MethodNode){
			return (MethodNode)getParent();
		}
		return null;
	}

	public void setMethod(MethodNode method){
		setParent(method);
	}

	public boolean evaluate(List<ChoiceNode> values) {
		if(fConstraint != null){
			return fConstraint.evaluate(values);
		}
		return false;
	}

	public boolean mentions(ChoiceNode choice) {
		if(fConstraint.mentions(choice)){
			return true;
		}
		return false;
	}

	public boolean mentions(ParameterNode parameter) {
		return fConstraint.mentions(parameter);
	}

	public boolean mentions(ParameterNode parameter, String label) {
		return fConstraint.mentions(parameter, label);
	}

	public boolean updateReferences(MethodNode method){
		if(fConstraint.updateRefrences(method)){
			setParent(method);
			return true;
		}
		return false;
	}

	public ConstraintNode getCopy(MethodNode method){
		ConstraintNode copy = getCopy();
		if(copy.updateReferences(method))
			return copy;
		else
			return null;
	}

	@Override
	public boolean compare(AbstractNode node){
		if(node instanceof ConstraintNode == false){
			return false;
		}
		ConstraintNode compared = (ConstraintNode)node;
		if(getConstraint().getPremise().compare(compared.getConstraint().getPremise()) == false){
			return false;
		}

		if(getConstraint().getConsequence().compare(compared.getConstraint().getConsequence()) == false){
			return false;
		}

		return super.compare(node);
	}

	@Override
	public Object accept(IModelVisitor visitor) throws Exception {
		return visitor.visit(this);
	}

	public boolean isConsistent() {
		for(ChoiceNode p : getConstraint().getReferencedChoices()){
			ParameterNode c = p.getParameter();
			if(c == null || c.getChoice(p.getQualifiedName()) == null){
				return false;
			}
			if((c.getMethod() == null) || (c.getMethod().getParameters().contains(c) == false)){
				return false;
			}
		}
		for(ParameterNode c : getMethod().getParameters()){
			for(String label : getConstraint().getReferencedLabels(c)){
				if(c.getLeafLabels().contains(label) == false){
					return false;
				}
			}
		}
		for(ParameterNode c: getConstraint().getReferencedParameters()){
			if(getMethod().getParameters().contains(c) == false){
				return false;
			}
		}
		return true;
	}

	@Override
	public int getMaxIndex(){
		if(getMethod() != null){
			return getMethod().getConstraintNodes().size();
		}
		return -1;
	}

}
