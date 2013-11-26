package com.testify.ecfeed.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraints {
	public static final String ALL = "__ALL__"; 
	public static final String NONE = "__NONE__"; 
	
	String[] value();
}
