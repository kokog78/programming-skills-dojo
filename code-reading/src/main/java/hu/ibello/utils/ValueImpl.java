package hu.ibello.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import hu.ibello.core.Value;

public class ValueImpl implements Value {

	private final Object object;
	
	public ValueImpl(Object object) {
		this.object = object;
	}
	
	@Override
	public String toString() {
		if (object == null) {
			return null;
		} else if (object instanceof Collection) {
			return ((Collection<?>)object).stream()
					.map(item -> item == null ? "" : item.toString())
					.collect(Collectors.joining(","));
		} else if (object.getClass().isArray()) {
			Object[] array = (Object[])object;
			return Arrays.stream(array)
					.map(item -> item == null ? "" : item.toString())
					.collect(Collectors.joining(","));
		} else {
			return object.toString();
		}
	}
	
	@Override
	public String[] toStringArray() {
		if (object==null) {
			return null;
		} else {
			Collection<?> collection = toCollection(object);
			String[] result = new String[collection.size()];
			int index = 0;
			for (Object item : collection) {
				if (item==null) {
					result[index] = null;
				} else {
					String str = item.toString();
					if (str!=null) {
						str = str.trim();
					}
					result[index] = str;
				}
				index++;
			}
			return result;
		}
	}
	
	@Override
	public Long toLong() {
		if (object!=null) {
			if (object instanceof Number) {
				return ((Number)object).longValue();
			} else {
				try {
					return Long.parseLong(toString());
				} catch (NumberFormatException ex) {
					// TODO invalid number
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Integer toInteger() {
		if (object!=null) {
			if (object instanceof Number) {
				return ((Number)object).intValue();
			} else {
				try {
					return Integer.parseInt(toString());
				} catch (NumberFormatException ex) {
					// TODO invalid number
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Short toShort() {
		if (object!=null) {
			if (object instanceof Number) {
				return ((Number)object).shortValue();
			} else {
				try {
					return Short.parseShort(toString());
				} catch (NumberFormatException ex) {
					// TODO invalid number
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Byte toByte() {
		if (object!=null) {
			if (object instanceof Number) {
				return ((Number)object).byteValue();
			} else {
				try {
					return Byte.parseByte(toString());
				} catch (NumberFormatException ex) {
					// TODO invalid number
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Double toDouble() {
		if (object!=null) {
			if (object instanceof Number) {
				return ((Number)object).doubleValue();
			} else {
				try {
					return Double.parseDouble(toString());
				} catch (NumberFormatException ex) {
					// TODO invalid number
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Float toFloat() {
		if (object!=null) {
			if (object instanceof Number) {
				return ((Number)object).floatValue();
			} else {
				try {
					return Float.parseFloat(toString());
				} catch (NumberFormatException ex) {
					// TODO invalid number
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Boolean toBoolean() {
		if (object!=null) {
			return Boolean.parseBoolean(toString());
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<?>> E toEnum(Class<E> type) {
		if (object!=null) {
			if (type.isAssignableFrom(object.getClass())) {
				return (E) object;
			} else {
				return toEnum(type, toString());
			}
		} else {
			return null;
		}
	}
	
	private <E extends Enum<?>> E toEnum(Class<E> type, String name) {
		if (name==null) {
			return null;
		} else {
			name = name.trim();
			if (name.isEmpty()) {
				return null;
			}
		}
		E result = null;
		for (E value : type.getEnumConstants()) {
			if (value.name().equals(name)) {
				return value;
			} else if (value.name().equalsIgnoreCase(name)) {
				result = value;
			}
		}
		// TODO invalid enum
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<E>> EnumSet<E> toEnumSet(Class<E> type) {
		if (object!=null) {
			EnumSet<E> result = EnumSet.noneOf(type);
			Collection<?> collection = toCollection(object);
			for (Object item : collection) {
				if (item!=null) {
					if (type.isAssignableFrom(item.getClass())) {
						result.add((E)item);
					} else {
						String name = item.toString();
						E found = toEnum(type, name);
						if (found!=null) {
							result.add(found);
						}
					}
				}
			}
			return result;
		} else {
			return null;
		}
	}
	
	@Override
	public File toFile() {
		if (object!=null && !object.equals("")) {
			if (object instanceof File) {
				return (File)object;
			} else {
				return new File(toString());
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Class<?> toClass() {
		if (object!=null) {
			if (object instanceof Class) {
				return (Class<?>)object;
			} else {
				try {
					return Class.forName(toString());
				} catch (Exception ex) {
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public Class<?>[] toClassArray() {
		if (object==null) {
			return null;
		} else {
			Collection<?> collection = toCollection(object);
			List<Class<?>> classes = new ArrayList<>();
			for (Object item : collection) {
				if (item!=null) {
					if (item instanceof Class) {
						classes.add((Class<?>)item);
					} else {
						String className = item.toString();
						if (className!=null) {
							try {
								Class<?> clazz = Class.forName(className.trim());
								classes.add(clazz);
							} catch (Exception ex) {
								// TODO not a class
							}
						}
					}
				}
			}
			return classes.toArray(new Class<?>[classes.size()]);
		}
	}
	
	private Collection<?> toCollection(Object value) {
		if (Collection.class.isAssignableFrom(value.getClass())) {
			return (Collection<?>)value;
		} else if (value.getClass().isArray()) {
			ArrayList<Object> result = new ArrayList<>();
			int length = Array.getLength(value);
			for (int i=0; i<length; i++) {
				result.add(Array.get(value, i));
			}
			return result;
		} else if (value instanceof String) {
			String str = ((String)value).trim();
			if (str.isEmpty()) {
				return Collections.emptyList();
			} else {
				return toCollection(str.split("\\,", -1));
			}
		} else {
			return Collections.singletonList(value);
		}
	}
	
}
