package hu.ibello.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import hu.ibello.core.Value;
import hu.ibello.inject.Injectable;
import hu.ibello.inject.Scope;

@Injectable(Scope.SINGLETON)
public class Configuration {
	
	private final Map<String, String> store = new HashMap<>();

	public boolean containsValue(String name) {
		return store.containsKey(name);
	}

	public Value getValue(String name) {
		String property = store.get(name);
		return new ValueImpl(property);
	}
	
	public void setValue(String key, Object value) {
		String stringValue;
		if (value==null) {
			stringValue = null;
		} else if (value instanceof File) {
			stringValue = ((File)value).getAbsolutePath();
		} else if (value.getClass().isArray()) {
			StringBuilder str = new StringBuilder();
			for (int i=0; i<Array.getLength(value); i++) {
				if (i>0) {
					str.append(',');
				}
				Object o = Array.get(value, i);
				if (o!=null) {
					str.append(o.toString());
				}
			}
			stringValue = str.toString();
		} else if (value instanceof Collection) {
			StringBuilder str = new StringBuilder();
			Collection<?> coll = (Collection<?>)value;
			boolean started = false;
			for (Object o : coll) {
				if (started) {
					str.append(',');
				} else {
					started = true;
				}
				if (o!=null) {
					str.append(o.toString());
				}
			}
			stringValue = str.toString();
		} else if (value instanceof Class) {
			stringValue = ((Class<?>)value).getCanonicalName();
		} else {
			stringValue = value.toString();
		}
		store.put(key, stringValue);
	}

}
