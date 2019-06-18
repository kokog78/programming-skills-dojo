package hu.ibello.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import hu.ibello.actions.Key;
import hu.ibello.inject.Injectable;
import hu.ibello.inject.Scope;
import hu.ibello.utils.ClassUtils;

@Injectable(Scope.SESSION)
public class TextProcessor {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public String processWithConfig(String template, Configuration config) {
		return processInternal(template, (name) -> {
			if (config==null) {
				return null;
			}
			boolean isNumber;
			try {
				Integer.parseInt(name);
				isNumber = true;
			} catch (NumberFormatException ex) {
				isNumber = false;
			}
			if (isNumber) {
				return null;
			} else {
				if (config.containsValue(name)) {
					String replace = config.getValue(name).toString();
					if (replace==null) {
						replace = "";
					}
					return replace;
				} else {
					return null;
				}
			}
		});
	}
	
	public String processWithArgs(String template, Object ... arguments) {
		return processInternal(template, (name) -> {
			if (arguments==null) {
				return null;
			}
			try {
				int argIndex = Integer.parseInt(name);
				if (argIndex>=0 && argIndex<arguments.length) {
					return toString(arguments[argIndex]);
				} else {
					return null;
				}
			} catch (NumberFormatException ex) {
				return null;
			}
		});
	}
	
	public String processWithConfigAndArgs(String template, Configuration config, Object ... arguments) {
		return processWithConfig(processWithArgs(template, arguments), config);
	}
	
	public String processWithObject(String template, Object object) {
		return processInternal(template, (name) -> {
			return getObjectValue(object, name);
		});
	}
	
	public String processWithMap(String template, Map<String, Object> map) {
		return processInternal(template, (name) -> {
			if (map==null) {
				return null;
			}
			Object value = map.get(name);
			return toString(value);
		});
	}
	
	private String getObjectValue(Object object, String name) {
		if (object==null) {
			return null;
		}
		int pos = name.indexOf('.');
		if (pos>0) {
			return getObjectValue(getObjectValue(object, name.substring(0,  pos)), name.substring(pos + 1));
		}
		try {
			Object value = ClassUtils.getValue(object, name);
			return toString(value);
		} catch (Exception ex) {
			// not a field - do nothing
		}
		// TODO remove method search
		try {
			Method method = object.getClass().getMethod(name);
			Object value = method.invoke(object);
			return toString(value);
		} catch (Exception ex) {
			return null;
		}
	}
	
	private String toString(Object obj) {
		if (obj==null) {
			return "";
		} else if (obj instanceof Field) {
			Field field = (Field)obj;
			return field.getDeclaringClass().getCanonicalName() + "." + field.getName();
		} else if (obj instanceof Class) {
			return ((Class<?>)obj).getCanonicalName();
		} else if (obj instanceof Key) {
			return ((Key)obj).name();
		} else if (obj instanceof Date) {
			return dateFormat.format(obj);
		} else if (obj.getClass().isArray()) {
			StringBuilder result = new StringBuilder();
			int length = Array.getLength(obj);
			for (int i=0; i<length; i++) {
				if (i>0) {
					result.append(", ");
				}
				result.append(toString(Array.get(obj, i)));
			}
			return result.toString();
		} else {
			return obj.toString();
		}
	}
	
	private String processInternal(String template, Resolver resolver) {
		if (template==null) {
			throw new NullPointerException();
		}
		StringBuilder result = new StringBuilder();
		StringBuilder expression = new StringBuilder();
		int index = 0;
		int level = 0;
		char prevChar = ' ';
		while (index<template.length()) {
			char c = template.charAt(index);
			switch (c) {
			case '$':
				if (level>0) {
					expression.append(c);
				}
				break;
			case '{':
				if (level>0) {
					expression.append(c);
					level++;
				} else if (prevChar=='$') {
					level = 1;
					expression.delete(0, expression.length());
				} else {
					result.append(c);
				}
				break;
			case '}':
				if (level>0) {
					level--;
					if (level==0) {
						String expr = expression.toString();
						expression.delete(0, expression.length());
						String name;
						String pattern;
						int questionmarkPos = expr.indexOf('?');
						if (questionmarkPos>0) {
							name = expr.substring(0, questionmarkPos).trim();
							pattern = expr.substring(questionmarkPos + 1);
						} else {
							name = expr.trim();
							pattern = null;
						}
						boolean invert = false;
						if (name.startsWith("!")) {
							name = name.substring(1).trim();
							invert = true;
						}
						String replacement = resolver.resolve(name);
						if (replacement==null) {
							replacement = "${" + expr + '}';
						} else if (pattern!=null) {
							if (invert == replacement.isEmpty()) {
								replacement = processInternal(pattern, resolver);
							} else {
								replacement = "";
							}
						}
						result.append(replacement);
					} else {
						expression.append(c);
					}
				} else {
					if (prevChar=='$') {
						result.append(prevChar);
					}
					result.append(c);
				}
				break;
			default:
				if (level>0) {
					expression.append(c);
				} else {
					if (prevChar=='$') {
						result.append(prevChar);
					}
					result.append(c);
				}
				break;
			}
			prevChar = c;
			index++;
		}
		if (level==0) {
			if (prevChar=='$') {
				result.append(prevChar);
			}
		}
		return result.toString();
	}
	
	@FunctionalInterface
	private interface Resolver {
		public String resolve(String name);
	}
	
}
