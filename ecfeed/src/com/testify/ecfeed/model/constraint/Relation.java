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

package com.testify.ecfeed.model.constraint;

public enum Relation{
	
	LESS("<"), 
	LESS_EQUAL("\u2264"), 
	EQUAL("="), 
	GREATER_EQUAL("\u2265"), 
	GREATER(">"), 
	NOT("\u2260");
	
	String fValue;

	public static final String RELATION_LESS = "<";
	public static final String RELATION_LESS_EQUAL = "\u2264";
	public static final String RELATION_EQUAL = "=";
	public static final String RELATION_GREATER_EQUAL = "\u2265";
	public static final String RELATION_GREATER = ">";
	public static final String RELATION_NOT = "\u2260";

	
	private Relation(String value){
		fValue = value;
	}
	
	public static Relation getRelation(String text){
		switch(text){
		case RELATION_LESS:
			return LESS;
		case RELATION_LESS_EQUAL:
			return LESS_EQUAL;
		case RELATION_EQUAL:
			return EQUAL;
		case RELATION_GREATER_EQUAL:
			return GREATER_EQUAL;
		case RELATION_GREATER:
			return GREATER;
		case RELATION_NOT:
			return NOT;
		}
		return NOT;
	}

	public String toString(){
		return fValue; 
	}
}
