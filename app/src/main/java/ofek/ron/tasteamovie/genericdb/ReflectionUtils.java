package ofek.ron.tasteamovie.genericdb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Ofek Ron
 *
 */
public class ReflectionUtils {
	public static final Class<?> getPrimitiveForWrapper(final Class<?> class1) {
		if (class1.equals(Byte.class))
			return byte.class;
		if (class1.equals(Short.class))
			return short.class;
		if (class1.equals(Integer.class))
			return int.class;
		if (class1.equals(Long.class))
			return long.class;
		if (class1.equals(Character.class))
			return char.class;
		if (class1.equals(Float.class))
			return float.class;
		if (class1.equals(Double.class))
			return double.class;
		if (class1.equals(Boolean.class))
			return boolean.class;
		if (class1.equals(Void.class))
			return void.class;
		return class1;
	}
	public static Object stringToPrimitive(final String s, final Class<?> type) {
		if (type==String.class) return s;
		if (type==Integer.class || type==int.class) return Integer.parseInt(s);
		if (type==Double.class || type==double.class) return Double.parseDouble(s);
		if (type==Long.class || type==long.class) return Long.parseLong(s);
		if (type==Boolean.class || type==boolean.class) return Boolean.parseBoolean(s);
		if (type==Float.class || type==float.class) return Float.parseFloat(s);
		if (type==Short.class || type==short.class) return Short.parseShort(s);
		if (type==Byte.class || type==byte.class) return Byte.parseByte(s);
		if (type==Character.class || type==char.class) return Character.valueOf(s.charAt(0));
		if (type.isEnum()) return Enum.valueOf((Class<Enum>)type, s);
		return null;
	}
	public static Field getField(final Class<? extends Object> class1,
			final String fieldName) {
		Field declaredField = null;
		try {
			declaredField = class1.getDeclaredField(fieldName);
		} catch (final NoSuchFieldException e) {
			final Class<?> superclass = class1.getSuperclass();
			return superclass!=null ? getField(superclass,fieldName) : null;
		}
		return declaredField;

	}

	public static Object getValue(final Object obj,final String fieldName) throws Exception {
		final Field field = getField(obj.getClass(),fieldName);
		field.setAccessible(true);
		return field.get(obj);
	}

	public static void invoke(final Object on, final String methodName, final Object... params) {
		final Class[] classes = new Class[params.length];
		for (int i = 0 ; i < params.length ;i++) 
			classes[i] = params[i].getClass();
		Method declaredMethod;
		try {
			declaredMethod = on.getClass().getDeclaredMethod(methodName, classes);
		} catch (final Exception e1) {
			throw new IllegalArgumentException("method doesnt exists!");
		}
		if (declaredMethod==null) 
			throw new IllegalArgumentException("method doesnt exists!");
		try {
			declaredMethod.setAccessible(true);
			declaredMethod.invoke(on, params);
		} catch (final Exception e) {
			throw new IllegalArgumentException("couldnt invoke method!");
		}
	}
	public static Method getMethod(final Class<? extends Object> clazz,final Object[] params, final String methodName) {
		if (clazz==null) return null;
		for (final Method m : clazz.getMethods())
			if (m.getName().equals(methodName) && validParams(params,m)) return m;
		return getMethod(clazz.getSuperclass(),params,methodName);
	}


	public static void setValue(final Object obj,final String fieldName,final Object value) throws Exception {
		final Field field = getField(obj.getClass(),fieldName);
		field.setAccessible(true);
		field.set(obj,value);
	}
	public static ArrayList<Method> getMethods(final Class clazz, final String methodName,final Object[] params,final ArrayList<Method> results) {
		if (clazz==null) 
			return results;
		for (final Method m : clazz.getMethods())
			if (m.getName().equals(methodName) && validParams(params,m)) 
				results.add(m);
		return getMethods(clazz.getSuperclass(),methodName,params,results);

	}
	/**
	 * @param params
	 * @param m
	 * @return
	 */
	public static boolean validParams(final Object[] params, final Method m) {
		final Class<?>[] parameterTypes = m.getParameterTypes();
		if ( parameterTypes.length!=params.length ) return false;
		for (int i = 0 ; i < parameterTypes.length ; i++)  { 
			if (!parameterTypes[i].isInstance(params[i])) return false;
		}
		return true;
	}

	public static Method getMethod(final Class<? extends Object> clazz,final String methodName, final Object[] params) {
		return getMethod(clazz,params,methodName);
	}
	/**
	 * @param obj
	 * @return
	 */
	public static boolean isPrimitive(final Object obj) {
		final Class<? extends Object> type = obj.getClass();
		if (type.isPrimitive()) return true;
		if (type==String.class) return true;
		if (type==Integer.class || type==int.class) return true;
		if (type==Double.class || type==double.class) return true;
		if (type==Long.class || type==long.class) return true;
		if (type==Boolean.class || type==boolean.class) return true;
		if (type==Float.class || type==float.class) return true;
		if (type==Short.class || type==short.class) return true;
		if (type==Byte.class || type==byte.class) return true;
		if (type==Character.class || type==char.class) return true;
		if (type.isEnum()) return true;
		return false;
	}
	/**
	 * @param obj
	 * @return
	 */
	public static boolean isCollection(final Object obj) {
		return obj instanceof Collection || obj instanceof List || obj instanceof Map;
	}
}

