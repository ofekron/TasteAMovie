package ofek.ron.tasteamovie.genericdb;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataField {
	public final static class EmptyClass {}
	public boolean isKey() default false;
	public boolean isAutoIncrement() default false;
	public boolean isNullable() default true;
	public Class references() default EmptyClass.class;
	public String referenceField() default "";
	public boolean isUnique() default false;
}
