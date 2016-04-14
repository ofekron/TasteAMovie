package ofek.ron.tasteamovie.genericdb;

import android.graphics.Bitmap;

import java.lang.reflect.Field;

public class Column {
	private final String name;
	private final String extendedtype;
	private final String sqltype;
	private final boolean isKey;
	private final Field field;
	private final boolean isAutoIncrement;
	private final boolean isNullable;
	private final Class references;
	private final String referenceField;

	public Column(final String prefix, final Field f) {
		field = f;
		field.setAccessible(true);
		extendedtype = getExtendedType(f);
		sqltype = getSqlType(f);
		name = prefix.length() == 0 ? f.getName() : prefix + '_' + f.getName();
		final DataField annotation = f.getAnnotation(DataField.class);
		isKey = annotation.isKey();
		isAutoIncrement = annotation.isAutoIncrement();
		isNullable = annotation.isNullable();
		references = annotation.references();
		referenceField = annotation.referenceField();
	}

	static String getSqlType(final Field f) {
		final Class<?> type = f.getType();
		if (type.equals(Long.class) || type.equals(long.class))
			return "long";
		if (type.equals(Integer.class) || type.equals(int.class) || type.equals(Boolean.class) || type.equals(boolean.class) || type.isEnum())
			return "int";
		if (type.equals(Float.class) || type.equals(float.class) || type.equals(Double.class) || type.equals(double.class))
			return "real";
		if (type.equals(String.class))
			return "text";
		if (type.equals(byte[].class) || type.equals(Bitmap.class))
			return "blob";
		throw new IllegalArgumentException("type not suppurted : " + type);
	}

	static String getExtendedType(final Field f) {
		final Class<?> type = f.getType();
		if (type.equals(Bitmap.class))
			return "image";
		if (type.equals(Boolean.class) || type.equals(boolean.class))
			return "boolean";
		if (type.isEnum())
			return "enum";
		return getSqlType(f);
	}

	public String getName() {
		return name;
	}

	public String getExtendedType() {
		return extendedtype;
	}

	@Override
	public String toString() {
		return name + " " + (isKey && isAutoIncrement ? "INTEGER" : sqltype) + (isNullable ? "" : " NOT NULL")
				+ (references != DataField.EmptyClass.class ? " REFERENCES " + references.getSimpleName() + "(" + referenceField + ") ON DELETE CASCADE " : "");
	}

	public boolean isKey() {
		return isKey;
	}

	public Class getReferencedTable() {
		return references != DataField.EmptyClass.class ? references : null;
	}

	public String getReferencedField() {
		return referenceField;
	}

	public void set(final Object object, final Object value) {
		try {
			field.set(object, value);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Object get(final Object object) {
		try {
			return field.get(object);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Class<?> getJavaType() {
		return field.getType();
	}

	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}

}
