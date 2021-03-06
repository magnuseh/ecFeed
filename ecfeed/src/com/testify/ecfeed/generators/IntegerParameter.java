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

package com.testify.ecfeed.generators;

import java.util.Arrays;

import com.testify.ecfeed.generators.api.GeneratorException;

public class IntegerParameter extends AbstractParameter {

	private Integer[] fAllowedValues = null;
	private int fDefaultValue;
	private int fMinValue = Integer.MIN_VALUE;
	private int fMaxValue = Integer.MAX_VALUE;
	
	public IntegerParameter(String name, boolean required, int defaultValue){
		super(name, TYPE.INTEGER, required);
		fDefaultValue = defaultValue;
	}

	public IntegerParameter(String name, boolean required, int defaultValue, Integer[] allowedValues) throws GeneratorException {
		super(name, TYPE.INTEGER, required);
		fDefaultValue = defaultValue;
		fAllowedValues = allowedValues;
		if(!Arrays.asList(fAllowedValues).contains(fDefaultValue)){
			throw new GeneratorException("Inconsistent parameter definition");
		}
	}

	public IntegerParameter(String name, boolean required, int defaultValue, int min, int max) throws GeneratorException {
		super(name, TYPE.INTEGER, required);
		fDefaultValue = defaultValue;
		fMinValue = min;
		fMaxValue = max;
		if(fDefaultValue < fMinValue || fDefaultValue > fMaxValue){
			throw new GeneratorException("Inconsistent parameter definition");
		}
	}
	
	@Override
	public Object[] allowedValues(){
		return fAllowedValues;
	}

	@Override
	public Object defaultValue() {
		return fDefaultValue;
	}

	@Override
	public boolean test(Object value){
		if (value instanceof Integer == false){
			return false;
		}
		int intValue = (Integer)value;
		if(allowedValues() != null){
			boolean isAllowed = false;
			for(Object allowed : allowedValues()){
				if(value.equals(allowed)){
					isAllowed = true;
				}
			}
			return isAllowed;
		}
		return (intValue >= fMinValue && intValue <= fMaxValue);
	}

	public int getMin(){
		return fMinValue;
	}
	
	public int getMax(){
		return fMaxValue;
	}
}
