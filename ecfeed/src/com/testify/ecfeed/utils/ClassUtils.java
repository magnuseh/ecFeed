package com.testify.ecfeed.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.testify.ecfeed.model.PartitionNode;

public class ClassUtils {

	private static URLClassLoader classLoader = null;

	public static URLClassLoader getClassLoader(boolean create, ClassLoader parentLoader) {
		if ((classLoader == null) || create){
			List<URL> urls = new ArrayList<URL>();
			try {
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (IProject project : projects){
					if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)){
						IJavaProject javaProject = JavaCore.create(project);
						IPath path = project.getWorkspace().getRoot().getLocation();
						path = path.append(javaProject.getOutputLocation());
						urls.add(new URL("file", null, path.toOSString() + "/"));
					}
				}
				if (classLoader != null) {
					classLoader.close();
				}
			} catch (Throwable e) {
			}
			classLoader = new URLClassLoader(urls.toArray(new URL[]{}), parentLoader);
		}
		return classLoader;
	}

	public static ArrayList<PartitionNode> defaultEnumPartitions(String typeName) {
		ArrayList<PartitionNode> partitions = new ArrayList<PartitionNode>();
		try {
			Class<?> typeClass = getClassLoader(true, null).loadClass(typeName);
			if (typeClass != null) {
				for (Object object: typeClass.getEnumConstants()) {
					partitions.add(new PartitionNode(object.toString(), ((Enum<?>)object).name()));
				}	
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return partitions;
	}

	public static HashMap<String, String> defaultEnumValues(String typeName) {
		HashMap<String, String> values = new HashMap<String, String>();
		try {
			Class<?> typeClass = getClassLoader(true, null).loadClass(typeName);
			if (typeClass != null) {
				for (Object object: typeClass.getEnumConstants()) {
					values.put(((Enum<?>)object).name(), ((Enum<?>)object).name());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return values;
	}

	public static String defaultEnumExpectedValueString(String typeName) {
		String value = "";
		try {
			Class<?> typeClass = getClassLoader(true, null).loadClass(typeName);
			if (typeClass != null) {
				for (Object object: typeClass.getEnumConstants()) {
					value = ((Enum<?>)object).name();
					break;
				}	
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}			
		return value;
	}

	public static Object enumPartitionValue(String valueString, String typeName, ClassLoader loader) {
		Object value = null;
		try {
			Class<?> typeClass = loader.loadClass(typeName);
			if (typeClass != null) {
				for (Object object: typeClass.getEnumConstants()) {
					if ((((Enum<?>)object).name()).equals(valueString)) {
						value = object;
						break;
					}
				}
			}
		} catch (Throwable e) {
			//e.printStackTrace();
		}
		return value;
	}

	public static Object parseEnumValue(String valueString, String typeName, ClassLoader loader) {
		return enumPartitionValue(valueString, typeName, loader);
	}

	public static Object getPartitionValueFromString(String valueString, String type, ClassLoader loader) {
		if (isPredefinedValueString(valueString)) {
			return getPredefinedValueFromString(valueString, type);
		} else {
			try{
				switch(type){
				case com.testify.ecfeed.model.Constants.TYPE_NAME_BOOLEAN:
					return Boolean.valueOf(valueString).booleanValue();
				case com.testify.ecfeed.model.Constants.TYPE_NAME_BYTE:
					return Byte.decode(valueString);
				case com.testify.ecfeed.model.Constants.TYPE_NAME_CHAR:
					if (valueString.charAt(0) == '\\') {
						return new Character((char)Integer.parseInt(valueString.substring(1)));
					} else if (valueString.length() == 1) {
						return valueString.charAt(0);
					}
					return null;
				case com.testify.ecfeed.model.Constants.TYPE_NAME_DOUBLE:
					return Double.valueOf(valueString).doubleValue();
				case com.testify.ecfeed.model.Constants.TYPE_NAME_FLOAT:
					return Float.valueOf(valueString).floatValue();
				case com.testify.ecfeed.model.Constants.TYPE_NAME_INT:
					return Integer.decode(valueString);
				case com.testify.ecfeed.model.Constants.TYPE_NAME_LONG:
					return Long.decode(valueString);
				case com.testify.ecfeed.model.Constants.TYPE_NAME_SHORT:
					return Short.decode(valueString);
				case com.testify.ecfeed.model.Constants.TYPE_NAME_STRING:
					return valueString;
				default:
					return enumPartitionValue(valueString, type, loader);
				}
			} catch (Throwable e) {
				return null;
			}
		}
	}

	private static boolean isPredefinedValueString(String valueString) {
		return valueString.equals(Constants.NULL_VALUE_STRING_REPRESENTATION) ||
				valueString.equals(Constants.BOOLEAN_FALSE_STRING_REPRESENTATION) ||
				valueString.equals(Constants.BOOLEAN_TRUE_STRING_REPRESENTATION) ||
				valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION) ||
				valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION) ||
				valueString.equals(Constants.POSITIVE_INFINITY_STRING_REPRESENTATION) ||
				valueString.equals(Constants.NEGATIVE_INFINITY_STRING_REPRESENTATION);
	}

	private static Object getPredefinedValueFromString(String valueString, String type){
		switch (type) {
		case com.testify.ecfeed.model.Constants.TYPE_NAME_BOOLEAN:
			return getBooleanPredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_BYTE:
			return getBytePredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_CHAR:
			return getCharPredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_DOUBLE:
			return getDoublePredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_FLOAT:
			return getFloatPredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_INT:
			return getIntegerPredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_LONG:
			return getLongPredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_SHORT:
			return getShortPredefinedValueFromString(valueString);
		case com.testify.ecfeed.model.Constants.TYPE_NAME_STRING:
			if (valueString.equals(Constants.NULL_VALUE_STRING_REPRESENTATION)) {
				return null;
			}
		default:
			return null;
		}
	}

	private static Object getBooleanPredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.BOOLEAN_FALSE_STRING_REPRESENTATION)) {
			return Boolean.FALSE;
		} else if (valueString.equals(Constants.BOOLEAN_TRUE_STRING_REPRESENTATION)) {
			return Boolean.TRUE;
		}
		return null;
	}

	private static Object getBytePredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Byte.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Byte.MAX_VALUE;
		}
		return null;
	}

	private static Object getCharPredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Character.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Character.MAX_VALUE;
		}
		return null;
	}

	private static Object getIntegerPredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Integer.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Integer.MAX_VALUE;
		}
		return null;
	}

	private static Object getLongPredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Long.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Long.MAX_VALUE;
		}
		return null;
	}

	private static Object getShortPredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Short.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Short.MAX_VALUE;
		}
		return null;
	}

	private static Object getDoublePredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Double.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Double.MAX_VALUE;
		} else if (valueString.equals(Constants.POSITIVE_INFINITY_STRING_REPRESENTATION)) {
			return Double.POSITIVE_INFINITY;
		} else if (valueString.equals(Constants.NEGATIVE_INFINITY_STRING_REPRESENTATION)) {
			return Double.NEGATIVE_INFINITY;
		}
		return null;
	}

	private static Object getFloatPredefinedValueFromString(String valueString) {
		if (valueString.equals(Constants.MIN_VALUE_STRING_REPRESENTATION)) {
			return Float.MIN_VALUE;
		} else if (valueString.equals(Constants.MAX_VALUE_STRING_REPRESENTATION)) {
			return Float.MAX_VALUE;
		} else if (valueString.equals(Constants.POSITIVE_INFINITY_STRING_REPRESENTATION)) {
			return Float.POSITIVE_INFINITY;
		} else if (valueString.equals(Constants.NEGATIVE_INFINITY_STRING_REPRESENTATION)) {
			return Float.NEGATIVE_INFINITY;
		}
		return null;
	}
}
